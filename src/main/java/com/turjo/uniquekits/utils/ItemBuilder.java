package com.turjo.uniquekits.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemBuilder {
    
    private ItemStack itemStack;
    private ItemMeta itemMeta;
    
    public ItemBuilder(Material material) {
        this(material, 1);
    }
    
    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.itemMeta = itemStack.getItemMeta();
    }
    
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemMeta = this.itemStack.getItemMeta();
    }
    
    public ItemBuilder name(String name) {
        if (itemMeta != null && name != null) {
            itemMeta.setDisplayName(MessageUtils.colorize(name));
        }
        return this;
    }
    
    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }
    
    public ItemBuilder lore(List<String> lore) {
        if (itemMeta != null && lore != null) {
            List<String> colorizedLore = new ArrayList<>();
            for (String line : lore) {
                colorizedLore.add(MessageUtils.colorize(line));
            }
            itemMeta.setLore(colorizedLore);
        }
        return this;
    }
    
    public ItemBuilder addLore(String... lines) {
        if (itemMeta != null && lines != null) {
            List<String> currentLore = itemMeta.getLore();
            if (currentLore == null) {
                currentLore = new ArrayList<>();
            }
            
            for (String line : lines) {
                currentLore.add(MessageUtils.colorize(line));
            }
            
            itemMeta.setLore(currentLore);
        }
        return this;
    }
    
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }
    
    public ItemBuilder durability(short durability) {
        itemStack.setDurability(durability);
        return this;
    }
    
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, true);
        }
        return this;
    }
    
    public ItemBuilder unsafeEnchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }
    
    public ItemBuilder removeEnchant(Enchantment enchantment) {
        if (itemMeta != null) {
            itemMeta.removeEnchant(enchantment);
        }
        return this;
    }
    
    public ItemBuilder flag(ItemFlag... flags) {
        if (itemMeta != null && flags != null) {
            itemMeta.addItemFlags(flags);
        }
        return this;
    }
    
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (itemMeta != null) {
            itemMeta.setUnbreakable(unbreakable);
        }
        return this;
    }
    
    public ItemBuilder customModelData(int customModelData) {
        if (itemMeta != null) {
            itemMeta.setCustomModelData(customModelData);
        }
        return this;
    }
    
    public ItemBuilder glow() {
        return glow(true);
    }
    
    public ItemBuilder glow(boolean glow) {
        if (glow) {
            enchant(Enchantment.LURE, 1);
            flag(ItemFlag.HIDE_ENCHANTS);
        } else {
            removeEnchant(Enchantment.LURE);
        }
        return this;
    }
    
    public ItemStack build() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
    
    public static ItemStack createItem(Material material, String name, String... lore) {
        return new ItemBuilder(material)
            .name(name)
            .lore(lore)
            .build();
    }
    
    public static ItemStack createItem(Material material, int amount, String name, List<String> lore) {
        return new ItemBuilder(material, amount)
            .name(name)
            .lore(lore)
            .build();
    }
    
    @SuppressWarnings("unchecked")
    public static ItemStack fromMap(Map<?, ?> map) {
        try {
            // Try to deserialize the ItemStack
            ItemStack item = ItemStack.deserialize((Map<String, Object>) map);
            return item;
        } catch (Exception e) {
            // If deserialization fails, try to create manually
            try {
                String type = (String) map.get("type");
                int amount = (Integer) map.getOrDefault("amount", 1);
                
                Material material = Material.valueOf(type.toUpperCase());
                ItemBuilder builder = new ItemBuilder(material, amount);
                
                // Handle meta if present
                if (map.containsKey("meta")) {
                    Map<?, ?> metaMap = (Map<?, ?>) map.get("meta");
                    
                    if (metaMap.containsKey("display-name")) {
                        builder.name((String) metaMap.get("display-name"));
                    }
                    
                    if (metaMap.containsKey("lore")) {
                        List<String> lore = (List<String>) metaMap.get("lore");
                        builder.lore(lore);
                    }
                    
                    if (metaMap.containsKey("enchants")) {
                        Map<String, Integer> enchants = (Map<String, Integer>) metaMap.get("enchants");
                        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                            try {
                                Enchantment enchant = Enchantment.getByName(entry.getKey());
                                if (enchant != null) {
                                    builder.unsafeEnchant(enchant, entry.getValue());
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    
                    if (metaMap.containsKey("custom-model-data")) {
                        builder.customModelData((Integer) metaMap.get("custom-model-data"));
                    }
                    
                    if (metaMap.containsKey("unbreakable")) {
                        builder.unbreakable((Boolean) metaMap.get("unbreakable"));
                    }
                }
                
                return builder.build();
            } catch (Exception ex) {
                // Last resort - return a basic item
                return new ItemStack(Material.STONE);
            }
        }
    }
}