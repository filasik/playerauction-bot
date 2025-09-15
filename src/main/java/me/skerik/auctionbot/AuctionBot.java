package me.skerik.auctionbot;

import com.olziedev.playerauctions.api.PlayerAuctionsAPI;
import me.skerik.auctionbot.config.ConfigManager;
import me.skerik.auctionbot.managers.AuctionManager;
import me.skerik.auctionbot.managers.OpenAIManager;
import me.skerik.auctionbot.tasks.AuctionMonitorTask;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for AuctionBot
 * Integrates with PlayerAuctions API and OpenAI to automatically manage auctions
 */
public class AuctionBot extends JavaPlugin {
    
    private static AuctionBot instance;
    private ConfigManager configManager;
    private AuctionManager auctionManager;
    private OpenAIManager openAIManager;
    private AuctionMonitorTask monitorTask;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Initialize managers
        openAIManager = new OpenAIManager(configManager);
        
        // Wait for PlayerAuctions API to be ready
        PlayerAuctionsAPI.getInstance(api -> {
            auctionManager = new AuctionManager(api, openAIManager, configManager);
            
            // Start the auction monitoring task
            monitorTask = new AuctionMonitorTask(auctionManager, configManager);
            monitorTask.start();
            
            getLogger().info("AuctionBot has been enabled successfully!");
        });
    }
    
    @Override
    public void onDisable() {
        if (monitorTask != null) {
            monitorTask.stop();
        }
        
        getLogger().info("AuctionBot has been disabled!");
    }
    
    public static AuctionBot getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public AuctionManager getAuctionManager() {
        return auctionManager;
    }
    
    public OpenAIManager getOpenAIManager() {
        return openAIManager;
    }
}
