package com.turjo.uniquekits.hooks;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.hooks.essentials.EssentialsXHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class HookManager {
    
    private final UniqueKits plugin;
    private Economy economy;
    private EssentialsXHook essentialsXHook;
    
    // Hook status flags
    private boolean vaultEnabled = false;
    private boolean placeholderAPIEnabled = false;
    private boolean mythicMobsEnabled = false;
    private boolean itemsAdderEnabled = false;
    private boolean mmoItemsEnabled = false;
    private boolean worldGuardEnabled = false;
    private boolean essentialsXEnabled = false;
    
    public HookManager(UniqueKits plugin) {
        this.plugin = plugin;
        setupHooks();
    }
    
    private void setupHooks() {
        plugin.getLogger().info("§e[HookManager] Initializing plugin hooks...");
        
        setupVault();
        setupPlaceholderAPI();
        setupMythicMobs();
        setupItemsAdder();
        setupMMOItems();
        setupWorldGuard();
        setupEssentialsX();
        
        plugin.getLogger().info("§a[HookManager] All hooks initialized successfully!");
        logHookStatus();
    }
    
    private void setupVault() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.vault.enabled", true)) {
            plugin.getLogger().info("§7[HookManager] Vault hook disabled in config");
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("§c[HookManager] Vault not found! Economy features disabled.");
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("§c[HookManager] No economy provider found! Economy features disabled.");
            return;
        }
        
        economy = rsp.getProvider();
        vaultEnabled = true;
        plugin.getLogger().info("§a[HookManager] ✓ Vault economy hooked successfully! Provider: " + economy.getName());
    }
    
    private void setupPlaceholderAPI() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.placeholderapi.enabled", true)) {
            plugin.getLogger().info("§7[HookManager] PlaceholderAPI hook disabled in config");
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            plugin.getLogger().info("§a[HookManager] ✓ PlaceholderAPI hooked successfully!");
        } else {
            plugin.getLogger().info("§7[HookManager] PlaceholderAPI not found - placeholder features limited");
        }
    }
    
    private void setupMythicMobs() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.mythicmobs.enabled", true)) {
            plugin.getLogger().info("§7[HookManager] MythicMobs hook disabled in config");
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            mythicMobsEnabled = true;
            plugin.getLogger().info("§a[HookManager] ✓ MythicMobs hooked successfully!");
        } else {
            plugin.getLogger().info("§7[HookManager] MythicMobs not found - custom mob items unavailable");
        }
    }
    
    private void setupItemsAdder() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.itemsadder.enabled", true)) {
            plugin.getLogger().info("§7[HookManager] ItemsAdder hook disabled in config");
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            itemsAdderEnabled = true;
            plugin.getLogger().info("§a[HookManager] ✓ ItemsAdder hooked successfully!");
        } else {
            plugin.getLogger().info("§7[HookManager] ItemsAdder not found - custom items unavailable");
        }
    }
    
    private void setupMMOItems() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.mmoitems.enabled", true)) {
            plugin.getLogger().info("§7[HookManager] MMOItems hook disabled in config");
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("MMOItems") != null) {
            mmoItemsEnabled = true;
            plugin.getLogger().info("§a[HookManager] ✓ MMOItems hooked successfully!");
        } else {
            plugin.getLogger().info("§7[HookManager] MMOItems not found - RPG items unavailable");
        }
    }
    
    private void setupWorldGuard() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.worldguard.enabled", true)) {
            plugin.getLogger().info("§7[HookManager] WorldGuard hook disabled in config");
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardEnabled = true;
            plugin.getLogger().info("§a[HookManager] ✓ WorldGuard hooked successfully!");
        } else {
            plugin.getLogger().info("§7[HookManager] WorldGuard not found - region restrictions unavailable");
        }
    }
    
    private void setupEssentialsX() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.essentialsx.enabled", true)) {
            plugin.getLogger().info("§7[HookManager] EssentialsX hook disabled in config");
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("Essentials") != null) {
            try {
                essentialsXHook = new EssentialsXHook(plugin);
                essentialsXEnabled = true;
                plugin.getLogger().info("§a[HookManager] ✓ EssentialsX hooked successfully! Kit import available.");
            } catch (Exception e) {
                plugin.getLogger().warning("§c[HookManager] Failed to hook EssentialsX: " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("§7[HookManager] EssentialsX not found - kit import unavailable");
        }
    }
    
    private void logHookStatus() {
        plugin.getLogger().info("§e[HookManager] Hook Status Summary:");
        plugin.getLogger().info("§7├─ Vault: " + (vaultEnabled ? "§a✓ Enabled" : "§c✗ Disabled"));
        plugin.getLogger().info("§7├─ PlaceholderAPI: " + (placeholderAPIEnabled ? "§a✓ Enabled" : "§c✗ Disabled"));
        plugin.getLogger().info("§7├─ MythicMobs: " + (mythicMobsEnabled ? "§a✓ Enabled" : "§c✗ Disabled"));
        plugin.getLogger().info("§7├─ ItemsAdder: " + (itemsAdderEnabled ? "§a✓ Enabled" : "§c✗ Disabled"));
        plugin.getLogger().info("§7├─ MMOItems: " + (mmoItemsEnabled ? "§a✓ Enabled" : "§c✗ Disabled"));
        plugin.getLogger().info("§7├─ WorldGuard: " + (worldGuardEnabled ? "§a✓ Enabled" : "§c✗ Disabled"));
        plugin.getLogger().info("§7└─ EssentialsX: " + (essentialsXEnabled ? "§a✓ Enabled" : "§c✗ Disabled"));
    }
    
    public void reloadHooks() {
        plugin.getLogger().info("§e[HookManager] Reloading all hooks...");
        
        // Reset all hooks
        vaultEnabled = false;
        placeholderAPIEnabled = false;
        mythicMobsEnabled = false;
        itemsAdderEnabled = false;
        mmoItemsEnabled = false;
        worldGuardEnabled = false;
        essentialsXEnabled = false;
        economy = null;
        essentialsXHook = null;
        
        // Reinitialize
        setupHooks();
        plugin.getLogger().info("§a[HookManager] All hooks reloaded successfully!");
    }
    
    // Getters for hook status
    public Economy getEconomy() { return economy; }
    public EssentialsXHook getEssentialsXHook() { return essentialsXHook; }
    
    public boolean isVaultEnabled() { return vaultEnabled; }
    public boolean isPlaceholderAPIEnabled() { return placeholderAPIEnabled; }
    public boolean isMythicMobsEnabled() { return mythicMobsEnabled; }
    public boolean isItemsAdderEnabled() { return itemsAdderEnabled; }
    public boolean isMMOItemsEnabled() { return mmoItemsEnabled; }
    public boolean isWorldGuardEnabled() { return worldGuardEnabled; }
    public boolean isEssentialsXEnabled() { return essentialsXEnabled; }
    
    // Utility methods
    public int getEnabledHooksCount() {
        int count = 0;
        if (vaultEnabled) count++;
        if (placeholderAPIEnabled) count++;
        if (mythicMobsEnabled) count++;
        if (itemsAdderEnabled) count++;
        if (mmoItemsEnabled) count++;
        if (worldGuardEnabled) count++;
        if (essentialsXEnabled) count++;
        return count;
    }
    
    public boolean hasEconomySupport() {
        return vaultEnabled && economy != null;
    }
    
    public boolean hasCustomItemSupport() {
        return mythicMobsEnabled || itemsAdderEnabled || mmoItemsEnabled;
    }
}