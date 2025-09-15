package me.skerik.auctionbot.tasks;

import me.skerik.auctionbot.AuctionBot;
import me.skerik.auctionbot.config.ConfigManager;
import me.skerik.auctionbot.managers.AuctionManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

/**
 * Periodic task that monitors auctions and triggers AI analysis
 */
public class AuctionMonitorTask {
    
    private final AuctionManager auctionManager;
    private final ConfigManager configManager;
    private final Logger logger;
    
    private BukkitTask task;
    private boolean running = false;
    
    public AuctionMonitorTask(AuctionManager auctionManager, ConfigManager configManager) {
        this.auctionManager = auctionManager;
        this.configManager = configManager;
        this.logger = AuctionBot.getInstance().getLogger();
    }
    
    /**
     * Starts the monitoring task
     */
    public void start() {
        if (running) {
            logger.warning("Auction monitor task is already running!");
            return;
        }
        
        long intervalTicks = configManager.getMonitorIntervalMinutes() * 20L * 60L; // Convert minutes to ticks
        long initialDelayTicks = 200L; // 10 seconds initial delay
        
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(
            AuctionBot.getInstance(),
            this::executeMonitoringCycle,
            initialDelayTicks,
            intervalTicks
        );
        
        running = true;
        logger.info(String.format(
            "Auction monitoring task started - will run every %d minutes",
            configManager.getMonitorIntervalMinutes()
        ));
    }
    
    /**
     * Stops the monitoring task
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        running = false;
        logger.info("Auction monitoring task stopped");
    }
    
    /**
     * Executes one monitoring cycle
     */
    private void executeMonitoringCycle() {
        try {
            logger.info("Starting auction monitoring cycle...");
            
            // Cleanup old data first
            auctionManager.cleanupOldData();
            
            // Log current market stats
            logMarketStats();
            
            // Run the main monitoring and decision logic
            auctionManager.monitorAndDecide().join();
            
            logger.info("Auction monitoring cycle completed");
            
        } catch (Exception e) {
            logger.severe("Unexpected error in monitoring cycle: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Logs current market statistics
     */
    private void logMarketStats() {
        try {
            var stats = auctionManager.getMarketStats();
            logger.info(String.format(
                "Market Stats - Total: %s, Bot: %s, Processed: %s, Last Check: %s",
                stats.get("total_auctions"),
                stats.get("bot_auctions"),
                stats.get("processed_auctions"),
                stats.get("last_check")
            ));
        } catch (Exception e) {
            logger.warning("Could not retrieve market stats: " + e.getMessage());
        }
    }
    
    /**
     * Check if the task is currently running
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Get the current task interval in minutes
     */
    public int getIntervalMinutes() {
        return configManager.getMonitorIntervalMinutes();
    }
}
