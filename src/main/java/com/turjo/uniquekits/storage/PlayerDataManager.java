package com.turjo.uniquekits.storage;

import com.turjo.uniquekits.UniqueKits;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    
    private final UniqueKits plugin;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private final File playerDataFolder;
    
    public PlayerDataManager(UniqueKits plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
        
        plugin.getLogger().info("§a[PlayerDataManager] Initialized successfully!");
    }
    
    public PlayerData getPlayerData(UUID playerId) {
        // Check cache first
        PlayerData data = playerDataCache.get(playerId);
        if (data != null) {
            return data;
        }
        
        // Load from file
        data = loadPlayerData(playerId);
        playerDataCache.put(playerId, data);
        
        return data;
    }
    
    private PlayerData loadPlayerData(UUID playerId) {
        File playerFile = new File(playerDataFolder, playerId.toString() + ".yml");
        
        if (!playerFile.exists()) {
            return new PlayerData(playerId);
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            return PlayerData.fromConfig(playerId, config);
        } catch (Exception e) {
            plugin.getLogger().warning("§c[PlayerDataManager] Failed to load data for " + playerId + ": " + e.getMessage());
            return new PlayerData(playerId);
        }
    }
    
    public void savePlayerData(UUID playerId) {
        PlayerData data = playerDataCache.get(playerId);
        if (data == null) {
            return;
        }
        
        File playerFile = new File(playerDataFolder, playerId.toString() + ".yml");
        
        try {
            FileConfiguration config = new YamlConfiguration();
            data.saveToConfig(config);
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().warning("§c[PlayerDataManager] Failed to save data for " + playerId + ": " + e.getMessage());
        }
    }
    
    public void saveAllPlayerData() {
        plugin.getLogger().info("§a[PlayerDataManager] Saving all player data...");
        
        for (UUID playerId : playerDataCache.keySet()) {
            savePlayerData(playerId);
        }
        
        plugin.getLogger().info("§a[PlayerDataManager] Saved data for " + playerDataCache.size() + " players");
    }
    
    public void unloadPlayerData(UUID playerId) {
        savePlayerData(playerId);
        playerDataCache.remove(playerId);
    }
    
    public void clearCache() {
        saveAllPlayerData();
        playerDataCache.clear();
        plugin.getLogger().info("§a[PlayerDataManager] Cache cleared!");
    }
    
    // Auto-save task
    public void startAutoSaveTask() {
        int interval = plugin.getConfigManager().getConfig().getInt("performance.auto-save-interval", 5) * 60 * 20; // Convert minutes to ticks
        
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            saveAllPlayerData();
        }, interval, interval);
        
        plugin.getLogger().info("§a[PlayerDataManager] Auto-save task started with interval: " + (interval / 20 / 60) + " minutes");
    }
}