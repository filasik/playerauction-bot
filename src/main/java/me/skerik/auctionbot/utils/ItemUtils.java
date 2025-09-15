package me.skerik.auctionbot.utils;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility functions for item management
 */
public class ItemUtils {
    
    // Common items that are typically easy to obtain/generate
    private static final Set<Material> COMMON_ITEMS = new HashSet<>(Arrays.asList(
        Material.COBBLESTONE,
        Material.DIRT,
        Material.SAND,
        Material.GRAVEL,
        Material.OAK_LOG,
        Material.SPRUCE_LOG,
        Material.BIRCH_LOG,
        Material.JUNGLE_LOG,
        Material.ACACIA_LOG,
        Material.DARK_OAK_LOG,
        Material.WHEAT,
        Material.POTATO,
        Material.CARROT,
        Material.BEETROOT,
        Material.SUGAR_CANE,
        Material.MELON,
        Material.PUMPKIN,
        Material.IRON_INGOT,
        Material.COAL,
        Material.STRING,
        Material.LEATHER,
        Material.BEEF,
        Material.PORKCHOP,
        Material.CHICKEN,
        Material.MUTTON,
        Material.RABBIT,
        Material.COD,
        Material.SALMON,
        Material.WHITE_WOOL
    ));
    
    // Valuable items that are worth more on the market
    private static final Set<Material> VALUABLE_ITEMS = new HashSet<>(Arrays.asList(
        Material.DIAMOND,
        Material.EMERALD,
        Material.GOLD_INGOT,
        Material.NETHERITE_INGOT,
        Material.ANCIENT_DEBRIS,
        Material.NETHER_STAR,
        Material.DRAGON_EGG,
        Material.ELYTRA,
        Material.TOTEM_OF_UNDYING,
        Material.ENCHANTED_GOLDEN_APPLE,
        Material.HEART_OF_THE_SEA,
        Material.NAUTILUS_SHELL,
        Material.SHULKER_SHELL,
        Material.PHANTOM_MEMBRANE,
        Material.BLAZE_ROD,
        Material.GHAST_TEAR,
        Material.ENDER_PEARL,
        Material.WITHER_SKELETON_SKULL
    ));
    
    /**
     * Checks if an item is commonly available
     */
    public static boolean isCommonItem(Material material) {
        return COMMON_ITEMS.contains(material);
    }
    
    /**
     * Checks if an item is valuable/rare
     */
    public static boolean isValuableItem(Material material) {
        return VALUABLE_ITEMS.contains(material);
    }
    
    /**
     * Gets the estimated base value of an item
     */
    public static double getEstimatedValue(Material material) {
        if (isValuableItem(material)) {
            return getValuableItemValue(material);
        } else if (isCommonItem(material)) {
            return getCommonItemValue(material);
        } else {
            // Default value for unknown items
            return 10.0;
        }
    }
    
    /**
     * Gets estimated value for valuable items
     */
    private static double getValuableItemValue(Material material) {
        return switch (material) {
            case DIAMOND -> 100.0;
            case EMERALD -> 50.0;
            case GOLD_INGOT -> 25.0;
            case NETHERITE_INGOT -> 1000.0;
            case ANCIENT_DEBRIS -> 500.0;
            case NETHER_STAR -> 2000.0;
            case DRAGON_EGG -> 10000.0;
            case ELYTRA -> 5000.0;
            case TOTEM_OF_UNDYING -> 1500.0;
            case ENCHANTED_GOLDEN_APPLE -> 800.0;
            case HEART_OF_THE_SEA -> 300.0;
            case NAUTILUS_SHELL -> 150.0;
            case SHULKER_SHELL -> 200.0;
            case PHANTOM_MEMBRANE -> 75.0;
            case BLAZE_ROD -> 40.0;
            case GHAST_TEAR -> 60.0;
            case ENDER_PEARL -> 30.0;
            case WITHER_SKELETON_SKULL -> 500.0;
            default -> 100.0;
        };
    }
    
    /**
     * Gets estimated value for common items
     */
    private static double getCommonItemValue(Material material) {
        return switch (material) {
            case IRON_INGOT -> 15.0;
            case COAL -> 2.0;
            case COBBLESTONE, DIRT, SAND, GRAVEL -> 0.5;
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG -> 3.0;
            case WHEAT, POTATO, CARROT, BEETROOT -> 1.5;
            case SUGAR_CANE -> 2.0;
            case MELON, PUMPKIN -> 1.0;
            case STRING -> 1.0;
            case LEATHER -> 3.0;
            case BEEF, PORKCHOP, CHICKEN, MUTTON, RABBIT -> 2.5;
            case COD, SALMON -> 2.0;
            case WHITE_WOOL -> 1.5;
            default -> 1.0;
        };
    }
    
    /**
     * Formats an item name for display
     */
    public static String formatItemName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1));
        }
        
        return formatted.toString();
    }
    
    /**
     * Checks if an item is stackable
     */
    public static boolean isStackable(Material material) {
        return material.getMaxStackSize() > 1;
    }
    
    /**
     * Gets the maximum stack size for an item
     */
    public static int getMaxStackSize(Material material) {
        return material.getMaxStackSize();
    }
    
    /**
     * Validates if a quantity is reasonable for the given material
     */
    public static boolean isValidQuantity(Material material, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        
        // For non-stackable items, only allow quantity of 1
        if (!isStackable(material)) {
            return quantity == 1;
        }
        
        // For stackable items, respect the stack size limit
        return quantity <= getMaxStackSize(material);
    }
}
