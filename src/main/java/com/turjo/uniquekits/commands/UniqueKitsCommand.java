package com.turjo.uniquekits.commands;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.kits.Kit;
import com.turjo.uniquekits.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UniqueKitsCommand implements CommandExecutor, TabCompleter {
    
    private final UniqueKits plugin;
    
    public UniqueKitsCommand(UniqueKits plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                showHelp(sender);
                break;
                
            case "list":
                listKits(sender);
                break;
                
            case "create":
                if (args.length < 2) {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("commands.usage.create"));
                    return true;
                }
                createKit(sender, args[1]);
                break;
                
            case "edit":
                if (!(sender instanceof Player)) {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.player-only"));
                    return true;
                }
                if (args.length < 2) {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("commands.usage.edit"));
                    return true;
                }
                editKit((Player) sender, args[1]);
                break;
                
            case "delete":
                if (args.length < 2) {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("commands.usage.delete"));
                    return true;
                }
                deleteKit(sender, args[1]);
                break;
                
            case "give":
                if (args.length < 3) {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("commands.usage.give"));
                    return true;
                }
                giveKit(sender, args[1], args[2]);
                break;
                
            case "reload":
                reloadPlugin(sender);
                break;
                
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        if (!sender.hasPermission("uniquekits.command.help")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        List<String> helpMessages = plugin.getLanguageManager().getMessage("commands.help.header")
            .equals("commands.help.header") ? Arrays.asList(
                "§6§l=== UniqueKits Help ===",
                "§e/kit [name] §7- Claim a kit",
                "§e/kit list §7- List available kits",
                "§e/kit preview <name> §7- Preview a kit",
                "§e/uniquekits help §7- Show this help",
                "§e/uniquekits list §7- List all kits (admin)",
                "§e/uniquekits create <name> §7- Create a kit",
                "§e/uniquekits edit <name> §7- Edit a kit",
                "§e/uniquekits delete <name> §7- Delete a kit",
                "§e/uniquekits give <player> <kit> §7- Give kit to player",
                "§e/uniquekits reload §7- Reload the plugin",
                "§6§l===================="
            ) : Arrays.asList(
                plugin.getLanguageManager().getMessage("commands.help.header"),
                plugin.getLanguageManager().getMessage("commands.help.footer")
            );
        
        for (String message : helpMessages) {
            MessageUtils.sendMessage(sender, message);
        }
    }
    
    private void listKits(CommandSender sender) {
        if (!sender.hasPermission("uniquekits.admin")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        MessageUtils.sendMessage(sender, "§6§l=== All Kits ===");
        
        if (plugin.getKitManager().getAllKits().isEmpty()) {
            MessageUtils.sendMessage(sender, "§cNo kits found!");
            return;
        }
        
        for (Kit kit : plugin.getKitManager().getAllKits()) {
            String status = kit.isEnabled() ? "§a✓" : "§c✗";
            MessageUtils.sendMessage(sender, status + " §e" + kit.getId() + " §7- " + kit.getName());
        }
        
        MessageUtils.sendMessage(sender, "§6§l===============");
    }
    
    private void createKit(CommandSender sender, String kitId) {
        if (!sender.hasPermission("uniquekits.command.create")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        if (plugin.getKitManager().kitExists(kitId)) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("admin.kit-exists", "{kit}", kitId));
            return;
        }
        
        plugin.getKitManager().createKit(kitId);
        MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("admin.kit-created", "{kit}", kitId));
    }
    
    private void editKit(Player player, String kitId) {
        if (!player.hasPermission("uniquekits.command.edit")) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        Kit kit = plugin.getKitManager().getKit(kitId);
        if (kit == null) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.not-found", "{kit}", kitId));
            return;
        }
        
        plugin.getGuiManager().openKitEditor(player, kit);
        MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("admin.editor-opened", "{kit}", kit.getName()));
    }
    
    private void deleteKit(CommandSender sender, String kitId) {
        if (!sender.hasPermission("uniquekits.command.delete")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        if (!plugin.getKitManager().kitExists(kitId)) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("admin.kit-not-exists", "{kit}", kitId));
            return;
        }
        
        plugin.getKitManager().deleteKit(kitId);
        MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("admin.kit-deleted", "{kit}", kitId));
    }
    
    private void giveKit(CommandSender sender, String playerName, String kitId) {
        if (!sender.hasPermission("uniquekits.command.give")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.invalid-player", "{player}", playerName));
            return;
        }
        
        Kit kit = plugin.getKitManager().getKit(kitId);
        if (kit == null) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("kit.not-found", "{kit}", kitId));
            return;
        }
        
        boolean success = plugin.getKitManager().giveKit(target, kit, true);
        if (success) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("kit.given", 
                "{kit}", kit.getName(), "{player}", target.getName()));
            MessageUtils.sendMessage(target, plugin.getLanguageManager().getMessage("kit.received-from", 
                "{kit}", kit.getName(), "{sender}", sender.getName()));
        } else {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("errors.kit-error"));
        }
    }
    
    private void reloadPlugin(CommandSender sender) {
        if (!sender.hasPermission("uniquekits.command.reload")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        try {
            plugin.reload();
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.reload-success"));
        } catch (Exception e) {
            MessageUtils.sendMessage(sender, "§cReload failed: " + e.getMessage());
            plugin.getLogger().severe("Reload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "list", "create", "edit", "delete", "give", "reload");
            return subCommands.stream()
                .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "edit":
                case "delete":
                case "give":
                    return plugin.getKitManager().getKitNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                case "create":
                    return completions; // No completions for new kit names
            }
        }
        
        if (args.length == 3 && args[0].toLowerCase().equals("give")) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}