package com.turjo.uniquekits.config;

import com.turjo.uniquekits.UniqueKits;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    
    private final UniqueKits plugin;
    private FileConfiguration languageConfig;
    private final Map<String, String> messages = new HashMap<>();
    private String currentLanguage;
    
    public LanguageManager(UniqueKits plugin) {
        this.plugin = plugin;
        loadLanguage();
    }
    
    private void loadLanguage() {
        this.currentLanguage = plugin.getConfigManager().getConfig().getString("settings.language", "en");
        
        // Create languages folder if it doesn't exist
        File languagesFolder = new File(plugin.getDataFolder(), "languages");
        if (!languagesFolder.exists()) {
            languagesFolder.mkdirs();
        }
        
        // Load language file
        File languageFile = new File(languagesFolder, currentLanguage + ".yml");
        if (!languageFile.exists()) {
            plugin.saveResource("languages/" + currentLanguage + ".yml", false);
        }
        
        this.languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        loadMessages();
        
        plugin.getLogger().info("§a[LanguageManager] Language '" + currentLanguage + "' loaded successfully!");
    }
    
    private void loadMessages() {
        messages.clear();
        for (String key : languageConfig.getKeys(true)) {
            if (languageConfig.isString(key)) {
                messages.put(key, languageConfig.getString(key));
            }
        }
    }
    
    public void reloadLanguage() {
        loadLanguage();
        plugin.getLogger().info("§a[LanguageManager] Language reloaded!");
    }
    
    public String getMessage(String key) {
        return messages.getOrDefault(key, "§cMissing message: " + key);
    }
    
    public String getMessage(String key, Object... placeholders) {
        String message = getMessage(key);
        
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = placeholders[i].toString();
                String value = placeholders[i + 1].toString();
                message = message.replace(placeholder, value);
            }
        }
        
        return message;
    }
    
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public List<String> getMessageList(String key) {
        if (languageConfig.isList(key)) {
            return languageConfig.getStringList(key);
        } else {
            // If it's a single string, return it as a list
            String message = getMessage(key);
            return Arrays.asList(message);
        }
    }
}