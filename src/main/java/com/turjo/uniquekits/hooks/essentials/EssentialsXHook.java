package com.turjo.uniquekits.hooks.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Kit;
import com.turjo.uniquekits.UniqueKits;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EssentialsXHook {
    
    private final UniqueKits plugin;
    private final Essentials essentials;
    
    public EssentialsXHook(UniqueKits plugin) throws Exception {
        this.plugin = plugin;
        this.essentials = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        
        if (essentials == null) {
            throw new Exception("Essentials plugin not found!");
        }
        
        plugin.getLogger().info("§a[EssentialsXHook] Successfully hooked into EssentialsX!");
    }
    
    /**
     * Import all kits from EssentialsX
     */
    public int importAllKits() {
        Map<String, Kit> essentialsKits = essentials.getSettings().getKits();
        int imported = 0;
        
        plugin.getLogger().info("§e[EssentialsXHook] Found " + essentialsKits.size() + " EssentialsX kits to import...");
        
        for (Map.Entry<String, Kit> entry : essentialsKits.entrySet()) {
            String kitName = entry.getKey();
            Kit essentialsKit = entry.getValue();
            
            try {
                if (importKit(kitName, essentialsKit)) {
                    imported++;
                    plugin.getLogger().info("§a[EssentialsXHook] ✓ Imported kit: " + kitName);
                } else {
                    plugin.getLogger().warning("§c[EssentialsXHook] ✗ Failed to import kit: " + kitName);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("§c[EssentialsXHook] Error importing kit " + kitName + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("§a[EssentialsXHook] Import complete! Successfully imported " + imported + " kits.");
        return imported;
    }
    
    /**
     * Import a specific kit from EssentialsX
     */
    public boolean importKit(String kitName, Kit essentialsKit) {
        try {
            // Check if kit already exists in UniqueKits
            if (plugin.getKitManager().kitExists(kitName)) {
                plugin.getLogger().info("§7[EssentialsXHook] Kit '" + kitName + "' already exists, skipping...");
                return false;
            }
            
            // Create new UniqueKits kit
            com.turjo.uniquekits.kits.Kit uniqueKit = new com.turjo.uniquekits.kits.Kit(kitName);
            
            // Set basic properties
            uniqueKit.setName("§6§l" + kitName.substring(0, 1).toUpperCase() + kitName.substring(1) + " Kit");
            uniqueKit.setDescription("§7Imported from EssentialsX");
            
            // Convert items
            List<ItemStack> items = new ArrayList<>();
            try {
                List<String> kitItems = essentialsKit.getItems();
                for (String itemString : kitItems) {
                    ItemStack item = essentials.getItemDb().get(itemString);
                    if (item != null) {
                        items.add(item);
                    }
                }
                uniqueKit.setItems(items);
            } catch (Exception e) {
                plugin.getLogger().warning("§c[EssentialsXHook] Failed to convert items for kit " + kitName + ": " + e.getMessage());
                // Continue with empty items list
            }
            
            // Set cooldown (convert from seconds to milliseconds)
            long cooldown = essentialsKit.getDelay() * 1000L;
            uniqueKit.setCooldown(cooldown);
            
            // Set lore with import information
            List<String> lore = new ArrayList<>();
            lore.add("§7This kit was imported from EssentialsX");
            lore.add("§7Original cooldown: §e" + formatTime(cooldown));
            lore.add("");
            lore.add("§a§lClick to claim!");
            uniqueKit.setLore(lore);
            
            // Set default sound and effects
            uniqueKit.setSound("ENTITY_PLAYER_LEVELUP");
            uniqueKit.setEnabled(true);
            uniqueKit.setPriority(1); // Lower priority for imported kits
            
            // Save the kit
            plugin.getKitManager().addKit(uniqueKit);
            plugin.getKitManager().saveKit(uniqueKit);
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("§c[EssentialsXHook] Critical error importing kit " + kitName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get the number of available EssentialsX kits
     */
    public int getAvailableKitsCount() {
        return essentials.getSettings().getKits().size();
    }
    
    /**
     * Check if a specific kit exists in EssentialsX
     */
    public boolean hasKit(String kitName) {
        return essentials.getSettings().getKits().containsKey(kitName.toLowerCase());
    }
    
    /**
     * Get all EssentialsX kit names
     */
    public List<String> getKitNames() {
        return new ArrayList<>(essentials.getSettings().getKits().keySet());
    }
    
    /**
     * Format time duration
     */
    private String formatTime(long millis) {
        if (millis <= 0) return "No cooldown";
        
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }
    
    /**
     * Get EssentialsX version info
     */
    public String getEssentialsVersion() {
        return essentials.getDescription().getVersion();
    }
}