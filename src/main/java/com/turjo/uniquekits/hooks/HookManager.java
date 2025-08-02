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
    private boolean essentialsXEnabled = false;
    
    public HookManager(UniqueKits plugin) {
        this.plugin = plugin;
        setupHooks();
    }
    
    private void setupHooks() {
        plugin.getLogger().info("§e[HookManager] Initializing plugin hooks...");
        
        setupVault();
        setupPlaceholderAPI();
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
        plugin.getLogger().info("§7└─ EssentialsX: " + (essentialsXEnabled ? "§a✓ Enabled" : "§c✗ Disabled"));
    }
    
    public void reloadHooks() {
        plugin.getLogger().info("§e[HookManager] Reloading all hooks...");
        
        // Reset all hooks
        vaultEnabled = false;
        placeholderAPIEnabled = false;
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
    public boolean isEssentialsXEnabled() { return essentialsXEnabled; }
    
    // Utility methods
    public int getEnabledHooksCount() {
        int count = 0;
        if (vaultEnabled) count++;
        if (placeholderAPIEnabled) count++;
        if (essentialsXEnabled) count++;
        return count;
    }
    
    public boolean hasEconomySupport() {
        return vaultEnabled && economy != null;
    }
    
}