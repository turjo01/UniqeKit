package com.turjo.uniquekits.gui.editors;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.gui.BaseGui;
import com.turjo.uniquekits.kits.Kit;
import com.turjo.uniquekits.utils.ItemBuilder;
import com.turjo.uniquekits.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KitEditorGui extends BaseGui {
    
    private final Kit kit;
    private boolean hasChanges = false;
    
    public KitEditorGui(UniqueKits plugin, Player player, Kit kit) {
        super(plugin, player);
        this.kit = kit;
    }
    
    @Override
    public void create() {
        String title = plugin.getLanguageManager().getMessage("gui.editor-title", "{kit}", kit.getName());
        this.inventory = createInventory(54, title);
        
        // Add kit items for editing (slots 0-44)
        addKitItems();
        
        // Add editor buttons (bottom row)
        addEditorButtons();
    }
    
    private void addKitItems() {
        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < items.size() && i < 45; i++) {
            inventory.setItem(i, items.get(i));
        }
    }
    
    private void addEditorButtons() {
        // Save button
        ItemStack saveButton = new ItemBuilder(Material.GREEN_CONCRETE)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.save.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.save.lore"))
            .build();
        inventory.setItem(45, saveButton);
        
        // Cancel button
        ItemStack cancelButton = new ItemBuilder(Material.RED_CONCRETE)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.cancel.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.cancel.lore"))
            .build();
        inventory.setItem(46, cancelButton);
        
        // Rename item button
        ItemStack renameButton = new ItemBuilder(Material.NAME_TAG)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.rename-item.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.rename-item.lore"))
            .build();
        inventory.setItem(47, renameButton);
        
        // Edit lore button
        ItemStack loreButton = new ItemBuilder(Material.WRITABLE_BOOK)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.edit-lore.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.edit-lore.lore"))
            .build();
        inventory.setItem(48, loreButton);
        
        // Kit settings button
        ItemStack settingsButton = new ItemBuilder(Material.ANVIL)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.kit-settings.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.kit-settings.lore"))
            .build();
        inventory.setItem(49, settingsButton);
        
        // Add enchantment button
        ItemStack enchantButton = new ItemBuilder(Material.ENCHANTING_TABLE)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.add-enchant.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.add-enchant.lore"))
            .build();
        inventory.setItem(50, enchantButton);
        
        // Remove enchantment button
        ItemStack removeEnchantButton = new ItemBuilder(Material.GRINDSTONE)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.remove-enchant.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.remove-enchant.lore"))
            .build();
        inventory.setItem(51, removeEnchantButton);
        
        // Add command button
        ItemStack commandButton = new ItemBuilder(Material.COMMAND_BLOCK)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.add-command.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.add-command.lore"))
            .build();
        inventory.setItem(52, commandButton);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        
        // Allow editing in kit area (slots 0-44)
        if (slot < 45) {
            hasChanges = true;
            return; // Don't cancel - allow item manipulation
        }
        
        // Cancel all clicks on editor buttons
        event.setCancelled(true);
        
        switch (slot) {
            case 45: // Save button
                saveKit();
                MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("success.kit-saved"));
                player.closeInventory();
                playSound("ENTITY_PLAYER_LEVELUP");
                break;
                
            case 46: // Cancel button
                player.closeInventory();
                playSound("UI_BUTTON_CLICK");
                break;
                
            case 47: // Rename item
                handleRenameItem();
                break;
                
            case 48: // Edit lore
                handleEditLore();
                break;
                
            case 49: // Kit settings
                openKitSettings();
                break;
                
            case 50: // Add enchantment
                handleAddEnchantment();
                break;
                
            case 51: // Remove enchantment
                handleRemoveEnchantment();
                break;
                
            case 52: // Add command
                handleAddCommand();
                break;
        }
    }
    
    private void saveKit() {
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
        hasChanges = false;
    }
    
    private void handleRenameItem() {
        ItemStack handItem = player.getItemInHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("editor.no-item"));
            return;
        }
        
        // For demo purposes, we'll add a simple rename
        // In a real implementation, you'd want to use a chat input system
        ItemMeta meta = handItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize("&e&lRenamed Item"));
            handItem.setItemMeta(meta);
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("editor.item-renamed", "{name}", "Renamed Item"));
            hasChanges = true;
        }
        playSound("UI_BUTTON_CLICK");
    }
    
    private void handleEditLore() {
        ItemStack handItem = player.getItemInHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("editor.no-item"));
            return;
        }
        
        // For demo purposes, add sample lore
        ItemMeta meta = handItem.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.colorize("&7This item has been"));
            lore.add(MessageUtils.colorize("&7edited in the kit editor!"));
            lore.add(MessageUtils.colorize("&e&lCustom Lore"));
            meta.setLore(lore);
            handItem.setItemMeta(meta);
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("editor.lore-updated"));
            hasChanges = true;
        }
        playSound("UI_BUTTON_CLICK");
    }
    
    private void handleAddEnchantment() {
        ItemStack handItem = player.getItemInHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("editor.no-item"));
            return;
        }
        
        // Add a sample over-enchantment
        Enchantment enchant = Enchantment.PROTECTION;
        int level = 10; // Over-enchanted level
        
        handItem.addUnsafeEnchantment(enchant, level);
        MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("editor.enchant-added", 
            "{enchant}", enchant.getKey().getKey(), "{level}", String.valueOf(level)));
        hasChanges = true;
        playSound("BLOCK_ENCHANTMENT_TABLE_USE");
    }
    
    private void handleRemoveEnchantment() {
        ItemStack handItem = player.getItemInHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("editor.no-item"));
            return;
        }
        
        if (handItem.getEnchantments().isEmpty()) {
            MessageUtils.sendMessage(player, "&c&l✗ &cThis item has no enchantments to remove!");
            return;
        }
        
        // Remove first enchantment found
        Enchantment firstEnchant = handItem.getEnchantments().keySet().iterator().next();
        handItem.removeEnchantment(firstEnchant);
        MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("editor.enchant-removed", 
            "{enchant}", firstEnchant.getKey().getKey()));
        hasChanges = true;
        playSound("BLOCK_GRINDSTONE_USE");
    }
    
    private void handleAddCommand() {
        // Add a sample command to the kit
        String sampleCommand = "tell {player} You received the " + kit.getName() + " kit!";
        kit.getCommands().add(sampleCommand);
        MessageUtils.sendMessage(player, plugin.getLanguageManager().getMessage("editor.command-added", 
            "{command}", sampleCommand));
        hasChanges = true;
        playSound("UI_BUTTON_CLICK");
    }
    
    private void openKitSettings() {
        // For now, just show a message. In a full implementation, this would open another GUI
        MessageUtils.sendMessage(player, "&e&l⚙ &eKit Settings:");
        MessageUtils.sendMessage(player, "&7Name: &f" + MessageUtils.stripColor(kit.getName()));
        MessageUtils.sendMessage(player, "&7Cooldown: &f" + MessageUtils.formatDuration(kit.getCooldown()));
        MessageUtils.sendMessage(player, "&7Cost: &f$" + kit.getCost());
        MessageUtils.sendMessage(player, "&7Permission: &f" + (kit.getPermission().isEmpty() ? "None" : kit.getPermission()));
        MessageUtils.sendMessage(player, "&7Enabled: &f" + (kit.isEnabled() ? "&aYes" : "&cNo"));
        playSound("UI_BUTTON_CLICK");
    }
    
    @Override
    public void handleClose(InventoryCloseEvent event) {
        if (hasChanges) {
            // Auto-save on close
            saveKit();
            MessageUtils.sendMessage(player, "&a&l✓ &aKit auto-saved on close!");
        }
    }
}