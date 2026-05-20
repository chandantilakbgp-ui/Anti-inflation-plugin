package com.ecoeco.commands;

import com.ecoeco.EcoEco;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EcoEcoCommand implements CommandExecutor {

    private final EcoEco plugin;

    public EcoEcoCommand(EcoEco plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix"));
        
        if (!sender.hasPermission("ecoeco.admin")) {
            sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to execute this administrative action.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getTaxManager().reloadSystem();
            sender.sendMessage(prefix + ChatColor.GREEN + "Configuration and active database caching models reloaded.");
            return true;
        }

        sender.sendMessage(prefix + ChatColor.YELLOW + "EcoEco Economy Engine v1.3.0. Use /ecoeco reload to refresh parameters.");
        return true;
    }
}
