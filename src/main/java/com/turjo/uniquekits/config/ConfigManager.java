package com.turjo.uniquekits.config;

import com.turjo.uniquekits.UniqueKits;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    
    private final UniqueKits plugin;
    private FileConfiguration config;
    private FileConfiguration guiConfig;
    
    public ConfigManager(UniqueKits plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    private void loadConfigs() {
        // Main config
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        // GUI config
        File guiFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        this.guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        
        plugin.getLogger().info("§a[ConfigManager] Configuration files loaded successfully!");
    }
    
    public void reloadConfigs() {
        loadConfigs();
        plugin.getLogger().info("§a[ConfigManager] Configuration files reloaded!");
    }
    
    public void saveGuiConfig() {
        try {
            File guiFile = new File(plugin.getDataFolder(), "gui.yml");
            guiConfig.save(guiFile);
        } catch (IOException e) {
            plugin.getLogger().severe("§c[ConfigManager] Could not save gui.yml: " + e.getMessage());
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }
}