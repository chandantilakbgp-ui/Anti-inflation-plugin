package com.ecoeco.storage;

import com.ecoeco.EcoEco;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MySQLStorage implements StorageProvider {

    private final EcoEco plugin;
    private HikariDataSource dataSource;

    public MySQLStorage(EcoEco plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            HikariConfig config = new HikariConfig();
            
            String host = plugin.getConfig().getString("mysql.host", "127.0.0.1");
            int port = plugin.getConfig().getInt("mysql.port", 3306);
            String database = plugin.getConfig().getString("mysql.database", "minecraft");
            
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(plugin.getConfig().getString("mysql.username", "root"));
            config.setPassword(plugin.getConfig().getString("mysql.password", "password"));
            
            // Core Performance & Connection Optimization Metrics
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            // Safeguard 1: Handle network drops and avoid ghost connections hanging on the proxy
            config.setMaxLifetime(1800000); // 30 minutes
            config.setConnectionTimeout(5000); // 5 seconds
            config.setMaximumPoolSize(plugin.getConfig().getInt("mysql.max-pool-size", 10));

            this.dataSource = new HikariDataSource(config);

            // Safeguard 2: Force modern UTF-8 encoding patterns to prevent Geyser/Floodgate crash loops
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Enforce global database collation matching before executing table builds
                stmt.execute("SET NAMES utf8mb4;");
                
                stmt.execute("CREATE TABLE IF NOT EXISTS eco_taxes (" +
                        "uuid VARCHAR(36) PRIMARY KEY," +
                        "tax_rate DOUBLE DEFAULT 0.0" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize critical connection pooling mapping layers!", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public CompletableFuture<Double> loadPlayerTax(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT tax_rate FROM eco_taxes WHERE uuid = ?;";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("tax_rate");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "SQL thread lookup exception on UUID: " + uuid, e);
            }
            return 0.0;
        });
    }

    @Override
    public CompletableFuture<Void> savePlayerTax(UUID uuid, double taxRate) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO eco_taxes (uuid, tax_rate) VALUES (?, ?) " +
                           "ON DUPLICATE KEY UPDATE tax_rate = ?;";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.setDouble(2, taxRate);
                ps.setDouble(3, taxRate);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "SQL data pipeline write failure!", e);
            }
        });
    }
}
