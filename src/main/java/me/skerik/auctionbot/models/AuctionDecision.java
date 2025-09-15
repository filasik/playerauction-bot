package me.skerik.auctionbot.models;

/**
 * Represents an AI decision about auction creation
 */
public class AuctionDecision {
    
    private final boolean shouldCreateAuction;
    private final String itemType;
    private final int quantity;
    private final double price;
    private final boolean bidding;
    private final String reasoning;
    
    private AuctionDecision(boolean shouldCreateAuction, String itemType, int quantity, 
                           double price, boolean bidding, String reasoning) {
        this.shouldCreateAuction = shouldCreateAuction;
        this.itemType = itemType;
        this.quantity = quantity;
        this.price = price;
        this.bidding = bidding;
        this.reasoning = reasoning;
    }
    
    /**
     * Creates a decision to create an auction
     */
    public static AuctionDecision createAuction(String itemType, int quantity, double price, 
                                               boolean bidding, String reasoning) {
        return new AuctionDecision(true, itemType, quantity, price, bidding, reasoning);
    }
    
    /**
     * Creates a decision to not create any auction
     */
    public static AuctionDecision noAction(String reasoning) {
        return new AuctionDecision(false, null, 0, 0.0, false, reasoning);
    }
    
    // Getters
    public boolean shouldCreateAuction() { return shouldCreateAuction; }
    public String getItemType() { return itemType; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public boolean isBidding() { return bidding; }
    public String getReasoning() { return reasoning; }
    
    /**
     * Validates the decision parameters
     */
    public boolean isValid() {
        if (!shouldCreateAuction) {
            return true; // No action decisions are always valid
        }
        
        return itemType != null && !itemType.trim().isEmpty() &&
               quantity > 0 && quantity <= 64 &&
               price > 0 && price <= 1000000;
    }
    
    @Override
    public String toString() {
        if (!shouldCreateAuction) {
            return String.format("AuctionDecision{action=WAIT, reasoning='%s'}", reasoning);
        }
        
        return String.format("AuctionDecision{action=CREATE, item='%s', qty=%d, price=%.2f, bidding=%s, reasoning='%s'}",
            itemType, quantity, price, bidding, reasoning);
    }
}
