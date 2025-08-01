package com.turjo.uniquekits.kits;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.storage.PlayerData;
import com.turjo.uniquekits.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KitManager {
    
    private final UniqueKits plugin;
    private final Map<String, Kit> kits = new ConcurrentHashMap<>();
    private FileConfiguration kitsConfig;
    private File kitsFile;
    
    public KitManager(UniqueKits plugin) {
        this.plugin = plugin;
        loadKits();
        createExampleKit();
    }
    
    private void loadKits() {
        kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        
        if (kitsConfig.contains("kits")) {
            ConfigurationSection kitsSection = kitsConfig.getConfigurationSection("kits");
            if (kitsSection != null) {
                for (String kitId : kitsSection.getKeys(false)) {
                    ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitId);
                    if (kitSection != null) {
                        Kit kit = Kit.fromConfig(kitId, kitSection);
                        kits.put(kitId.toLowerCase(), kit);
                    }
                }
            }
        }
        
        plugin.getLogger().info("§a[KitManager] Loaded " + kits.size() + " kits successfully!");
    }
    
    private void createExampleKit() {
        if (!kits.containsKey("starter")) {
            Kit starterKit = new Kit("starter");
            starterKit.setName("§a§lStarter Kit");
            starterKit.setDescription("§7A basic starter kit for new players");
            starterKit.setLore(Arrays.asList(
                "§7This kit contains basic items",
                "§7to help you get started!",
                "",
                "§e§lCooldown: §f30 minutes",
                "§a§lFree kit!"
            ));
            
            // Add some basic items
            List<ItemStack> items = new ArrayList<>();
            items.add(new ItemStack(org.bukkit.Material.STONE_SWORD, 1));
            items.add(new ItemStack(org.bukkit.Material.LEATHER_HELMET, 1));
            items.add(new ItemStack(org.bukkit.Material.LEATHER_CHESTPLATE, 1));
            items.add(new ItemStack(org.bukkit.Material.LEATHER_LEGGINGS, 1));
            items.add(new ItemStack(org.bukkit.Material.LEATHER_BOOTS, 1));
            items.add(new ItemStack(org.bukkit.Material.BREAD, 16));
            items.add(new ItemStack(org.bukkit.Material.OAK_LOG, 32));
            
            starterKit.setItems(items);
            starterKit.setCooldown(1800000); // 30 minutes in milliseconds
            starterKit.setFirstJoinKit(true);
            starterKit.setSound("ENTITY_PLAYER_LEVELUP");
            
            kits.put("starter", starterKit);
            saveKit(starterKit);
            
            plugin.getLogger().info("§a[KitManager] Created example 'starter' kit!");
        }
    }
    
    public void reloadKits() {
        kits.clear();
        loadKits();
        plugin.getLogger().info("§a[KitManager] Kits reloaded successfully!");
    }
    
    public void saveKit(Kit kit) {
        if (kitsConfig == null) return;
        
        ConfigurationSection kitsSection = kitsConfig.getConfigurationSection("kits");
        if (kitsSection == null) {
            kitsSection = kitsConfig.createSection("kits");
        }
        
        ConfigurationSection kitSection = kitsSection.createSection(kit.getId());
        kit.saveToConfig(kitSection);
        
        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("§c[KitManager] Could not save kit " + kit.getId() + ": " + e.getMessage());
        }
    }
    
    public void deleteKit(String kitId) {
        kits.remove(kitId.toLowerCase());
        
        if (kitsConfig.contains("kits." + kitId)) {
            kitsConfig.set("kits." + kitId, null);
            try {
                kitsConfig.save(kitsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("§c[KitManager] Could not delete kit " + kitId + ": " + e.getMessage());
            }
        }
    }
    
    public boolean giveKit(Player player, String kitId, boolean bypassChecks) {
        Kit kit = getKit(kitId);
        if (kit == null) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.not-found", "{kit}", kitId));
            return false;
        }
        
        return giveKit(player, kit, bypassChecks);
    }
    
    public boolean giveKit(Player player, Kit kit, boolean bypassChecks) {
        if (!bypassChecks) {
            // Check if player can use this kit
            if (!kit.canUse(player)) {
                MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.no-permission"));
                return false;
            }
            
            // Check cooldown
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (!player.hasPermission("uniquekits.bypass.cooldown") && playerData.isKitOnCooldown(kit.getId())) {
                long remainingTime = playerData.getKitCooldownRemaining(kit.getId());
                String timeFormat = formatTime(remainingTime);
                MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.cooldown", "{time}", timeFormat));
                return false;
            }
            
            // Check one-time use
            if (kit.isOneTimeUse() && playerData.hasUsedKit(kit.getId())) {
                MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.already-used"));
                return false;
            }
            
            // Check cost
            if (kit.getCost() > 0) {
                if (plugin.getHookManager().getEconomy() != null) {
                    double balance = plugin.getHookManager().getEconomy().getBalance(player);
                    if (balance < kit.getCost()) {
                        MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.insufficient-funds", 
                            "{cost}", String.valueOf(kit.getCost()),
                            "{balance}", String.valueOf(balance)));
                        return false;
                    }
                    
                    // Withdraw money
                    plugin.getHookManager().getEconomy().withdrawPlayer(player, kit.getCost());
                }
            }
        }
        
        // Give items
        giveItems(player, kit.getItems());
        
        // Apply effects
        for (PotionEffect effect : kit.getEffects()) {
            player.addPotionEffect(effect);
        }
        
        // Execute commands
        for (String command : kit.getCommands()) {
            String processedCommand = command
                .replace("{player}", player.getName())
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("{world}", player.getWorld().getName());
            
            if (processedCommand.startsWith("[CONSOLE]")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand.substring(9));
            } else if (processedCommand.startsWith("[PLAYER]")) {
                player.performCommand(processedCommand.substring(8));
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            }
        }
        
        // Play sound
        if (!kit.getSound().isEmpty()) {
            try {
                Sound sound = Sound.valueOf(kit.getSound().toUpperCase());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound for kit " + kit.getId() + ": " + kit.getSound());
            }
        }
        
        // Update player data
        if (!bypassChecks) {
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            playerData.setKitCooldown(kit.getId(), System.currentTimeMillis() + kit.getCooldown());
            playerData.addKitUsage(kit.getId());
        }
        
        // Send success message
        MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.received", "{kit}", kit.getName()));
        
        return true;
    }
    
    private void giveItems(Player player, List<ItemStack> items) {
        List<ItemStack> overflow = new ArrayList<>();
        
        for (ItemStack item : items) {
            if (item == null) continue;
            
            HashMap<Integer, ItemStack> notAdded = player.getInventory().addItem(item.clone());
            overflow.addAll(notAdded.values());
        }
        
        // Handle overflow items
        if (!overflow.isEmpty()) {
            // Try to create a virtual chest or drop items
            if (plugin.getConfigManager().getConfig().getBoolean("settings.virtual-storage", true)) {
                // Store in virtual storage (implement this feature)
                MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.overflow-stored"));
            } else {
                // Drop items at player location
                for (ItemStack item : overflow) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
                MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("kit.overflow-dropped"));
            }
        }
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    public Kit getKit(String kitId) {
        return kits.get(kitId.toLowerCase());
    }
    
    public Collection<Kit> getAllKits() {
        return kits.values();
    }
    
    public List<Kit> getAvailableKits(Player player) {
        return kits.values().stream()
            .filter(kit -> kit.canUse(player))
            .sorted(Comparator.comparingInt(Kit::getPriority).reversed())
            .toList();
    }
    
    public List<Kit> getFirstJoinKits() {
        return kits.values().stream()
            .filter(Kit::isFirstJoinKit)
            .toList();
    }
    
    public List<Kit> getAutoJoinKits() {
        return kits.values().stream()
            .filter(Kit::isAutoGiveOnJoin)
            .toList();
    }
    
    public List<Kit> getAutoRespawnKits() {
        return kits.values().stream()
            .filter(Kit::isAutoGiveOnRespawn)
            .toList();
    }
    
    public void createKit(String kitId) {
        Kit kit = new Kit(kitId);
        kits.put(kitId.toLowerCase(), kit);
        saveKit(kit);
    }
    
    public boolean kitExists(String kitId) {
        return kits.containsKey(kitId.toLowerCase());
    }
    
    public Set<String> getKitNames() {
        return new HashSet<>(kits.keySet());
    }
}