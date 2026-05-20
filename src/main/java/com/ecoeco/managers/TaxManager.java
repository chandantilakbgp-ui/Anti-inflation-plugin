package com.ecoeco.managers;

import com.ecoeco.EcoEco;
import com.ecoeco.storage.StorageProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaxManager {

    private final EcoEco plugin;
    private final StorageProvider storage;
    private final ConcurrentHashMap<String, Integer> chunkItemSpawns = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Double> playerTaxRates = new ConcurrentHashMap<>();

    public TaxManager(EcoEco plugin, StorageProvider storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void trackSpawn(String chunkKey) {
        chunkItemSpawns.merge(chunkKey, 1, Integer::sum);
    }

    public int getSpawnCount(String chunkKey) {
        return chunkItemSpawns.getOrDefault(chunkKey, 0);
    }

    public void loadUserData(UUID uuid) {
        storage.loadPlayerTax(uuid).thenAccept(taxRate -> {
            if (taxRate > 0.0) {
                playerTaxRates.put(uuid, taxRate);
            }
        });
    }

    public void unloadUserData(UUID uuid) {
        Double activeTax = playerTaxRates.remove(uuid);
        if (activeTax != null) {
            storage.savePlayerTax(uuid, activeTax);
        }
    }

    public void applyTaxPenalty(Player player) {
        if (player.hasPermission("ecoeco.bypass")) return;

        UUID uuid = player.getUniqueId();
        FileConfiguration config = plugin.getConfig();
        double maxTax = config.getDouble("taxing.max-tax-multiplier", 0.30);
        double growth = config.getDouble("taxing.increment-rate", 0.01);

        playerTaxRates.merge(uuid, growth, (oldVal, newVal) -> Math.min(oldVal + newVal, maxTax));
        
        String alert = config.getString("messages.alert-taxed", "&cHigh automation detected! Market tax applied.");
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix") + alert));
    }

    public double getPlayerTax(UUID uuid) {
        return playerTaxRates.getOrDefault(uuid, 0.0);
    }

    public void clearCache() {
        chunkItemSpawns.clear();
    }
    
    public void reloadSystem() {
        chunkItemSpawns.clear();
        playerTaxRates.forEach(storage::savePlayerTax);
        playerTaxRates.clear();
    }
}
