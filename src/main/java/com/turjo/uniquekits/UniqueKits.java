package com.turjo.uniquekits;

import com.turjo.uniquekits.commands.KitCommand;
import com.turjo.uniquekits.commands.UniqueKitsCommand;
import com.turjo.uniquekits.config.ConfigManager;
import com.turjo.uniquekits.config.LanguageManager;
import com.turjo.uniquekits.gui.GuiManager;
import com.turjo.uniquekits.hooks.HookManager;
import com.turjo.uniquekits.kits.KitManager;
import com.turjo.uniquekits.listeners.PlayerListener;
import com.turjo.uniquekits.placeholders.PlaceholderManager;
import com.turjo.uniquekits.storage.PlayerDataManager;
import com.turjo.uniquekits.utils.MessageUtils;
import com.turjo.uniquekits.utils.UpdateChecker;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UniqueKits extends JavaPlugin {
    
    private static UniqueKits instance;
    private BukkitAudiences adventure;
    
    // Managers
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private KitManager kitManager;
    private GuiManager guiManager;
    private HookManager hookManager;
    private PlayerDataManager playerDataManager;
    private PlaceholderManager placeholderManager;
    
    @Override
    public void onLoad() {
        instance = this;
    }
    
    @Override
    public void onEnable() {
        // Initialize Adventure
        this.adventure = BukkitAudiences.create(this);
        
        getLogger().info("§a[UniqueKits] Starting UniqueKits v" + getDescription().getVersion() + " by Turjo");
        
        // Initialize managers
        initializeManagers();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Check for updates
        checkForUpdates();
        
        getLogger().info("§a[UniqueKits] Successfully enabled! Ready to serve amazing kits!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("§c[UniqueKits] Shutting down...");
        
        // Save all player data
        if (playerDataManager != null) {
            playerDataManager.saveAllPlayerData();
        }
        
        // Close adventure
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
        
        getLogger().info("§c[UniqueKits] Successfully disabled!");
    }
    
    private void initializeManagers() {
        // Order matters for dependencies
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.kitManager = new KitManager(this);
        this.guiManager = new GuiManager(this);
        this.hookManager = new HookManager(this);
        this.placeholderManager = new PlaceholderManager(this);
        
        getLogger().info("§a[UniqueKits] All managers initialized successfully!");
    }
    
    private void registerCommands() {
        getCommand("uniquekits").setExecutor(new UniqueKitsCommand(this));
        getCommand("kit").setExecutor(new KitCommand(this));
        
        getLogger().info("§a[UniqueKits] Commands registered successfully!");
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("§a[UniqueKits] Event listeners registered successfully!");
    }
    
    private void checkForUpdates() {
        if (configManager.getConfig().getBoolean("settings.check-updates", true)) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                UpdateChecker updateChecker = new UpdateChecker(this);
                updateChecker.checkForUpdates();
            });
        }
    }
    
    public void reload() {
        try {
            // Reload configurations
            configManager.reloadConfigs();
            languageManager.reloadLanguage();
            
            // Reload kits
            kitManager.reloadKits();
            
            // Reload hooks
            hookManager.reloadHooks();
            
            getLogger().info("§a[UniqueKits] Plugin reloaded successfully!");
        } catch (Exception e) {
            getLogger().severe("§c[UniqueKits] Error during reload: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters
    public static UniqueKits getInstance() {
        return instance;
    }
    
    public BukkitAudiences getAdventure() {
        return adventure;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public KitManager getKitManager() {
        return kitManager;
    }
    
    public GuiManager getGuiManager() {
        return guiManager;
    }
    
    public HookManager getHookManager() {
        return hookManager;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
}