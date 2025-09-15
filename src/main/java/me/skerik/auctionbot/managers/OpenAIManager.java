package me.skerik.auctionbot.managers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.skerik.auctionbot.config.ConfigManager;
import me.skerik.auctionbot.models.AuctionData;
import me.skerik.auctionbot.models.AuctionDecision;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OpenAIManager {
    
    private final ConfigManager configManager;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Logger logger;
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    public OpenAIManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
        this.logger = configManager.getPlugin().getLogger();
    }
    
    public CompletableFuture<AuctionDecision> analyzeMarket(List<AuctionData> marketData, String botPlayerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = buildMarketAnalysisPrompt(marketData, botPlayerName);
                String response = callOpenAI(prompt);
                return parseAIResponse(response);
            } catch (Exception e) {
                logger.severe("Error during OpenAI analysis: " + e.getMessage());
                return AuctionDecision.noAction("Error occurred during analysis: " + e.getMessage());
            }
        });
    }
    
    private String buildMarketAnalysisPrompt(List<AuctionData> marketData, String botPlayerName) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Analyze this Minecraft auction market data and decide whether to create a new auction or wait:\n\n");
        
        Map<String, List<AuctionData>> itemGroups = marketData.stream()
            .collect(Collectors.groupingBy(AuctionData::getItemName));
        
        prompt.append("Current Market Status:\n");
        if (marketData.isEmpty()) {
            prompt.append("- No active auctions found\n");
        } else {
            // Debug: Log what items are actually found
            logger.info("DEBUG: Found items on market: " + itemGroups.keySet());
            
            for (Map.Entry<String, List<AuctionData>> entry : itemGroups.entrySet()) {
                String itemType = entry.getKey();
                List<AuctionData> items = entry.getValue();
                
                double avgPrice = items.stream()
                    .mapToDouble(AuctionData::getPricePerItem)
                    .average()
                    .orElse(0.0);
                
                int totalQuantity = items.stream()
                    .mapToInt(AuctionData::getAmount)
                    .sum();
                
                prompt.append(String.format("- %s: %d auctions, %d total items, avg price: %.2f coins/item\n",
                    itemType, items.size(), totalQuantity, avgPrice));
                
                items.stream().limit(3).forEach(data -> 
                    prompt.append(String.format("  * %dx %s @ %.2f/item by %s (%s)\n",
                        data.getAmount(),
                        data.getItemName(), 
                        data.getPricePerItem(),
                        data.getSeller(),
                        data.isBidding() ? "Bidding" : "Fixed"
                    ))
                );
            }
        }
        
        prompt.append("\nBot Configuration:\n");
        prompt.append("- Budget: ").append(configManager.getBotBudget()).append(" coins\n");
        prompt.append("- Available items: ").append(String.join(", ", configManager.getAvailableItems())).append("\n");
        prompt.append("- Min profit margin: ").append(configManager.getMinProfitMargin()).append("%\n");
        prompt.append("- MAX LISTINGS PER ITEM: ").append(configManager.getMaxListingsPerItem()).append(" (CRITICAL LIMIT!)\n");
        
        // Add current bot auction status for each available item
        prompt.append("\nCURRENT BOT AUCTION STATUS:\n");
        List<String> availableItems = configManager.getAvailableItems();
        
        for (String availableItem : availableItems) {
            // Count bot's current auctions for this item (only bot's auctions!)
            long currentListings = itemGroups.entrySet().stream()
                .filter(entry -> entry.getKey().equals(availableItem))
                .mapToLong(entry -> entry.getValue().stream()
                    .filter(auctionData -> botPlayerName.equals(auctionData.getSeller()))
                    .count())
                .sum();
            
            // Debug: Log market vs bot status
            long totalMarketListings = itemGroups.entrySet().stream()
                .filter(entry -> entry.getKey().equals(availableItem))
                .mapToLong(entry -> entry.getValue().size())
                .sum();
            
            logger.info(String.format("DEBUG: %s - Market: %d auctions, Bot: %d auctions", 
                availableItem, totalMarketListings, currentListings));
            
            String status = currentListings >= configManager.getMaxListingsPerItem() ? "FULL" : "AVAILABLE";
            prompt.append(String.format("- %s: %d/%d listings (%s) [Market total: %d auctions]\n", 
                availableItem, currentListings, configManager.getMaxListingsPerItem(), status, totalMarketListings));
        }
        
        prompt.append("\nDECISION RULES:\n");
        prompt.append("1. NEVER create auction for items marked as FULL\n");
        prompt.append("2. ONLY create auctions for items in available-items list: ").append(String.join(", ", configManager.getAvailableItems())).append("\n");
        prompt.append("3. PREFER items with 0 listings (new market opportunities)\n");
        prompt.append("4. Consider items with status AVAILABLE but not FULL\n");
        prompt.append("5. Ensure minimum profit margin\n");
        prompt.append("6. Price competitively based on existing market data\n");
        prompt.append("7. CRITICAL: itemType MUST be from available-items list, no exceptions!\n");
        
        prompt.append("\nRespond with a JSON object containing:\n");
        prompt.append("- 'action': 'create' or 'wait'\n");
        prompt.append("- 'itemType': Minecraft material name (e.g., 'DIAMOND', 'IRON_INGOT')\n");
        prompt.append("- 'quantity': Number of items (1-64)\n");
        prompt.append("- 'price': Total price for the auction (MUST BE A NUMBER, NO MATH EXPRESSIONS!)\n");
        prompt.append("- 'bidding': true/false for auction type\n");
        prompt.append("- 'reasoning': Explanation of your decision\n");
        prompt.append("\nIMPORTANT: price MUST be a calculated number, NOT a math expression like '1.41 * 32 * 1.15'\n");
        prompt.append("Calculate the final price yourself and provide only the result number!\n");
        prompt.append("\nSTRATEGY: Analyze BOTH market presence AND bot status carefully!\n");
        prompt.append("- If market shows auctions for an item, it HAS market presence\n");
        prompt.append("- Only create auctions for items with AVAILABLE status (not FULL)\n");
        prompt.append("- Consider competitive pricing based on existing market data\n");
        prompt.append("\nExamples:\n");
        prompt.append("- Gap opportunity: {\"action\": \"create\", \"itemType\": \"WHEAT\", \"quantity\": 64, \"price\": 320.0, \"bidding\": false, \"reasoning\": \"WHEAT has no current market presence - opportunity to establish pricing without competition\"}\n");
        prompt.append("- Existing market: {\"action\": \"create\", \"itemType\": \"DIAMOND\", \"quantity\": 8, \"price\": 1200.0, \"bidding\": true, \"reasoning\": \"Diamonds are in high demand with limited supply\"}");
        
        return prompt.toString();
    }
    
    private String callOpenAI(String prompt) throws IOException, InterruptedException {
        String apiKey = configManager.getOpenAIApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API key not configured");
        }
        
        String requestBody = "{"
            + "\"model\": \"gpt-3.5-turbo\","
            + "\"messages\": ["
            + "{"
            + "\"role\": \"system\","
            + "\"content\": \"You are an expert Minecraft auction bot. Analyze market data and make strategic auction decisions. Always respond with valid JSON.\""
            + "},"
            + "{"
            + "\"role\": \"user\","
            + "\"content\": \"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\""
            + "}"
            + "],"
            + "\"max_tokens\": 500,"
            + "\"temperature\": 0.7"
            + "}";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OPENAI_API_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(30))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("OpenAI API request failed with status: " + response.statusCode() + 
                                ", response: " + response.body());
        }
        
        return response.body();
    }
    
    private AuctionDecision parseAIResponse(String response) throws IOException {
        JsonNode rootNode = objectMapper.readTree(response);
        
        JsonNode choicesNode = rootNode.get("choices");
        if (choicesNode == null || !choicesNode.isArray() || choicesNode.size() == 0) {
            throw new IOException("Invalid OpenAI response format");
        }
        
        JsonNode messageNode = choicesNode.get(0).get("message");
        if (messageNode == null) {
            throw new IOException("No message in OpenAI response");
        }
        
        String content = messageNode.get("content").asText();
        logger.info("OpenAI response: " + content);
        
        String jsonString = extractJsonFromResponse(content);
        JsonNode decisionNode = objectMapper.readTree(jsonString);
        
        String action = decisionNode.get("action").asText();
        String itemType = decisionNode.has("itemType") ? decisionNode.get("itemType").asText() : "";
        int quantity = decisionNode.has("quantity") ? decisionNode.get("quantity").asInt() : 0;
        double price = decisionNode.has("price") ? decisionNode.get("price").asDouble() : 0.0;
        boolean bidding = decisionNode.has("bidding") ? decisionNode.get("bidding").asBoolean() : false;
        String reasoning = decisionNode.has("reasoning") ? decisionNode.get("reasoning").asText() : "";
        
        if ("create".equals(action)) {
            return AuctionDecision.createAuction(itemType, quantity, price, bidding, reasoning);
        } else {
            return AuctionDecision.noAction(reasoning);
        }
    }
    
    private String extractJsonFromResponse(String response) {
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        
        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            throw new IllegalArgumentException("No valid JSON found in response: " + response);
        }
        
        String jsonString = response.substring(startIndex, endIndex + 1);
        
        // Clean up any mathematical expressions in the JSON
        jsonString = jsonString.replaceAll("\"price\"\\s*:\\s*[0-9.]+\\s*\\*\\s*[0-9.]+\\s*\\*\\s*[0-9.]+", 
            "\"price\": 0.0")
            .replaceAll("\"price\"\\s*:\\s*[0-9.]+\\s*\\*\\s*[0-9.]+", 
            "\"price\": 0.0");
        
        return jsonString;
    }
    
    public boolean testConnection() {
        try {
            String testPrompt = "Respond with this exact JSON: {\"status\": \"ok\", \"message\": \"test successful\"}";
            String response = callOpenAI(testPrompt);
            return response.contains("test successful");
        } catch (Exception e) {
            logger.warning("OpenAI connection test failed: " + e.getMessage());
            return false;
        }
    }
}
