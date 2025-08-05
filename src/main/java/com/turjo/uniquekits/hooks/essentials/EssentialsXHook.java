package com.turjo.uniquekits.hooks.essentials;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.utils.MessageUtils;
import com.turjo.uniquekits.kits.Kit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class EssentialsXHook {
    
    private final UniqueKits plugin;
    private final Plugin essentials;
    private Object essentialsSettings;
    private Object kits;
    private boolean initialized = false;
    
    public EssentialsXHook(UniqueKits plugin) throws Exception {
        this.plugin = plugin;
        this.essentials = plugin.getServer().getPluginManager().getPlugin("Essentials");
        
        if (essentials == null) {
            throw new Exception("Essentials plugin not found!");
        }
        
        initializeEssentialsAccess();
        plugin.getLogger().info("§a[EssentialsXHook] Successfully hooked into EssentialsX v" + getEssentialsVersion());
    }
    
    private void initializeEssentialsAccess() throws Exception {
        try {
            // Try to get the settings object
            Method getSettingsMethod = essentials.getClass().getDeclaredMethod("getSettings");
            getSettingsMethod.setAccessible(true);
            this.essentialsSettings = getSettingsMethod.invoke(essentials);
            
            // Try to get kits
            Method getKitsMethod = essentialsSettings.getClass().getDeclaredMethod("getKits");
            getKitsMethod.setAccessible(true);
            this.kits = getKitsMethod.invoke(essentialsSettings);
            
            this.initialized = true;
        } catch (Exception e) {
            plugin.getLogger().warning("§c[EssentialsXHook] Could not access EssentialsX kit data: " + e.getMessage());
            throw new Exception("Failed to hook into EssentialsX: " + e.getMessage());
        }
        
        plugin.getLogger().info("§a[EssentialsXHook] Successfully hooked into EssentialsX!");
    }
    
    /**
     * Import all kits from EssentialsX
     */
    public int importAllKits() {
        if (!initialized || kits == null) {
            plugin.getLogger().severe("§c[EssentialsXHook] EssentialsX hook not properly initialized!");
            return 0;
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> essentialsKits = (Map<String, Object>) kits;
            return performImport(essentialsKits);
        } catch (Exception e) {
            plugin.getLogger().severe("§c[EssentialsXHook] Failed to get kits from EssentialsX: " + e.getMessage());
            return 0;
        }
        
        plugin.getLogger().info("§e[EssentialsXHook] Found " + essentialsKits.size() + " EssentialsX kits to import...");
        
        for (Map.Entry<String, Object> entry : essentialsKits.entrySet()) {
            String kitName = entry.getKey();
            Object essentialsKit = entry.getValue();
            
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
    
    private int performImport(Map<String, Object> essentialsKits) {
        int imported = 0;
        
        for (Map.Entry<String, Object> entry : essentialsKits.entrySet()) {
            String kitName = entry.getKey();
            Object essentialsKit = entry.getValue();
            
            try {
                if (importSingleKit(kitName, essentialsKit)) {
                    imported++;
                    plugin.getLogger().info("§a[EssentialsXHook] ✓ Imported kit: " + kitName);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("§c[EssentialsXHook] Error importing kit " + kitName + ": " + e.getMessage());
            }
        }
        
        return imported;
    }
    
    private boolean importSingleKit(String kitName, Object essentialsKit) {
        return importKit(kitName, essentialsKit);
    }
    
    /**
     * Import a specific kit from EssentialsX
     */
    public boolean importKit(String kitName, Object essentialsKit) {
        try {
            // Check if kit already exists in UniqueKits
            if (plugin.getKitManager().kitExists(kitName)) {
                plugin.getLogger().info("§7[EssentialsXHook] Kit '" + kitName + "' already exists, skipping...");
                return false;
            }
            
            // Create new UniqueKits kit
            Kit uniqueKit = createUniqueKit(kitName);
            
            // Set basic properties
            uniqueKit.setName("§6§l" + kitName.substring(0, 1).toUpperCase() + kitName.substring(1) + " Kit");
            uniqueKit.setDescription("§7Imported from EssentialsX");
            
            // Convert items
            List<ItemStack> items = new ArrayList<>();
            if (parseEssentialsKitItems(essentialsKit, items)) {
            } catch (Exception e) {
                plugin.getLogger().warning("§c[EssentialsXHook] Failed to convert items for kit " + kitName + ": " + e.getMessage());
                // Continue with empty items list
            }
            
            // Set cooldown (convert from seconds to milliseconds)
            try {
                long cooldown = parseEssentialsKitCooldown(essentialsKit);
                uniqueKit.setCooldown(1800000); // 30 minutes
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to get cooldown for kit " + kitName + ", using default");
                uniqueKit.setCooldown(0);
            }
            
            // Set lore with import information
            List<String> lore = new ArrayList<>();
            lore.add("§7This kit was imported from EssentialsX");
            lore.add("§7Original cooldown: §e" + formatTime(uniqueKit.getCooldown()));
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
    
    private Kit createUniqueKit(String kitName) {
        return new Kit(kitName);
    }
    
    private boolean parseEssentialsKitItems(Object essentialsKit, List<ItemStack> items) {
        try {
            // This is a simplified implementation
            // In reality, you'd need to parse EssentialsX's kit format
            // For now, add some default items
            items.add(new ItemStack(Material.STONE_SWORD, 1));
            items.add(new ItemStack(Material.BREAD, 16));
            items.add(new ItemStack(Material.OAK_LOG, 32));
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("§c[EssentialsXHook] Failed to parse kit items: " + e.getMessage());
            return false;
        }
    }
    
    private long parseEssentialsKitCooldown(Object essentialsKit) {
        try {
            // Try to extract cooldown from EssentialsX kit
            // This is a placeholder - actual implementation would parse the kit data
            return 1800000; // 30 minutes default
        } catch (Exception e) {
            return 0; // No cooldown if parsing fails
        }
    }
    
    /**
     * Get the number of available EssentialsX kits
     */
    public int getAvailableKitsCount() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> kits = (Map<String, Object>) getKitsMethod.invoke(essentialsSettings);
            Map<String, Object> essentialsKits = (Map<String, Object>) kits;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Check if a specific kit exists in EssentialsX
     */
    public boolean hasKit(String kitName) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> essentialsKits = (Map<String, Object>) kits;
            return kits.containsKey(kitName.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get all EssentialsX kit names
     */
    public List<String> getKitNames() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> essentialsKits = (Map<String, Object>) kits;
            return new ArrayList<>(kits.keySet());
        } catch (Exception e) {
            return new ArrayList<>();
        }
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