package com.turjo.uniquekits.gui;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public abstract class BaseGui {
    
    protected final UniqueKits plugin;
    protected final Player player;
    protected Inventory inventory;
    
    public BaseGui(UniqueKits plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    public abstract void create();
    public abstract void handleClick(InventoryClickEvent event);
    public abstract void handleClose(InventoryCloseEvent event);
    
    public void open() {
        create();
        player.openInventory(inventory);
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    protected Inventory createInventory(int size, String title) {
        return Bukkit.createInventory(null, size, MessageUtils.colorize(title));
    }
    
    protected void playSound(String sound) {
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(sound), 1.0f, 1.0f);
        } catch (Exception e) {
            // Fallback sound
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }
}