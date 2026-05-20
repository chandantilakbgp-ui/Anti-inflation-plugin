package com.ecoeco.listeners;

import com.ecoeco.EcoEco;
import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager.ShopAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ShopGUIPlusListener implements Listener {

    private final EcoEco plugin;

    public ShopGUIPlusListener(EcoEco plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShopTransaction(ShopPreTransactionEvent event) {
        if (event.getShopAction() != ShopAction.SELL && event.getShopAction() != ShopAction.SELL_ALL) {
            return;
        }

        Player player = event.getPlayer();
        double activeTaxRate = plugin.getTaxManager().getPlayerTax(player.getUniqueId());

        if (activeTaxRate <= 0.0) {
            return;
        }

        double originalPrice = event.getPrice();
        double reductionAmount = originalPrice * activeTaxRate;
        double adjustedPrice = originalPrice - reductionAmount;

        event.setPrice(adjustedPrice);
    }
}
