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
    
    @SuppressWarnings("deprecation")
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
    
    public static ItemStack fromMap(Map<?, ?> map) {
        try {
            // Try to deserialize the ItemStack
            @SuppressWarnings("unchecked")
            Map<String, Object> stringMap = (Map<String, Object>) map;
            ItemStack item = ItemStack.deserialize(stringMap);
            return item;
        } catch (Exception e) {
            // If deserialization fails, try to create manually
            try {
                Object typeObj = map.get("type");
                Object amountObj = map.get("amount");
                
                if (typeObj == null) {
                    return new ItemStack(Material.STONE);
                }
                
                String type = typeObj.toString();
                int amount = 1;
                
                if (amountObj instanceof Number) {
                    amount = ((Number) amountObj).intValue();
                } else if (amountObj != null) {
                    try {
                        amount = Integer.parseInt(amountObj.toString());
                    } catch (NumberFormatException ignored) {
                        amount = 1;
                    }
                }
                
                Material material;
                try {
                    material = Material.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    material = Material.STONE;
                }
                
                ItemBuilder builder = new ItemBuilder(material, amount);
                
                // Handle meta if present
                Object metaObj = map.get("meta");
                if (metaObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> metaMap = (Map<String, Object>) metaObj;
                    
                    Object displayNameObj = metaMap.get("display-name");
                    if (displayNameObj != null) {
                        builder.name(displayNameObj.toString());
                    }
                    
                    Object loreObj = metaMap.get("lore");
                    if (loreObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> lore = (List<String>) loreObj;
                        builder.lore(lore);
                    }
                    
                    Object enchantsObj = metaMap.get("enchants");
                    if (enchantsObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> enchants = (Map<String, Object>) enchantsObj;
                        for (Map.Entry<String, Object> entry : enchants.entrySet()) {
                            try {
                                Enchantment enchant = Enchantment.getByName(entry.getKey());
                                if (enchant != null) {
                                    int level = 1;
                                    if (entry.getValue() instanceof Number) {
                                        level = ((Number) entry.getValue()).intValue();
                                    } else if (entry.getValue() != null) {
                                        try {
                                            level = Integer.parseInt(entry.getValue().toString());
                                        } catch (NumberFormatException ignored) {
                                            level = 1;
                                        }
                                    }
                                    builder.unsafeEnchant(enchant, level);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    
                    Object customModelDataObj = metaMap.get("custom-model-data");
                    if (customModelDataObj instanceof Number) {
                        builder.customModelData(((Number) customModelDataObj).intValue());
                    } else if (customModelDataObj != null) {
                        try {
                            int customModelData = Integer.parseInt(customModelDataObj.toString());
                            builder.customModelData(customModelData);
                        } catch (NumberFormatException ignored) {}
                    }
                    
                    Object unbreakableObj = metaMap.get("unbreakable");
                    if (unbreakableObj instanceof Boolean) {
                        builder.unbreakable((Boolean) unbreakableObj);
                    } else if (unbreakableObj != null) {
                        builder.unbreakable(Boolean.parseBoolean(unbreakableObj.toString()));
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