package com.turjo.uniquekits.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {
    
    private final UUID playerId;
    private final Map<String, Long> kitCooldowns = new ConcurrentHashMap<>();
    private final Map<String, Integer> kitUsages = new ConcurrentHashMap<>();
    private final Set<String> usedOneTimeKits = new HashSet<>();
    private final Map<String, Object> customData = new ConcurrentHashMap<>();
    private boolean firstJoin = true;
    private long lastLogin;
    private long totalPlayTime;
    private String lastKnownName;
    
    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.lastLogin = System.currentTimeMillis();
    }
    
    public static PlayerData fromConfig(UUID playerId, FileConfiguration config) {
        PlayerData data = new PlayerData(playerId);
        
        // Load kit cooldowns
        if (config.contains("kit-cooldowns")) {
            ConfigurationSection cooldownSection = config.getConfigurationSection("kit-cooldowns");
            if (cooldownSection != null) {
                for (String kitId : cooldownSection.getKeys(false)) {
                    data.kitCooldowns.put(kitId, cooldownSection.getLong(kitId));
                }
            }
        }
        
        // Load kit usages
        if (config.contains("kit-usages")) {
            ConfigurationSection usageSection = config.getConfigurationSection("kit-usages");
            if (usageSection != null) {
                for (String kitId : usageSection.getKeys(false)) {
                    data.kitUsages.put(kitId, usageSection.getInt(kitId));
                }
            }
        }
        
        // Load used one-time kits
        data.usedOneTimeKits.addAll(config.getStringList("used-one-time-kits"));
        
        // Load custom data
        if (config.contains("custom-data")) {
            ConfigurationSection customSection = config.getConfigurationSection("custom-data");
            if (customSection != null) {
                for (String key : customSection.getKeys(false)) {
                    data.customData.put(key, customSection.get(key));
                }
            }
        }
        
        // Load other data
        data.firstJoin = config.getBoolean("first-join", true);
        data.lastLogin = config.getLong("last-login", System.currentTimeMillis());
        data.totalPlayTime = config.getLong("total-play-time", 0);
        data.lastKnownName = config.getString("last-known-name", "");
        
        return data;
    }
    
    public void saveToConfig(FileConfiguration config) {
        // Save kit cooldowns
        if (!kitCooldowns.isEmpty()) {
            ConfigurationSection cooldownSection = config.createSection("kit-cooldowns");
            for (Map.Entry<String, Long> entry : kitCooldowns.entrySet()) {
                cooldownSection.set(entry.getKey(), entry.getValue());
            }
        }
        
        // Save kit usages
        if (!kitUsages.isEmpty()) {
            ConfigurationSection usageSection = config.createSection("kit-usages");
            for (Map.Entry<String, Integer> entry : kitUsages.entrySet()) {
                usageSection.set(entry.getKey(), entry.getValue());
            }
        }
        
        // Save used one-time kits
        if (!usedOneTimeKits.isEmpty()) {
            config.set("used-one-time-kits", new ArrayList<>(usedOneTimeKits));
        }
        
        // Save custom data
        if (!customData.isEmpty()) {
            ConfigurationSection customSection = config.createSection("custom-data");
            for (Map.Entry<String, Object> entry : customData.entrySet()) {
                customSection.set(entry.getKey(), entry.getValue());
            }
        }
        
        // Save other data
        config.set("first-join", firstJoin);
        config.set("last-login", lastLogin);
        config.set("total-play-time", totalPlayTime);
        config.set("last-known-name", lastKnownName);
    }
    
    // Kit cooldown methods
    public void setKitCooldown(String kitId, long expireTime) {
        if (expireTime <= System.currentTimeMillis()) {
            kitCooldowns.remove(kitId);
        } else {
            kitCooldowns.put(kitId, expireTime);
        }
    }
    
    public boolean isKitOnCooldown(String kitId) {
        Long expireTime = kitCooldowns.get(kitId);
        if (expireTime == null) {
            return false;
        }
        
        if (expireTime <= System.currentTimeMillis()) {
            kitCooldowns.remove(kitId);
            return false;
        }
        
        return true;
    }
    
    public long getKitCooldownExpireTime(String kitId) {
        return kitCooldowns.getOrDefault(kitId, 0L);
    }
    
    public long getKitCooldownRemaining(String kitId) {
        long expireTime = getKitCooldownExpireTime(kitId);
        if (expireTime <= System.currentTimeMillis()) {
            return 0;
        }
        return expireTime - System.currentTimeMillis();
    }
    
    public void clearKitCooldown(String kitId) {
        kitCooldowns.remove(kitId);
    }
    
    public void clearAllCooldowns() {
        kitCooldowns.clear();
    }
    
    // Kit usage methods
    public void addKitUsage(String kitId) {
        kitUsages.put(kitId, kitUsages.getOrDefault(kitId, 0) + 1);
    }
    
    public int getKitUsageCount(String kitId) {
        return kitUsages.getOrDefault(kitId, 0);
    }
    
    public void setKitUsageCount(String kitId, int count) {
        if (count <= 0) {
            kitUsages.remove(kitId);
        } else {
            kitUsages.put(kitId, count);
        }
    }
    
    public Map<String, Integer> getAllKitUsages() {
        return new HashMap<>(kitUsages);
    }
    
    // One-time kit methods
    public void markKitAsUsed(String kitId) {
        usedOneTimeKits.add(kitId);
    }
    
    public boolean hasUsedKit(String kitId) {
        return usedOneTimeKits.contains(kitId);
    }
    
    public void resetKitUsage(String kitId) {
        usedOneTimeKits.remove(kitId);
    }
    
    public Set<String> getUsedOneTimeKits() {
        return new HashSet<>(usedOneTimeKits);
    }
    
    // Custom data methods
    public void setCustomData(String key, Object value) {
        if (value == null) {
            customData.remove(key);
        } else {
            customData.put(key, value);
        }
    }
    
    public Object getCustomData(String key) {
        return customData.get(key);
    }
    
    public <T> T getCustomData(String key, Class<T> type, T defaultValue) {
        Object value = customData.get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return defaultValue;
    }
    
    public boolean hasCustomData(String key) {
        return customData.containsKey(key);
    }
    
    public void removeCustomData(String key) {
        customData.remove(key);
    }
    
    // Getters and setters
    public UUID getPlayerId() {
        return playerId;
    }
    
    public boolean isFirstJoin() {
        return firstJoin;
    }
    
    public void setFirstJoin(boolean firstJoin) {
        this.firstJoin = firstJoin;
    }
    
    public long getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public long getTotalPlayTime() {
        return totalPlayTime;
    }
    
    public void setTotalPlayTime(long totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }
    
    public void addPlayTime(long time) {
        this.totalPlayTime += time;
    }
    
    public String getLastKnownName() {
        return lastKnownName;
    }
    
    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }
    
    // Statistics methods
    public int getTotalKitsUsed() {
        return kitUsages.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public String getMostUsedKit() {
        return kitUsages.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    public long getActiveCooldowns() {
        long currentTime = System.currentTimeMillis();
        return kitCooldowns.values().stream()
            .filter(time -> time > currentTime)
            .count();
    }
    
    public void cleanup() {
        // Remove expired cooldowns
        long currentTime = System.currentTimeMillis();
        kitCooldowns.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
        
        // Remove zero usage counts
        kitUsages.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }
}