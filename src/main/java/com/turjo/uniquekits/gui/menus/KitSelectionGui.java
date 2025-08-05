package com.turjo.uniquekits.gui.menus;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.gui.BaseGui;
import com.turjo.uniquekits.kits.Kit;
import com.turjo.uniquekits.storage.PlayerData;
import com.turjo.uniquekits.utils.ItemBuilder;
import com.turjo.uniquekits.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitSelectionGui extends BaseGui {
    
    private List<Kit> availableKits;
    private int currentPage = 0;
    private final int itemsPerPage = 45;
    
    public KitSelectionGui(UniqueKits plugin, org.bukkit.entity.Player player) {
        super(plugin, player);
        this.availableKits = plugin.getKitManager().getAvailableKits(player);
    }
    
    @Override
    public void create() {
        String title = plugin.getLanguageManager().getMessage("gui.main-title");
        this.inventory = createInventory(54, title);
        
        // Add kits
        addKits();
        
        // Add navigation
        addNavigation();
        
        // Add info item
        addInfoItem();
    }
    
    private void addKits() {
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, availableKits.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Kit kit = availableKits.get(i);
            ItemStack icon = createKitIcon(kit);
            inventory.setItem(i - startIndex, icon);
        }
    }
    
    private ItemStack createKitIcon(Kit kit) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        List<String> lore = new ArrayList<>(kit.getLore());
        lore.add("");
        
        // Add cooldown info
        if (kit.getCooldown() > 0) {
            if (playerData.isKitOnCooldown(kit.getId())) {
                long remaining = playerData.getKitCooldownRemaining(kit.getId());
                String timeFormat = MessageUtils.formatDuration(remaining);
                lore.add(plugin.getLanguageManager().getMessage("gui.lore.kit-on-cooldown", "{time}", timeFormat));
            } else {
                lore.add("&a✓ Ready to claim!");
            }
        }
        
        // Add cost info
        if (kit.getCost() > 0) {
            lore.add(plugin.getLanguageManager().getMessage("gui.lore.kit-cost", "{cost}", String.valueOf(kit.getCost())));
        }
        
        // Add permission info
        if (!kit.getPermission().isEmpty()) {
            lore.add(plugin.getLanguageManager().getMessage("gui.lore.kit-permission", "{permission}", kit.getPermission()));
        }
        
        lore.add("");
        lore.add(plugin.getLanguageManager().getMessage("gui.lore.click-to-claim"));
        lore.add(plugin.getLanguageManager().getMessage("gui.lore.click-to-preview"));
        
        return new ItemBuilder(kit.getIconMaterial())
            .name(kit.getName())
            .lore(lore)
            .build();
    }
    
    private void addNavigation() {
        int totalPages = (int) Math.ceil((double) availableKits.size() / itemsPerPage);
        
        // Previous page
        if (currentPage > 0) {
            ItemStack prevButton = new ItemBuilder(Material.ARROW)
                .name(plugin.getLanguageManager().getMessage("gui.buttons.previous-page.name"))
                .lore(plugin.getLanguageManager().getMessageList("gui.buttons.previous-page.lore"))
                .build();
            inventory.setItem(45, prevButton);
        }
        
        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = new ItemBuilder(Material.ARROW)
                .name(plugin.getLanguageManager().getMessage("gui.buttons.next-page.name"))
                .lore(plugin.getLanguageManager().getMessageList("gui.buttons.next-page.lore"))
                .build();
            inventory.setItem(53, nextButton);
        }
        
        // Close button
        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
            .name(plugin.getLanguageManager().getMessage("gui.buttons.close.name"))
            .lore(plugin.getLanguageManager().getMessageList("gui.buttons.close.lore"))
            .build();
        inventory.setItem(49, closeButton);
    }
    
    private void addInfoItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7Total Kits: &e" + plugin.getKitManager().getAllKits().size());
        lore.add("&7Available Kits: &a" + availableKits.size());
        lore.add("&7Page: &e" + (currentPage + 1) + "&7/&e" + Math.max(1, (int) Math.ceil((double) availableKits.size() / itemsPerPage)));
        lore.add("");
        lore.add("&7Left-click a kit to preview");
        lore.add("&7Right-click a kit to claim");
        
        ItemStack infoItem = new ItemBuilder(Material.BOOK)
            .name("&e&lKit Information")
            .lore(lore)
            .build();
        inventory.setItem(4, infoItem);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Handle navigation
        if (slot == 45 && currentPage > 0) {
            currentPage--;
            create();
            playSound("UI_BUTTON_CLICK");
            return;
        }
        
        if (slot == 53) {
            int totalPages = (int) Math.ceil((double) availableKits.size() / itemsPerPage);
            if (currentPage < totalPages - 1) {
                currentPage++;
                create();
                playSound("UI_BUTTON_CLICK");
                return;
            }
        }
        
        if (slot == 49) {
            player.closeInventory();
            playSound("UI_BUTTON_CLICK");
            return;
        }
        
        // Handle kit selection
        if (slot < itemsPerPage) {
            int kitIndex = currentPage * itemsPerPage + slot;
            if (kitIndex < availableKits.size()) {
                Kit kit = availableKits.get(kitIndex);
                
                if (event.isRightClick()) {
                    // Claim kit
                    player.closeInventory();
                    plugin.getKitManager().giveKit(player, kit, false);
                } else {
                    // Preview kit
                    plugin.getGuiManager().openKitPreview(player, kit);
                }
                playSound("UI_BUTTON_CLICK");
            }
        }
    }
    
    @Override
    public void handleClose(InventoryCloseEvent event) {
        // Nothing special needed for kit selection GUI
    }
}