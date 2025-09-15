package me.skerik.auctionbot.models;

/**
 * Represents auction data for analysis
 */
public class AuctionData {
    
    private final long auctionId;
    private final String itemName;
    private final String displayName;
    private final int amount;
    private final double price;
    private final double pricePerItem;
    private final String seller;
    private final boolean isBidding;
    private final long timeRemaining;
    private final String[] categories;
    
    private AuctionData(Builder builder) {
        this.auctionId = builder.auctionId;
        this.itemName = builder.itemName;
        this.displayName = builder.displayName;
        this.amount = builder.amount;
        this.price = builder.price;
        this.pricePerItem = builder.pricePerItem;
        this.seller = builder.seller;
        this.isBidding = builder.isBidding;
        this.timeRemaining = builder.timeRemaining;
        this.categories = builder.categories;
    }
    
    // Getters
    public long getAuctionId() { return auctionId; }
    public String getItemName() { return itemName; }
    public String getDisplayName() { return displayName; }
    public int getAmount() { return amount; }
    public double getPrice() { return price; }
    public double getPricePerItem() { return pricePerItem; }
    public String getSeller() { return seller; }
    public boolean isBidding() { return isBidding; }
    public long getTimeRemaining() { return timeRemaining; }
    public String[] getCategories() { return categories; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private long auctionId;
        private String itemName;
        private String displayName;
        private int amount;
        private double price;
        private double pricePerItem;
        private String seller;
        private boolean isBidding;
        private long timeRemaining;
        private String[] categories;
        
        public Builder auctionId(long auctionId) {
            this.auctionId = auctionId;
            return this;
        }
        
        public Builder itemName(String itemName) {
            this.itemName = itemName;
            return this;
        }
        
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder price(double price) {
            this.price = price;
            return this;
        }
        
        public Builder pricePerItem(double pricePerItem) {
            this.pricePerItem = pricePerItem;
            return this;
        }
        
        public Builder seller(String seller) {
            this.seller = seller;
            return this;
        }
        
        public Builder isBidding(boolean isBidding) {
            this.isBidding = isBidding;
            return this;
        }
        
        public Builder timeRemaining(long timeRemaining) {
            this.timeRemaining = timeRemaining;
            return this;
        }
        
        public Builder categories(String[] categories) {
            this.categories = categories;
            return this;
        }
        
        public AuctionData build() {
            return new AuctionData(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("AuctionData{id=%d, item='%s', amount=%d, price=%.2f, seller='%s'}",
            auctionId, itemName, amount, price, seller);
    }
}
