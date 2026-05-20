package com.ecoeco.listeners;

import com.ecoeco.EcoEco;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final EcoEco plugin;

    public ConnectionListener(EcoEco plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getTaxManager().loadUserData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getTaxManager().unloadUserData(event.getPlayer().getUniqueId());
    }
}
