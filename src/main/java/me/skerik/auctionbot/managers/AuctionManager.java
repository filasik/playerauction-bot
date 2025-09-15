package me.skerik.auctionbot.managers;

import com.olziedev.playerauctions.api.PlayerAuctionsAPI;
import com.olziedev.playerauctions.api.auction.Auction;
import com.olziedev.playerauctions.api.auction.product.AProduct;
import com.olziedev.playerauctions.api.expansion.AProductProvider;
import com.olziedev.playerauctions.api.player.APlayer;
import me.skerik.auctionbot.config.ConfigManager;
import me.skerik.auctionbot.models.AuctionData;
import me.skerik.auctionbot.models.AuctionDecision;
import me.skerik.auctionbot.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages auction operations and integrates with OpenAI for decision making
 */
public class AuctionManager {
    
    private final PlayerAuctionsAPI api;
    private final OpenAIManager openAIManager;
    private final ConfigManager configManager;
    private final Logger logger;
    
    // Cache for auction data to avoid duplicate processing
    private final Map<Long, AuctionData> processedAuctions = new ConcurrentHashMap<>();
    
    // Bot's auction player instance
    private APlayer botPlayer;
    
    public AuctionManager(PlayerAuctionsAPI api, OpenAIManager openAIManager, ConfigManager configManager) {
        this.api = api;
        this.openAIManager = openAIManager;
        this.configManager = configManager;
        this.logger = configManager.getPlugin().getLogger();
        
        // Initialize bot player
        initializeBotPlayer();
    }
    
    private void initializeBotPlayer() {
        UUID botUUID = UUID.fromString(configManager.getBotPlayerUUID());
        this.botPlayer = api.getAuctionPlayer(botUUID);
        
        if (botPlayer == null) {
            logger.warning("Bot player UUID not found in auction system: " + botUUID);
        }
    }
    
    /**
     * Monitors all active auctions and decides whether to create new ones
     */
    public CompletableFuture<Void> monitorAndDecide() {
        return CompletableFuture.runAsync(() -> {
            try {
                List<Auction> allAuctions = api.getPlayerAuctions();
                
                // Convert to our internal data structure
                List<AuctionData> auctionDataList = new ArrayList<>();
                for (Auction auction : allAuctions) {
                    AuctionData data = convertToAuctionData(auction);
                    auctionDataList.add(data);
                    processedAuctions.put(data.getAuctionId(), data);
                }
                
                logger.info(String.format("Analyzing %d active auctions...", auctionDataList.size()));
                
                // Get AI decision
                openAIManager.analyzeMarket(auctionDataList, botPlayer.getName())
                    .thenAccept(this::processAIDecision)
                    .join();
                    
            } catch (Exception e) {
                logger.severe("Error in auction monitoring: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Converts auction to our internal data structure
     */
    private AuctionData convertToAuctionData(Auction auction) {
        ItemStack item = auction.getItem();
        return AuctionData.builder()
            .auctionId(auction.getID())
            .itemName(item.getType().name())
            .displayName(auction.getPrettyItemName(true))
            .amount((int) auction.getItemAmount())
            .price(auction.getPrice())
            .pricePerItem(auction.getPrice() / Math.max(1.0, auction.getItemAmount()))
            .seller(auction.getAuctionPlayer().getName())
            .isBidding(auction.isBidding())
            .timeRemaining(auction.getExpireTime() - System.currentTimeMillis())
            .categories(auction.getAuctionCategories().stream()
                .map(cat -> cat.getName()) // Changed from getCategory() to getName()
                .toArray(String[]::new))
            .build();
    }
    
    /**
     * Processes AI decision and creates auctions accordingly
     */
    private void processAIDecision(AuctionDecision decision) {
        try {
            if (!decision.shouldCreateAuction()) {
                logger.info("AI decided not to create any auctions at this time: " + decision.getReasoning());
                return;
            }
            
            logger.info("AI decision: " + decision.toString());
            
            // Validate decision
            if (!decision.isValid()) {
                logger.warning("Invalid AI decision received");
                return;
            }
            
            // Validate we have the required item
            Material material = Material.matchMaterial(decision.getItemType());
            if (material == null) {
                logger.warning("Unknown material: " + decision.getItemType());
                return;
            }
            
            // Validate item is in available items list
            if (!configManager.getAvailableItems().contains(material.name())) {
                logger.warning(String.format("AI tried to create auction for %s which is not in available-items list! This should not happen.", material.name()));
                return;
            }
            
            // Check if we already have too many of this item listed
            if (hasExcessiveListings(material)) {
                logger.info(String.format("Skipping %s auction - already have enough listings of this item", 
                    material.name()));
                return;
            }
            
            // Check if we have enough items
            if (!hasEnoughItems(material, decision.getQuantity())) {
                logger.warning(String.format("Not enough %s items available (need %d)", 
                    material.name(), decision.getQuantity()));
                return;
            }
            
            // Validate price constraints
            if (decision.getPrice() > configManager.getMaxAuctionPrice()) {
                logger.warning(String.format("Auction price %.2f exceeds maximum allowed %.2f", 
                    decision.getPrice(), configManager.getMaxAuctionPrice()));
                return;
            }
            
            createBotAuction(material, decision);
            
        } catch (Exception e) {
            logger.severe("Error processing AI decision: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates an auction based on AI decision
     */
    private void createBotAuction(Material material, AuctionDecision decision) {
        try {
            if (configManager.isVirtualMode()) {
                // Virtual mode: create items programmatically
                createVirtualAuction(material, decision);
            } else {
                // Standard mode: use existing method
                createStandardAuction(material, decision);
            }
        } catch (Exception e) {
            logger.severe("Error creating bot auction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates an auction for virtual mode (no physical items required)
     */
    private void createVirtualAuction(Material material, AuctionDecision decision) {
        try {
            // Get the default product provider
            AProductProvider<ItemStack> productProvider = (AProductProvider<ItemStack>) api.getDefaultProductProvider();
            
            if (productProvider == null) {
                logger.warning("No default product provider available");
                return;
            }
            
            // Create ItemStack programmatically for virtual mode
            ItemStack itemStack = new ItemStack(material, (int) decision.getQuantity());
            
            // Setup the product using the ItemStack directly
            @SuppressWarnings("unchecked")
            AProduct<ItemStack> product = productProvider.setupProduct((long) decision.getQuantity(), itemStack);
            
            if (product == null) {
                logger.warning("Failed to create product for virtual auction");
                return;
            }
            
            // Create the auction using the direct method
            // Note: Virtual auctions use server default duration settings
            api.createPlayerAuction(
                decision.getPrice(),
                botPlayer,
                product,
                decision.isBidding(),
                auction -> {
                    if (auction != null) {
                        logger.info(String.format(
                            "Successfully created virtual %s auction: %s x%d for %.2f coins (using server default duration, ID: %d)",
                            decision.isBidding() ? "bidding" : "fixed",
                            ItemUtils.formatItemName(material),
                            decision.getQuantity(),
                            decision.getPrice(),
                            auction.getID()
                        ));
                    } else {
                        logger.warning("Failed to create virtual auction - auction is null");
                    }
                }
            );
            
        } catch (Exception e) {
            logger.severe("Error creating virtual auction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates an auction for standard mode (requires physical items)
     */
    private void createStandardAuction(Material material, AuctionDecision decision) {
        try {
            // Get the default product provider
            AProductProvider<?> productProvider = api.getDefaultProductProvider();
            
            if (productProvider == null) {
                logger.warning("No default product provider available");
                return;
            }
            
            // Calculate duration in milliseconds (or null for default)
            Long durationMs = configManager.getAuctionDurationHours() * 60 * 60 * 1000L;
            
            // Create the auction using the safe method
            api.createSafePlayerAuction(
                decision.getPrice(),
                durationMs,
                botPlayer,
                productProvider,
                decision.isBidding(),
                auction -> {
                    if (auction != null) {
                        logger.info(String.format(
                            "Successfully created %s auction: %s x%d for %.2f coins (ID: %d)",
                            decision.isBidding() ? "bidding" : "fixed",
                            ItemUtils.formatItemName(material),
                            decision.getQuantity(),
                            decision.getPrice(),
                            auction.getID()
                        ));
                    } else {
                        logger.warning("Failed to create auction - auction is null");
                    }
                }
            );
            
        } catch (Exception e) {
            logger.severe("Error creating standard auction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if bot has enough items (or if virtual mode is enabled)
     */
    private boolean hasEnoughItems(Material material, long quantity) {
        // If virtual mode is enabled, bypass item checks
        if (configManager.isVirtualMode()) {
            // In virtual mode, check if the item is in the available items list
            List<String> availableItems = configManager.getAvailableItems();
            if (!availableItems.contains(material.name())) {
                logger.warning(String.format("Item %s is not in the available items list", material.name()));
                return false;
            }
            
            // Check reasonable quantity limits to prevent abuse
            if (quantity > 64) {
                logger.warning(String.format("Requested quantity %d exceeds maximum limit of 64", quantity));
                return false;
            }
            
            logger.info(String.format("Virtual mode: Allowing creation of %d x %s", quantity, material.name()));
            return true;
        }
        
        // Non-virtual mode: actual item checking
        // In a real implementation, you would:
        // 1. Check bot's inventory
        // 2. Check chest/storage systems
        // 3. Check item generation capabilities
        // 4. Implement proper item management
        
        // For now, return true for common items
        return ItemUtils.isCommonItem(material) && quantity <= 64;
    }
    
    /**
     * Check if we already have too many listings of a specific item type
     */
    private boolean hasExcessiveListings(Material material) {
        try {
            List<Auction> allAuctions = api.getPlayerAuctions();
            
            // Count how many active auctions the bot has for this specific material
            long botAuctionsForItem = allAuctions.stream()
                .filter(auction -> auction.getAuctionPlayer().getUUID().equals(botPlayer.getUUID()))
                .filter(auction -> !auction.hasExpired())
                .filter(auction -> {
                    ItemStack item = auction.getItem();
                    return item != null && item.getType() == material;
                })
                .count();
            
            // Configure maximum listings per item type (make this configurable later)
            int maxListingsPerItem = configManager.getMaxListingsPerItem();
            
            logger.info(String.format("Bot has %d active listings for %s (max: %d)", 
                botAuctionsForItem, material.name(), maxListingsPerItem));
                
            return botAuctionsForItem >= maxListingsPerItem;
            
        } catch (Exception e) {
            logger.warning("Error checking excessive listings: " + e.getMessage());
            return false; // If we can't check, allow the auction
        }
    }
    
    /**
     * Get current market statistics for reporting
     */
    public Map<String, Object> getMarketStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Auction> allAuctions = api.getPlayerAuctions();
        
        stats.put("total_auctions", allAuctions.size());
        stats.put("bot_auctions", allAuctions.stream()
            .mapToInt(auction -> auction.getUUID().equals(botPlayer.getUUID()) ? 1 : 0)
            .sum());
        stats.put("processed_auctions", processedAuctions.size());
        stats.put("last_check", LocalDateTime.now().toString());
        
        return stats;
    }
    
    /**
     * Cleanup old processed auctions
     */
    public void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        long maxAge = configManager.getDataRetentionTime();
        
        processedAuctions.entrySet().removeIf(entry -> {
            long auctionTime = entry.getValue().getTimeRemaining();
            return (currentTime - auctionTime) > maxAge;
        });
    }
}
