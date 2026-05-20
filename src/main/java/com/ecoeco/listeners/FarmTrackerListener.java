package com.ecoeco.listeners;

import com.ecoeco.EcoEco;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class FarmTrackerListener implements Listener {

    private final EcoEco plugin;

    public FarmTrackerListener(EcoEco plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Chunk chunk = item.getLocation().getChunk();
        String chunkKey = chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();

        plugin.getTaxManager().trackSpawn(chunkKey);
        int threshold = plugin.getConfig().getInt("settings.items-per-chunk-threshold", 150);

        if (plugin.getTaxManager().getSpawnCount(chunkKey) > threshold) {
            // Safe synchronous call inside main thread event space
            for (Entity entity : item.getNearbyEntities(16.0, 16.0, 16.0)) {
                if (entity instanceof Player) {
                    plugin.getTaxManager().applyTaxPenalty((Player) entity);
                }
            }
        }
    }
}
