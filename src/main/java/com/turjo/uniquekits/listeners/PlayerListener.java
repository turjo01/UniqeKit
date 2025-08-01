package com.turjo.uniquekits.listeners;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.kits.Kit;
import com.turjo.uniquekits.storage.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;

public class PlayerListener implements Listener {
    
    private final UniqueKits plugin;
    
    public PlayerListener(UniqueKits plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        // Update player data
        playerData.setLastLogin(System.currentTimeMillis());
        playerData.setLastKnownName(player.getName());
        
        // Handle first join
        if (playerData.isFirstJoin()) {
            handleFirstJoin(player, playerData);
        }
        
        // Handle auto-give on join kits
        if (plugin.getConfigManager().getConfig().getBoolean("auto-give.on-join.enabled", true)) {
            int delay = plugin.getConfigManager().getConfig().getInt("auto-give.on-join.delay", 1);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                List<Kit> autoJoinKits = plugin.getKitManager().getAutoJoinKits();
                for (Kit kit : autoJoinKits) {
                    if (kit.canUse(player)) {
                        plugin.getKitManager().giveKit(player, kit, false);
                    }
                }
            }, delay * 20L);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        // Update play time
        long sessionTime = System.currentTimeMillis() - playerData.getLastLogin();
        playerData.addPlayTime(sessionTime);
        
        // Clean up expired data
        playerData.cleanup();
        
        // Save and unload player data
        plugin.getPlayerDataManager().unloadPlayerData(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Handle auto-give on respawn kits
        if (plugin.getConfigManager().getConfig().getBoolean("auto-give.on-respawn.enabled", true)) {
            int delay = plugin.getConfigManager().getConfig().getInt("auto-give.on-respawn.delay", 0);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                List<Kit> autoRespawnKits = plugin.getKitManager().getAutoRespawnKits();
                for (Kit kit : autoRespawnKits) {
                    if (kit.canUse(player)) {
                        plugin.getKitManager().giveKit(player, kit, false);
                    }
                }
            }, delay * 20L);
        }
    }
    
    private void handleFirstJoin(Player player, PlayerData playerData) {
        if (!plugin.getConfigManager().getConfig().getBoolean("first-join.enabled", true)) {
            return;
        }
        
        // Mark as no longer first join
        playerData.setFirstJoin(false);
        
        // Send welcome message
        if (plugin.getConfigManager().getConfig().getBoolean("first-join.welcome-message", true)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage(plugin.getLanguageManager().getMessage("welcome.first-join"));
                
                // Send title if configured
                String title = plugin.getLanguageManager().getMessage("welcome.title");
                String subtitle = plugin.getLanguageManager().getMessage("welcome.subtitle");
                
                if (!title.equals("welcome.title") && !subtitle.equals("welcome.subtitle")) {
                    player.sendTitle(title, subtitle, 10, 70, 20);
                }
            }, 20L);
        }
        
        // Give first join kits
        int delay = plugin.getConfigManager().getConfig().getInt("first-join.delay", 3);
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            List<Kit> firstJoinKits = plugin.getKitManager().getFirstJoinKits();
            for (Kit kit : firstJoinKits) {
                if (kit.canUse(player)) {
                    plugin.getKitManager().giveKit(player, kit, false);
                }
            }
        }, delay * 20L);
    }
}