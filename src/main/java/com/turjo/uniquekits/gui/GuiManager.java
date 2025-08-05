package com.turjo.uniquekits.gui;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.gui.editors.KitEditorGui;
import com.turjo.uniquekits.gui.menus.KitPreviewGui;
import com.turjo.uniquekits.gui.menus.KitSelectionGui;
import com.turjo.uniquekits.kits.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager implements Listener {
    
    private final UniqueKits plugin;
    private final Map<UUID, BaseGui> activeGuis = new HashMap<>();
    
    public GuiManager(UniqueKits plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[GuiManager] Initialized successfully!");
    }
    
    public void openKitSelection(Player player) {
        KitSelectionGui gui = new KitSelectionGui(plugin, player);
        gui.open();
        activeGuis.put(player.getUniqueId(), gui);
    }
    
    public void openKitPreview(Player player, Kit kit) {
        KitPreviewGui gui = new KitPreviewGui(plugin, player, kit);
        gui.open();
        activeGuis.put(player.getUniqueId(), gui);
    }
    
    public void openKitEditor(Player player, Kit kit) {
        KitEditorGui gui = new KitEditorGui(plugin, player, kit);
        gui.open();
        activeGuis.put(player.getUniqueId(), gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        BaseGui gui = activeGuis.get(player.getUniqueId());
        
        if (gui != null && gui.getInventory().equals(event.getInventory())) {
            gui.handleClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            BaseGui gui = activeGuis.get(player.getUniqueId());
            
            if (gui != null && gui.getInventory().equals(event.getInventory())) {
                gui.handleClose(event);
                activeGuis.remove(player.getUniqueId());
            }
        }
    }
    
    public void closeAllGuis() {
        for (UUID playerId : activeGuis.keySet()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                player.closeInventory();
            }
        }
        activeGuis.clear();
    }
}