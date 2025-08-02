package com.turjo.uniquekits.gui;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.kits.Kit;
import com.turjo.uniquekits.utils.ItemBuilder;
import com.turjo.uniquekits.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiManager implements Listener {
    
    private final UniqueKits plugin;
    private final Map<UUID, GuiSession> sessions = new HashMap<>();
    
    public GuiManager(UniqueKits plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("§a[GuiManager] Initialized successfully!");
    }
    
    public void openKitSelection(Player player) {
        List<Kit> availableKits = plugin.getKitManager().getAvailableKits(player);
        
        String title = plugin.getConfigManager().getGuiConfig().getString("main-gui.title", "&6&lUniqueKits");
        title = MessageUtils.colorize(title);
        
        int size = plugin.getConfigManager().getGuiConfig().getInt("main-gui.size", 54);
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Add kits to GUI
        int slot = 0;
        for (Kit kit : availableKits) {
            if (slot >= 45) break; // Leave space for navigation
            gui.setItem(slot, kit.getIcon());
            slot++;
        }
        
        // Add navigation items
        addNavigationItems(gui);
        
        // Create session
        GuiSession session = new GuiSession(GuiType.KIT_SELECTION, gui, availableKits);
        sessions.put(player.getUniqueId(), session);
        
        player.openInventory(gui);
    }
    
    public void openKitPreview(Player player, Kit kit) {
        String title = plugin.getConfigManager().getGuiConfig().getString("preview-gui.title", "&6&lKit Preview");
        title = MessageUtils.colorize(title.replace("{kit_name}", kit.getName()));
        
        int size = plugin.getConfigManager().getGuiConfig().getInt("preview-gui.size", 54);
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Add kit items
        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < items.size() && i < 45; i++) {
            gui.setItem(i, items.get(i));
        }
        
        // Add claim button
        ItemStack claimButton = new ItemBuilder(Material.LIME_CONCRETE)
            .name("§a§lClaim Kit")
            .lore("§7Click to claim this kit")
            .build();
        gui.setItem(49, claimButton);
        
        // Add back button
        ItemStack backButton = new ItemBuilder(Material.ARROW)
            .name("§a§lBack")
            .lore("§7Return to kit selection")
            .build();
        gui.setItem(45, backButton);
        
        // Create session
        GuiSession session = new GuiSession(GuiType.KIT_PREVIEW, gui, kit);
        sessions.put(player.getUniqueId(), session);
        
        player.openInventory(gui);
    }
    
    public void openKitEditor(Player player, Kit kit) {
        String title = plugin.getConfigManager().getGuiConfig().getString("editor-gui.title", "&6&lKit Editor");
        title = MessageUtils.colorize(title.replace("{kit_name}", kit.getName()));
        
        int size = plugin.getConfigManager().getGuiConfig().getInt("editor-gui.size", 54);
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Add kit items for editing
        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < items.size() && i < 45; i++) {
            gui.setItem(i, items.get(i));
        }
        
        // Add editor buttons
        addEditorButtons(gui);
        
        // Create session
        GuiSession session = new GuiSession(GuiType.KIT_EDITOR, gui, kit);
        sessions.put(player.getUniqueId(), session);
        
        player.openInventory(gui);
    }
    
    private void addNavigationItems(Inventory gui) {
        // Close button
        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
            .name("§c§lClose")
            .lore("§7Click to close this menu")
            .build();
        gui.setItem(49, closeButton);
        
        // Info item
        ItemStack infoItem = new ItemBuilder(Material.BOOK)
            .name("§e§lKit Information")
            .lore("§7Left-click a kit to preview", "§7Right-click a kit to claim")
            .build();
        gui.setItem(4, infoItem);
    }
    
    private void addEditorButtons(Inventory gui) {
        // Save button
        ItemStack saveButton = new ItemBuilder(Material.GREEN_CONCRETE)
            .name("§a§lSave Kit")
            .lore("§7Click to save all changes")
            .build();
        gui.setItem(45, saveButton);
        
        // Cancel button
        ItemStack cancelButton = new ItemBuilder(Material.RED_CONCRETE)
            .name("§c§lCancel")
            .lore("§7Click to cancel editing")
            .build();
        gui.setItem(46, cancelButton);
        
        // Settings button
        ItemStack settingsButton = new ItemBuilder(Material.ANVIL)
            .name("§e§lKit Settings")
            .lore("§7Click to edit kit settings")
            .build();
        gui.setItem(49, settingsButton);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        GuiSession session = sessions.get(player.getUniqueId());
        
        if (session == null || !event.getInventory().equals(session.getInventory())) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        switch (session.getType()) {
            case KIT_SELECTION:
                handleKitSelectionClick(player, session, slot, event.isRightClick());
                break;
            case KIT_PREVIEW:
                handleKitPreviewClick(player, session, slot);
                break;
            case KIT_EDITOR:
                handleKitEditorClick(player, session, slot);
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            GuiSession session = sessions.get(player.getUniqueId());
            
            if (session != null && event.getInventory().equals(session.getInventory())) {
                // Handle editor saving if needed
                if (session.getType() == GuiType.KIT_EDITOR) {
                    saveKitFromEditor(player, session);
                }
                
                sessions.remove(player.getUniqueId());
            }
        }
    }
    
    private void handleKitSelectionClick(Player player, GuiSession session, int slot, boolean rightClick) {
        Object data = session.getData();
        if (!(data instanceof List)) {
            return;
        }
        
        @SuppressWarnings("unchecked")
        List<Kit> kits = (List<Kit>) data;
        
        if (slot == 49) { // Close button
            player.closeInventory();
            return;
        }
        
        if (slot < kits.size()) {
            Kit kit = kits.get(slot);
            
            if (rightClick) {
                // Claim kit
                plugin.getKitManager().giveKit(player, kit, false);
                player.closeInventory();
            } else {
                // Preview kit
                openKitPreview(player, kit);
            }
        }
    }
    
    private void handleKitPreviewClick(Player player, GuiSession session, int slot) {
        Object data = session.getData();
        if (!(data instanceof Kit)) {
            return;
        }
        Kit kit = (Kit) data;
        
        if (slot == 49) { // Claim button
            plugin.getKitManager().giveKit(player, kit, false);
            player.closeInventory();
        } else if (slot == 45) { // Back button
            openKitSelection(player);
        }
    }
    
    private void handleKitEditorClick(Player player, GuiSession session, int slot) {
        Object data = session.getData();
        if (!(data instanceof Kit)) {
            return;
        }
        
        if (slot == 45) { // Save button
            saveKitFromEditor(player, session);
            MessageUtils.sendMessage(player, "§aKit saved successfully!");
            player.closeInventory();
        } else if (slot == 46) { // Cancel button
            player.closeInventory();
        } else if (slot == 49) { // Settings button
            MessageUtils.sendMessage(player, "§eKit settings editor not yet implemented!");
        }
    }
    
    private void saveKitFromEditor(Player player, GuiSession session) {
        Object data = session.getData();
        if (!(data instanceof Kit)) {
            return;
        }
        Kit kit = (Kit) data;
        Inventory inventory = session.getInventory();
        
        // Collect items from inventory (slots 0-44)
        kit.getItems().clear();
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                kit.getItems().add(item.clone());
            }
        }
        
        // Save kit to file
        plugin.getKitManager().saveKit(kit);
    }
    
    public void closeAllGuis() {
        for (UUID playerId : sessions.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.closeInventory();
            }
        }
        sessions.clear();
    }
    
    // Inner classes
    private static class GuiSession {
        private final GuiType type;
        private final Inventory inventory;
        private final Object data;
        
        public GuiSession(GuiType type, Inventory inventory, Object data) {
            this.type = type;
            this.inventory = inventory;
            this.data = data;
        }
        
        public GuiType getType() { return type; }
        public Inventory getInventory() { return inventory; }
        public Object getData() { return data; }
    }
    
    private enum GuiType {
        KIT_SELECTION,
        KIT_PREVIEW,
        KIT_EDITOR
    }
}