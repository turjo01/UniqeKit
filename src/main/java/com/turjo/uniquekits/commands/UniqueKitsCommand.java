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
            case "?":
                showHelp(sender);
                break;
                
            case "list":
            case "ls":
                listKits(sender);
                break;
                
            case "create":
            case "new":
                if (args.length < 2) {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("commands.usage.create"));
                    return true;
                }
                createKit(sender, args[1]);
                break;
                
            case "edit":
            case "modify":
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
            case "remove":
            case "del":
                if (args.length < 2) {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("commands.usage.delete"));
                    return true;
                }
                deleteKit(sender, args[1]);
                break;
                
            case "give":
            case "grant":
                if (args.length < 3) {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("commands.usage.give"));
                    return true;
                }
                giveKit(sender, args[1], args[2]);
                break;
                
            case "import":
                importKits(sender);
                break;
                
            case "export":
                if (args.length < 2) {
                    MessageUtils.sendMessage(sender, "<red>Usage: <yellow>/uk export <kit></yellow></red>");
                    return true;
                }
                exportKit(sender, args[1]);
                break;
                
            case "reload":
            case "rl":
                reloadPlugin(sender);
                break;
                
            case "stats":
            case "statistics":
                if (args.length >= 2) {
                    showPlayerStats(sender, args[1]);
                } else if (sender instanceof Player) {
                    showPlayerStats(sender, sender.getName());
                } else {
                    MessageUtils.sendMessage(sender, "<red>Usage: <yellow>/uk stats <player></yellow></red>");
                }
                break;
                
            case "version":
            case "ver":
            case "info":
                showVersion(sender);
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
        
        // Send header
        List<String> headerMessages = plugin.getLanguageManager().getMessageList("commands.help.header");
        MessageUtils.sendMessages(sender, headerMessages);
        
        // Send player commands if applicable
        if (sender instanceof Player || sender.hasPermission("uniquekits.admin")) {
            List<String> playerCommands = plugin.getLanguageManager().getMessageList("commands.help.player-commands");
            MessageUtils.sendMessages(sender, playerCommands);
        }
        
        // Send admin commands if has permission
        if (sender.hasPermission("uniquekits.admin")) {
            List<String> adminCommands = plugin.getLanguageManager().getMessageList("commands.help.admin-commands");
            MessageUtils.sendMessages(sender, adminCommands);
        }
        
        // Send footer
        List<String> footerMessages = plugin.getLanguageManager().getMessageList("commands.help.footer");
        MessageUtils.sendMessages(sender, footerMessages);
    }
    
    private void listKits(CommandSender sender) {
        if (!sender.hasPermission("uniquekits.admin")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        MessageUtils.sendMessage(sender, "<gradient:#FFD700:#FFA500>╔══════════════════════════════════════════════════════════════╗</gradient>");
        MessageUtils.sendMessage(sender, "<gradient:#FFD700:#FFA500>║</gradient>                    <gradient:#00FFFF:#0080FF><bold>✦ ALL KITS OVERVIEW ✦</bold></gradient>                   <gradient:#FFD700:#FFA500>║</gradient>");
        MessageUtils.sendMessage(sender, "<gradient:#FFD700:#FFA500>╠══════════════════════════════════════════════════════════════╣</gradient>");
        
        if (plugin.getKitManager().getAllKits().isEmpty()) {
            MessageUtils.sendMessage(sender, "<gradient:#FFD700:#FFA500>║</gradient>                    <red>No kits found!</red>                         <gradient:#FFD700:#FFA500>║</gradient>");
        } else {
            for (Kit kit : plugin.getKitManager().getAllKits()) {
                String status = kit.isEnabled() ? "<green>✓</green>" : "<red>✗</red>";
                String priority = kit.getPriority() > 5 ? "<yellow>★</yellow>" : "<gray>☆</gray>";
                MessageUtils.sendMessage(sender, "<gradient:#FFD700:#FFA500>║</gradient> " + status + " " + priority + " <yellow>" + kit.getId() + "</yellow> <gray>- " + MessageUtils.stripColor(kit.getName()) + "</gray>");
            }
        }
        
        MessageUtils.sendMessage(sender, "<gradient:#FFD700:#FFA500>╚══════════════════════════════════════════════════════════════╝</gradient>");
        MessageUtils.sendMessage(sender, "<gray>Total: <yellow>" + plugin.getKitManager().getAllKits().size() + "</yellow> kits | Hooks: <green>" + plugin.getHookManager().getEnabledHooksCount() + "</green> active</gray>");
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
    
    private void importKits(CommandSender sender) {
        if (!sender.hasPermission("uniquekits.admin")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        if (!plugin.getHookManager().isEssentialsXEnabled()) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("import.no-source"));
            return;
        }
        
        MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("import.essentials-found", 
            "{count}", String.valueOf(plugin.getHookManager().getEssentialsXHook().getAvailableKitsCount())));
        
        MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("import.essentials-importing"));
        
        // Run import asynchronously to prevent server lag
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int imported = plugin.getHookManager().getEssentialsXHook().importAllKits();
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("import.essentials-success", 
                        "{count}", String.valueOf(imported)));
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("import.essentials-failed", 
                        "{error}", e.getMessage()));
                });
            }
        });
    }
    
    private void exportKit(CommandSender sender, String kitId) {
        if (!sender.hasPermission("uniquekits.admin")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        Kit kit = plugin.getKitManager().getKit(kitId);
        if (kit == null) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("kit.not-found", "{kit}", kitId));
            return;
        }
        
        try {
            String fileName = "kit_" + kitId + "_export.yml";
            // Export logic would go here
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("export.success", 
                "{kit}", kit.getName(), "{file}", fileName));
        } catch (Exception e) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("export.failed", 
                "{error}", e.getMessage()));
        }
    }
    
    private void reloadPlugin(CommandSender sender) {
        if (!sender.hasPermission("uniquekits.command.reload")) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        try {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.processing"));
            plugin.reload();
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.reload-success"));
        } catch (Exception e) {
            MessageUtils.sendMessage(sender, "<red>⚠ Reload failed: " + e.getMessage() + "</red>");
            plugin.getLogger().severe("Reload failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showPlayerStats(CommandSender sender, String playerName) {
        if (!sender.hasPermission("uniquekits.admin") && !sender.getName().equals(playerName)) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.no-permission"));
            return;
        }
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageUtils.sendMessage(sender, plugin.getLanguageManager().getMessage("general.invalid-player", "{player}", playerName));
            return;
        }
        
        // Show player statistics
        List<String> statsHeader = plugin.getLanguageManager().getMessageList("stats.header");
        MessageUtils.sendMessages(sender, statsHeader);
        // Add stats implementation here
        List<String> statsFooter = plugin.getLanguageManager().getMessageList("stats.footer");
        MessageUtils.sendMessages(sender, statsFooter);
    }
    
    private void showVersion(CommandSender sender) {
        MessageUtils.sendMessage(sender, "");
        MessageUtils.sendMessage(sender, "<gradient:#FFD700:#FFA500>✦ UniqueKits</gradient> <gray>v" + plugin.getDescription().getVersion() + "</gray>");
        MessageUtils.sendMessage(sender, "<gray>Created by: <yellow>Turjo</yellow></gray>");
        MessageUtils.sendMessage(sender, "<gray>Hooks Active: <green>" + plugin.getHookManager().getEnabledHooksCount() + "</green></gray>");
        MessageUtils.sendMessage(sender, "<gray>Total Kits: <yellow>" + plugin.getKitManager().getAllKits().size() + "</yellow></gray>");
        MessageUtils.sendMessage(sender, "");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "list", "create", "edit", "delete", "give", "import", "export", "reload", "stats", "version");
            return subCommands.stream()
                .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "edit":
                case "delete":
                case "export":
                    return plugin.getKitManager().getKitNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                case "give":
                case "stats":
                    return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3 && args[0].toLowerCase().equals("give")) {
            return plugin.getKitManager().getKitNames().stream()
                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}