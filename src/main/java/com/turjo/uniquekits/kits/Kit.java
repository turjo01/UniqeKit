package com.turjo.uniquekits.kits;

import com.turjo.uniquekits.UniqueKits;
import com.turjo.uniquekits.utils.ItemBuilder;
import com.turjo.uniquekits.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Kit {
    
    private final String id;
    private String name;
    private String description;
    private List<String> lore;
    private Material icon;
    private int iconData;
    private String iconCustomModelData;
    private List<ItemStack> items;
    private List<String> commands;
    private List<PotionEffect> effects;
    private long cooldown;
    private int cost;
    private String permission;
    private boolean oneTimeUse;
    private boolean autoGiveOnJoin;
    private boolean autoGiveOnRespawn;
    private boolean firstJoinKit;
    private List<String> allowedWorlds;
    private List<String> blockedWorlds;
    private Map<String, Object> requirements;
    private String sound;
    private String particle;
    private int priority;
    private boolean enabled;
    
    public Kit(String id) {
        this.id = id;
        this.name = id;
        this.description = "";
        this.lore = new ArrayList<>();
        this.icon = Material.CHEST;
        this.iconData = 0;
        this.iconCustomModelData = null;
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.cooldown = 0;
        this.cost = 0;
        this.permission = "";
        this.oneTimeUse = false;
        this.autoGiveOnJoin = false;
        this.autoGiveOnRespawn = false;
        this.firstJoinKit = false;
        this.allowedWorlds = new ArrayList<>();
        this.blockedWorlds = new ArrayList<>();
        this.requirements = new HashMap<>();
        this.sound = "";
        this.particle = "";
        this.priority = 0;
        this.enabled = true;
    }
    
    public static Kit fromConfig(String id, ConfigurationSection section) {
        Kit kit = new Kit(id);
        
        kit.name = MessageUtils.colorize(section.getString("name", id));
        kit.description = MessageUtils.colorize(section.getString("description", ""));
        kit.lore = MessageUtils.colorizeList(section.getStringList("lore"));
        
        // Icon
        String iconString = section.getString("icon", "CHEST");
        try {
            kit.icon = Material.valueOf(iconString.toUpperCase());
        } catch (IllegalArgumentException e) {
            kit.icon = Material.CHEST;
            UniqueKits.getInstance().getLogger().warning("Invalid icon material for kit " + id + ": " + iconString);
        }
        
        kit.iconData = section.getInt("icon-data", 0);
        kit.iconCustomModelData = section.getString("icon-custom-model-data");
        
        // Items
        if (section.contains("items")) {
            List<Map<?, ?>> itemMaps = section.getMapList("items");
            for (Map<?, ?> itemMap : itemMaps) {
                ItemStack item = ItemBuilder.fromMap(itemMap);
                if (item != null) {
                    kit.items.add(item);
                }
            }
        }
        
        // Commands
        kit.commands = section.getStringList("commands");
        
        // Effects
        if (section.contains("effects")) {
            List<Map<?, ?>> effectMaps = section.getMapList("effects");
            for (Map<?, ?> effectMap : effectMaps) {
                PotionEffect effect = createPotionEffect(effectMap);
                if (effect != null) {
                    kit.effects.add(effect);
                }
            }
        }
        
        // Settings
        kit.cooldown = section.getLong("cooldown", 0);
        kit.cost = section.getInt("cost", 0);
        kit.permission = section.getString("permission", "");
        kit.oneTimeUse = section.getBoolean("one-time-use", false);
        kit.autoGiveOnJoin = section.getBoolean("auto-give-on-join", false);
        kit.autoGiveOnRespawn = section.getBoolean("auto-give-on-respawn", false);
        kit.firstJoinKit = section.getBoolean("first-join-kit", false);
        kit.allowedWorlds = section.getStringList("allowed-worlds");
        kit.blockedWorlds = section.getStringList("blocked-worlds");
        kit.sound = section.getString("sound", "");
        kit.particle = section.getString("particle", "");
        kit.priority = section.getInt("priority", 0);
        kit.enabled = section.getBoolean("enabled", true);
        
        // Requirements
        if (section.contains("requirements")) {
            ConfigurationSection reqSection = section.getConfigurationSection("requirements");
            if (reqSection != null) {
                for (String key : reqSection.getKeys(false)) {
                    kit.requirements.put(key, reqSection.get(key));
                }
            }
        }
        
        return kit;
    }
    
    private static PotionEffect createPotionEffect(Map<?, ?> effectMap) {
        try {
            Object typeObj = effectMap.get("type");
            if (typeObj == null) return null;
            String type = typeObj.toString();
            
            Object durationObj = effectMap.get("duration");
            int duration = 600;
            if (durationObj instanceof Number) {
                duration = ((Number) durationObj).intValue();
            } else if (durationObj != null) {
                try {
                    duration = Integer.parseInt(durationObj.toString());
                } catch (NumberFormatException ignored) {
                    duration = 600;
                }
            }
            
            Object amplifierObj = effectMap.get("amplifier");
            int amplifier = 0;
            if (amplifierObj instanceof Number) {
                amplifier = ((Number) amplifierObj).intValue();
            } else if (amplifierObj != null) {
                try {
                    amplifier = Integer.parseInt(amplifierObj.toString());
                } catch (NumberFormatException ignored) {
                    amplifier = 0;
                }
            }
            
            Object ambientObj = effectMap.get("ambient");
            boolean ambient = false;
            if (ambientObj instanceof Boolean) {
                ambient = (Boolean) ambientObj;
            } else if (ambientObj != null) {
                ambient = Boolean.parseBoolean(ambientObj.toString());
            }
            
            Object particlesObj = effectMap.get("particles");
            boolean particles = true;
            if (particlesObj instanceof Boolean) {
                particles = (Boolean) particlesObj;
            } else if (particlesObj != null) {
                particles = Boolean.parseBoolean(particlesObj.toString());
            }
            
            Object iconObj = effectMap.get("icon");
            boolean icon = true;
            if (iconObj instanceof Boolean) {
                icon = (Boolean) iconObj;
            } else if (iconObj != null) {
                icon = Boolean.parseBoolean(iconObj.toString());
            }
            
            PotionEffectType effectType = PotionEffectType.getByName(type.toUpperCase());
            if (effectType != null) {
                return new PotionEffect(effectType, duration, amplifier, ambient, particles, icon);
            }
        } catch (Exception e) {
            UniqueKits.getInstance().getLogger().warning("Invalid potion effect configuration: " + e.getMessage());
        }
        return null;
    }
    
    public void saveToConfig(ConfigurationSection section) {
        section.set("name", name);
        section.set("description", description);
        section.set("lore", lore);
        section.set("icon", icon.name());
        section.set("icon-data", iconData);
        if (iconCustomModelData != null) {
            section.set("icon-custom-model-data", iconCustomModelData);
        }
        
        // Save items
        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (ItemStack item : items) {
            itemMaps.add(item.serialize());
        }
        section.set("items", itemMaps);
        
        // Save commands
        section.set("commands", commands);
        
        // Save effects
        List<Map<String, Object>> effectMaps = new ArrayList<>();
        for (PotionEffect effect : effects) {
            Map<String, Object> effectMap = new HashMap<>();
            effectMap.put("type", effect.getType().getName());
            effectMap.put("duration", effect.getDuration());
            effectMap.put("amplifier", effect.getAmplifier());
            effectMap.put("ambient", effect.isAmbient());
            effectMap.put("particles", effect.hasParticles());
            effectMap.put("icon", effect.hasIcon());
            effectMaps.add(effectMap);
        }
        section.set("effects", effectMaps);
        
        // Save settings
        section.set("cooldown", cooldown);
        section.set("cost", cost);
        section.set("permission", permission);
        section.set("one-time-use", oneTimeUse);
        section.set("auto-give-on-join", autoGiveOnJoin);
        section.set("auto-give-on-respawn", autoGiveOnRespawn);
        section.set("first-join-kit", firstJoinKit);
        section.set("allowed-worlds", allowedWorlds);
        section.set("blocked-worlds", blockedWorlds);
        section.set("sound", sound);
        section.set("particle", particle);
        section.set("priority", priority);
        section.set("enabled", enabled);
        
        // Save requirements
        if (!requirements.isEmpty()) {
            ConfigurationSection reqSection = section.createSection("requirements");
            for (Map.Entry<String, Object> entry : requirements.entrySet()) {
                reqSection.set(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public boolean canUse(Player player) {
        // Check if kit is enabled
        if (!enabled) {
            return false;
        }
        
        // Check permission
        if (!permission.isEmpty() && !player.hasPermission(permission) && !player.hasPermission("uniquekits.bypass.permission")) {
            return false;
        }
        
        // Check world restrictions
        String worldName = player.getWorld().getName();
        if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(worldName) && !player.hasPermission("uniquekits.bypass.world")) {
            return false;
        }
        if (!blockedWorlds.isEmpty() && blockedWorlds.contains(worldName) && !player.hasPermission("uniquekits.bypass.world")) {
            return false;
        }
        
        // Check requirements
        return checkRequirements(player);
    }
    
    private boolean checkRequirements(Player player) {
        for (Map.Entry<String, Object> entry : requirements.entrySet()) {
            String requirement = entry.getKey().toLowerCase();
            Object value = entry.getValue();
            
            switch (requirement) {
                case "level":
                    int requiredLevel = 0;
                    if (value instanceof Number) {
                        requiredLevel = ((Number) value).intValue();
                    } else if (value != null) {
                        try {
                            requiredLevel = Integer.parseInt(value.toString());
                        } catch (NumberFormatException ignored) {
                            continue;
                        }
                    }
                    if (player.getLevel() < requiredLevel) {
                        return false;
                    }
                    break;
                case "exp":
                    int requiredExp = 0;
                    if (value instanceof Number) {
                        requiredExp = ((Number) value).intValue();
                    } else if (value != null) {
                        try {
                            requiredExp = Integer.parseInt(value.toString());
                        } catch (NumberFormatException ignored) {
                            continue;
                        }
                    }
                    if (player.getTotalExperience() < requiredExp) {
                        return false;
                    }
                    break;
                case "money":
                    if (UniqueKits.getInstance().getHookManager().getEconomy() != null) {
                        double balance = UniqueKits.getInstance().getHookManager().getEconomy().getBalance(player);
                        double requiredMoney = 0.0;
                        if (value instanceof Number) {
                            requiredMoney = ((Number) value).doubleValue();
                        } else if (value != null) {
                            try {
                                requiredMoney = Double.parseDouble(value.toString());
                            } catch (NumberFormatException ignored) {
                                continue;
                            }
                        }
                        if (balance < requiredMoney) {
                            return false;
                        }
                    }
                    break;
                case "permission":
                    if (!player.hasPermission(value.toString())) {
                        return false;
                    }
                    break;
                // Add more requirements as needed
            }
        }
        return true;
    }
    
    public ItemStack getIcon() {
        ItemBuilder builder = new ItemBuilder(icon, 1)
                .name(name)
                .lore(lore);
        
        if (iconData > 0) {
            builder.durability((short) iconData);
        }
        
        if (iconCustomModelData != null) {
            try {
                builder.customModelData(Integer.parseInt(iconCustomModelData));
            } catch (NumberFormatException ignored) {}
        }
        
        return builder.build();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public Material getIconMaterial() { return icon; }
    public void setIcon(Material icon) { this.icon = icon; }
    public int getIconData() { return iconData; }
    public void setIconData(int iconData) { this.iconData = iconData; }
    public String getIconCustomModelData() { return iconCustomModelData; }
    public void setIconCustomModelData(String iconCustomModelData) { this.iconCustomModelData = iconCustomModelData; }
    public List<ItemStack> getItems() { return items; }
    public void setItems(List<ItemStack> items) { this.items = items; }
    public List<String> getCommands() { return commands; }
    public void setCommands(List<String> commands) { this.commands = commands; }
    public List<PotionEffect> getEffects() { return effects; }
    public void setEffects(List<PotionEffect> effects) { this.effects = effects; }
    public long getCooldown() { return cooldown; }
    public void setCooldown(long cooldown) { this.cooldown = cooldown; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public boolean isOneTimeUse() { return oneTimeUse; }
    public void setOneTimeUse(boolean oneTimeUse) { this.oneTimeUse = oneTimeUse; }
    public boolean isAutoGiveOnJoin() { return autoGiveOnJoin; }
    public void setAutoGiveOnJoin(boolean autoGiveOnJoin) { this.autoGiveOnJoin = autoGiveOnJoin; }
    public boolean isAutoGiveOnRespawn() { return autoGiveOnRespawn; }
    public void setAutoGiveOnRespawn(boolean autoGiveOnRespawn) { this.autoGiveOnRespawn = autoGiveOnRespawn; }
    public boolean isFirstJoinKit() { return firstJoinKit; }
    public void setFirstJoinKit(boolean firstJoinKit) { this.firstJoinKit = firstJoinKit; }
    public List<String> getAllowedWorlds() { return allowedWorlds; }
    public void setAllowedWorlds(List<String> allowedWorlds) { this.allowedWorlds = allowedWorlds; }
    public List<String> getBlockedWorlds() { return blockedWorlds; }
    public void setBlockedWorlds(List<String> blockedWorlds) { this.blockedWorlds = blockedWorlds; }
    public Map<String, Object> getRequirements() { return requirements; }
    public void setRequirements(Map<String, Object> requirements) { this.requirements = requirements; }
    public String getSound() { return sound; }
    public void setSound(String sound) { this.sound = sound; }
    public String getParticle() { return particle; }
    public void setParticle(String particle) { this.particle = particle; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}