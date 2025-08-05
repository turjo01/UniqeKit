package com.turjo.uniquekits.gui.menus;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.gui.BaseGui;
import com.turjo.uniquekits.kits.Kit;
import com.turjo.uniquekits.utils.ItemBuilder;
import com.turjo.uniquekits.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitPreviewGui extends BaseGui {
    
    private final Kit kit;
    
    public KitPreviewGui(UniqueKits plugin, Player player, Kit kit) {
        super(plugin, player);
        this.kit = kit;
    }
    
    @Override
    public void create() {
        String title = plugin.getLanguageManager().getMessage("gui.preview-title", "{kit}", kit.getName());
        this.inventory = createInventory(54, title);
        
        // Add kit items
        addKitItems();
        
        // Add control buttons
        addControlButtons();
        
        // Add info item
        addKitInfo();
    }
    
    private void addKitItems() {
        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < items.size() && i < 45; i++) {
            inventory.setItem(i, items.get(i));
        }
    }
    
    private void addControlButtons() {
        // Claim button
        List<String> claimLore = new ArrayList<>();
        claimLore.add("&7Click to claim this amazing kit!");
        claimLore.add("");
        
        if (kit.getCooldown() > 0) {
            String cooldownText = MessageUtils.formatDuration(kit.getCooldown());
            claimLore.add("&eCooldown: &f" + cooldownText);
        } else {
            claimLore.add("&eCooldown: &fNone");
        }
        
        if (kit.getCost() > 0) {
            claimLore.add("&eCost: &f$" + kit.getCost());
        } else {
            claimLore.add("&eCost: &fFree");
        }
        
        claimLore.add("");
        claimLore.add("&a&l✓ Click to claim!");
        
        ItemStack claimButton = new ItemBuilder(Material.LIME_CONCRETE)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.claim.name"))
            .lore(claimLore)
            .build();
        inventory.setItem(49, claimButton);
        
        // Back button
        ItemStack backButton = new ItemBuilder(Material.ARROW)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.back.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.back.lore"))
            .build();
        inventory.setItem(45, backButton);
    }
    
    private void addKitInfo() {
        List<String> lore = new ArrayList<>();
        lore.add("&7Name: &f" + MessageUtils.stripColor(kit.getName()));
        lore.add("&7Description: &f" + MessageUtils.stripColor(kit.getDescription()));
        
        if (kit.getCooldown() > 0) {
            lore.add("&7Cooldown: &f" + MessageUtils.formatDuration(kit.getCooldown()));
        }
        
        if (kit.getCost() > 0) {
            lore.add("&7Cost: &f$" + kit.getCost());
        }
        
        if (!kit.getPermission().isEmpty()) {
            lore.add("&7Permission: &f" + kit.getPermission());
        }
        
        lore.add("&7Items: &f" + kit.getItems().size());
        lore.add("&7Commands: &f" + kit.getCommands().size());
        lore.add("&7Effects: &f" + kit.getEffects().size());
        
        ItemStack infoItem = new ItemBuilder(Material.PAPER)
            .name("&e&lKit Information")
            .lore(lore)
            .build();
        inventory.setItem(4, infoItem);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        if (slot == 49) { // Claim button
            player.closeInventory();
            plugin.getKitManager().giveKit(player, kit, false);
            playSound("ENTITY_PLAYER_LEVELUP");
        } else if (slot == 45) { // Back button
            plugin.getGuiManager().openKitSelection(player);
            playSound("UI_BUTTON_CLICK");
        }
    }
    
    @Override
    public void handleClose(InventoryCloseEvent event) {
        // Nothing special needed
    }
}