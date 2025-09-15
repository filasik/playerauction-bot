package me.skerik.auctionbot.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Manages plugin configuration
 */
public class ConfigManager {
    
    private final Plugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Loads the configuration from config.yml
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    /**
     * Reloads the configuration
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    // OpenAI Configuration
    public String getOpenAIApiKey() {
        return config.getString("openai.api-key", "");
    }
    
    public String getOpenAIModel() {
        return config.getString("openai.model", "gpt-3.5-turbo");
    }
    
    public double getTemperature() {
        return config.getDouble("openai.temperature", 0.7);
    }
    
    public int getMaxTokens() {
        return config.getInt("openai.max-tokens", 1000);
    }
    
    // Bot Configuration
    public String getBotPlayerUUID() {
        return config.getString("bot.player-uuid", "");
    }
    
    public double getBotBudget() {
        return config.getDouble("bot.budget", 10000.0);
    }
    
    public double getMinProfitMargin() {
        return config.getDouble("bot.min-profit-margin", 15.0);
    }
    
    public boolean isVirtualMode() {
        return config.getBoolean("bot.virtual-mode", true);
    }
    
    public List<String> getAvailableItems() {
        return config.getStringList("bot.available-items");
    }
    
    public int getMaxListingsPerItem() {
        return config.getInt("bot.max-listings-per-item", 2);
    }
    
    // Monitoring Configuration
    public int getMonitorIntervalMinutes() {
        return config.getInt("monitoring.interval-minutes", 30);
    }
    
    public long getDataRetentionTime() {
        return config.getLong("monitoring.data-retention-hours", 24) * 60 * 60 * 1000; // Convert to milliseconds
    }
    
    public boolean isDebugMode() {
        return config.getBoolean("monitoring.debug", false);
    }
    
    // Auction Settings
    public double getMaxAuctionPrice() {
        return config.getDouble("auction.max-price", 5000.0);
    }
    
    public int getMaxAuctionQuantity() {
        return config.getInt("auction.max-quantity", 64);
    }
    
    public int getAuctionDurationHours() {
        return config.getInt("auction.duration-hours", 24);
    }
    
    public boolean getAllowBiddingAuctions() {
        return config.getBoolean("auction.allow-bidding", true);
    }
    
    /**
     * Get the plugin instance
     */
    public Plugin getPlugin() {
        return plugin;
    }
}
