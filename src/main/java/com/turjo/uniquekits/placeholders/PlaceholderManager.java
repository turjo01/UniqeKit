package com.turjo.uniquekits.placeholders;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.storage.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderManager extends PlaceholderExpansion {
    
    private final UniqueKits plugin;
    
    public PlaceholderManager(UniqueKits plugin) {
        this.plugin = plugin;
        
        if (plugin.getHookManager().isPlaceholderAPIEnabled()) {
            register();
            plugin.getLogger().info("§a[PlaceholderManager] PlaceholderAPI expansion registered!");
        }
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "uniquekits";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        switch (params.toLowerCase()) {
            case "total_kits":
                return String.valueOf(plugin.getKitManager().getAllKits().size());
                
            case "available_kits":
                if (player.isOnline()) {
                    return String.valueOf(plugin.getKitManager().getAvailableKits(player.getPlayer()).size());
                }
                return "0";
                
            case "used_kits":
                return String.valueOf(playerData.getTotalKitsUsed());
                
            case "active_cooldowns":
                return String.valueOf(playerData.getActiveCooldowns());
                
            case "most_used_kit":
                String mostUsed = playerData.getMostUsedKit();
                return mostUsed != null ? mostUsed : "None";
                
            case "first_join":
                return playerData.isFirstJoin() ? "Yes" : "No";
                
            case "total_playtime":
                return formatTime(playerData.getTotalPlayTime());
                
            default:
                // Check for kit-specific placeholders
                if (params.startsWith("kit_cooldown_")) {
                    String kitId = params.substring("kit_cooldown_".length());
                    if (playerData.isKitOnCooldown(kitId)) {
                        return formatTime(playerData.getKitCooldownRemaining(kitId));
                    }
                    return "Ready";
                }
                
                if (params.startsWith("kit_usage_")) {
                    String kitId = params.substring("kit_usage_".length());
                    return String.valueOf(playerData.getKitUsageCount(kitId));
                }
                
                if (params.startsWith("kit_used_")) {
                    String kitId = params.substring("kit_used_".length());
                    return playerData.hasUsedKit(kitId) ? "Yes" : "No";
                }
                
                break;
        }
        
        return null;
    }
    
    private String formatTime(long millis) {
        if (millis <= 0) return "0s";
        
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}