package com.turjo.uniquekits.commands;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.kits.Kit;
import com.turjo.uniquekits.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KitCommand implements CommandExecutor, TabCompleter {
    
    private final UniqueKits plugin;
    
    public KitCommand(UniqueKits plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("uniquekits.kit.use")) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("general.no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            // Open kit GUI
            plugin.getGuiManager().openKitSelection(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list":
                listAvailableKits(player);
                break;
                
            case "preview":
                if (args.length < 2) {
                    MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("commands.usage.preview"));
                    return true;
                }
                previewKit(player, args[1]);
                break;
                
            default:
                // Try to give the kit
                claimKit(player, args[0]);
                break;
        }
        
        return true;
    }
    
    private void listAvailableKits(Player player) {
        List<Kit> availableKits = plugin.getKitManager().getAvailableKits(player);
        
        if (availableKits.isEmpty()) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.no-kits-available"));
            return;
        }
        
        MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.list-header"));
        
        for (Kit kit : availableKits) {
            StringBuilder message = new StringBuilder();
            message.append(plugin.getLanguageManager().getMessage("kit.list-item", 
                "{kit}", kit.getName(), "{description}", kit.getDescription()));
            
            // Add cooldown info if applicable
            if (kit.getCooldown() > 0) {
                // Check if player has cooldown
                // This would require implementing cooldown checking
                // For now, just show if kit has cooldown
                message.append(" ").append(plugin.getLanguageManager().getMessage("kit.list-cooldown", "{time}", ""));
            }
            
            // Add cost info if applicable
            if (kit.getCost() > 0) {
                message.append(" ").append(plugin.getLanguageManager().getMessage("kit.list-cost", "{cost}", String.valueOf(kit.getCost())));
            }
            
            MessageUtils.sendMessage(player, message.toString());
        }
        
        MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.list-footer"));
    }
    
    private void previewKit(Player player, String kitId) {
        Kit kit = plugin.getKitManager().getKit(kitId);
        if (kit == null) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.not-found", "{kit}", kitId));
            return;
        }
        
        plugin.getGuiManager().openKitPreview(player, kit);
    }
    
    private void claimKit(Player player, String kitId) {
        Kit kit = plugin.getKitManager().getKit(kitId);
        if (kit == null) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.not-found", "{kit}", kitId));
            return;
        }
        
        plugin.getKitManager().giveKit(player, kit, false);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Add subcommands
            completions.add("list");
            completions.add("preview");
            
            // Add available kit names
            List<Kit> availableKits = plugin.getKitManager().getAvailableKits(player);
            completions.addAll(availableKits.stream()
                .map(Kit::getId)
                .collect(Collectors.toList()));
            
            return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].toLowerCase().equals("preview")) {
            List<Kit> availableKits = plugin.getKitManager().getAvailableKits(player);
            return availableKits.stream()
                .map(Kit::getId)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}