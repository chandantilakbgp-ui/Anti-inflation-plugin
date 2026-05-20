package com.ecoeco;

import com.ecoeco.commands.EcoEcoCommand;
import com.ecoeco.listeners.ConnectionListener;
import com.ecoeco.listeners.FarmTrackerListener;
import com.ecoeco.listeners.ShopGUIPlusListener;
import com.ecoeco.managers.TaxManager;
import com.ecoeco.storage.MySQLStorage;
import com.ecoeco.storage.StorageProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class EcoEco extends JavaPlugin {

    private static EcoEco instance;
    private Economy econ = null;
    private StorageProvider storage;
    private TaxManager taxManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe("Vault interface mapping fell through. Disabling engine dependencies.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize advanced Hikari connection pool system
        this.storage = new MySQLStorage(this);
        this.storage.init();

        this.taxManager = new TaxManager(this, storage);

        this.getCommand("ecoeco").setExecutor(new EcoEcoCommand(this));
        
        getServer().getPluginManager().registerEvents(new FarmTrackerListener(this), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);

        if (getServer().getPluginManager().getPlugin("ShopGUIPlus") != null) {
            getServer().getPluginManager().registerEvents(new ShopGUIPlusListener(this), this);
            getLogger().info("[EcoEco Hook] Successfully mapped runtime intercept pipeline into ShopGUI+ API.");
        }

        long intervalTicks = getConfig().getInt("settings.eval-interval-seconds", 10) * 20L;
        // Asynchronous caching wipes provide perfect 20.0 TPS maintenance windows
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> taxManager.clearCache(), intervalTicks, intervalTicks);

        getLogger().info("EcoEco Professional Enterprise System finalized initialization.");
    }

    @Override
    public void onDisable() {
        if (taxManager != null) taxManager.reloadSystem();
        if (storage != null) storage.close();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        return (rsp != null && (econ = rsp.getProvider()) != null);
    }

    public static EcoEco getInstance() { return instance; }
    public Economy getEconomy() { return econ; }
    public TaxManager getTaxManager() { return taxManager; }
}
