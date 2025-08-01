package com.turjo.uniquekits.hooks;

import com.turjo.uniquekits.UniqueKits;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class HookManager {
    
    private final UniqueKits plugin;
    private Economy economy;
    private boolean vaultEnabled = false;
    private boolean placeholderAPIEnabled = false;
    private boolean mythicMobsEnabled = false;
    
    public HookManager(UniqueKits plugin) {
        this.plugin = plugin;
        setupHooks();
    }
    
    private void setupHooks() {
        setupVault();
        setupPlaceholderAPI();
        setupMythicMobs();
        
        plugin.getLogger().info("§a[HookManager] Hooks initialized successfully!");
    }
    
    private void setupVault() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.vault.enabled", true)) {
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
        plugin.getLogger().info("§a[HookManager] Vault economy hooked successfully!");
    }
    
    private void setupPlaceholderAPI() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.placeholderapi.enabled", true)) {
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            plugin.getLogger().info("§a[HookManager] PlaceholderAPI hooked successfully!");
        } else {
            plugin.getLogger().warning("§c[HookManager] PlaceholderAPI not found! Placeholder features disabled.");
        }
    }
    
    private void setupMythicMobs() {
        if (!plugin.getConfigManager().getConfig().getBoolean("hooks.mythicmobs.enabled", true)) {
            return;
        }
        
        if (plugin.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            mythicMobsEnabled = true;
            plugin.getLogger().info("§a[HookManager] MythicMobs hooked successfully!");
        } else {
            plugin.getLogger().info("§7[HookManager] MythicMobs not found, skipping hook.");
        }
    }
    
    public void reloadHooks() {
        vaultEnabled = false;
        placeholderAPIEnabled = false;
        mythicMobsEnabled = false;
        economy = null;
        
        setupHooks();
        plugin.getLogger().info("§a[HookManager] Hooks reloaded successfully!");
    }
    
    // Getters
    public Economy getEconomy() {
        return economy;
    }
    
    public boolean isVaultEnabled() {
        return vaultEnabled;
    }
    
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
    
    public boolean isMythicMobsEnabled() {
        return mythicMobsEnabled;
    }
}