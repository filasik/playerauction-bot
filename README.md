# AuctionBot

An intelligent Minecraft plugin that automatically manages auctions using PlayerAuctions API and OpenAI artificial intelligence for smart market analysis and decision making.

## 🚀 Features

- 🤖 **AI-Powered Market Analysis**: Uses OpenAI GPT-4o-mini to analyze auction market trends and make intelligent decisions
- 📊 **Smart Market Monitoring**: Continuously monitors active auctions and identifies profitable opportunities
- 💰 **Intelligent Pricing**: Automatically sets competitive prices with configurable profit margins
- 🎯 **Market Saturation Control**: Prevents flooding the market by limiting listings per item type
- ⚙️ **Virtual Mode**: Can operate without physical items in inventory for testing and virtual economies
- 🔄 **Automatic Management**: Runs continuously with configurable monitoring intervals
- � **Comprehensive Logging**: Detailed debug information for market analysis and bot decisions
- 🛡️ **Safe Operation**: Built-in safeguards to prevent excessive spending and invalid auctions

## 📋 Requirements

- **Minecraft Server**: Spigot/Paper 1.21+
- **Java**: 17 or higher
- **PlayerAuctions Plugin**: Version 2.2.4+
- **OpenAI API Key**: For AI functionality ([Get one here](https://platform.openai.com/api-keys))

## 🔧 Installation

### 1. Prerequisites

First, install the PlayerAuctions plugin on your server and ensure it's working properly.

### 2. Build from Source

```bash
# Clone the repository
git clone https://github.com/your-username/playerauction-bot.git
cd playerauction-bot

# Install PlayerAuctions API dependency (REQUIRED!)
# Download the PlayerAuctions plugin JAR and install it to local Maven repository:
mvn install:install-file -Dfile=path/to/PlayerAuctions.jar -DgroupId=com.olziedev -DartifactId=playerauctions-api -Dversion=2.2.4 -Dpackaging=jar

# Build the plugin
mvn clean package
```

### 3. Server Installation

1. Copy `target/AuctionBot-1.0.0.jar` to your server's `plugins/` folder
2. Start the server to generate configuration files
3. Stop the server and configure the plugin

## ⚙️ Configuration

Edit `plugins/AuctionBot/config.yml`:

```yaml
# AuctionBot Configuration File
openai:
  # Your OpenAI API key (REQUIRED!)
  # Get one from: https://platform.openai.com/api-keys
  api-key: "sk-your-openai-api-key-here"
  
  # OpenAI model to use (recommended: gpt-4o-mini for better analysis)
  model: "gpt-4o-mini"
  
  # Temperature for AI responses (0.0 = deterministic, 1.0 = creative)
  temperature: 0.7
  
  # Maximum tokens for AI responses
  max-tokens: 1000

bot:
  # UUID of the player account that will create auctions (REQUIRED!)
  # Use /auctionbot uuid to get your UUID
  player-uuid: "00000000-0000-0000-0000-000000000000"
  
  # Virtual mode: If true, bot doesn't check for actual items in inventory
  # Perfect for dedicated bot accounts
  virtual-mode: true
  
  # Bot's maximum budget for creating auctions
  budget: 10000.0
  
  # Minimum profit margin percentage
  min-profit-margin: 15.0
  
  # Maximum listings per item type (prevents market flooding)
  max-listings-per-item: 2
  
  # Items the bot can auction (add/remove as needed)
  available-items:
    - "DIAMOND"
    - "EMERALD"
    - "IRON_INGOT"
    - "GOLD_INGOT"
    - "COAL"
    - "WHEAT"
    - "POTATO"
    - "CARROT"
    - "BEEF"
    - "PORKCHOP"
    - "CHICKEN"
    - "OAK_LOG"
    - "SPRUCE_LOG"
    - "BIRCH_LOG"
    - "COBBLESTONE"
    - "DIRT"
    - "SAND"
    - "GRAVEL"
    - "BREAD"
    - "STONE"
    - "DIAMOND_SWORD"
    - "IRON_PICKAXE"

# Monitoring Configuration
monitoring:
  # How often to check the auction house (in minutes)
  interval-minutes: 30
  
  # How long to keep auction data in memory (in hours)
  data-retention-hours: 24
  
  # Enable debug logging for troubleshooting
  debug: false

# Auction Settings
auction:
  # Maximum price the bot is allowed to set for any auction
  max-price: 5000.0
  
  # Maximum quantity the bot can auction at once
  max-quantity: 64
  
  # Default auction duration (in hours)
  # Note: Virtual auctions use server default duration
  duration-hours: 24
  
  # Whether to allow the bot to create bidding auctions
  allow-bidding: true

# Advanced Settings
advanced:
  # Retry attempts for failed API calls
  max-retries: 3
```

## 🔑 Setup Guide

### 1. Get OpenAI API Key
1. Visit [OpenAI Platform](https://platform.openai.com/api-keys)
2. Sign in or create an account
3. Create a new API key
4. Copy it to your configuration file

### 2. Get Bot Player UUID
**Option A: In-game command**
```
/auctionbot uuid
```

**Option B: Online tool**
1. Visit [NameMC](https://namemc.com) or [UUID Generator](https://www.uuidgenerator.net/version4)
2. Enter your player name
3. Copy the UUID to configuration

### 3. Virtual Mode Setup

**What is Virtual Mode?**
Virtual Mode allows the bot to operate without requiring physical items in inventory. Perfect for:
- Dedicated bot accounts
- Testing environments
- Virtual economies
- Servers where bots don't physically play

**How to Enable:**
```yaml
bot:
  virtual-mode: true
  available-items:
    - "DIAMOND"
    - "EMERALD"
    # Add items the bot should be able to auction
```

The bot will validate auctions against this list instead of checking inventory.

## 🎮 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/auctionbot status` | `auctionbot.admin` | Show bot status and current configuration |
| `/auctionbot stats` | `auctionbot.admin` | Display market statistics and bot performance |
| `/auctionbot reload` | `auctionbot.reload` | Reload configuration from file |
| `/auctionbot uuid` | `auctionbot.admin` | Show current player's UUID |

## 🔍 How It Works

### 1. Market Monitoring
- Bot regularly scans all active auctions
- Collects data: item types, quantities, prices, sellers, time remaining
- Tracks bot's own auctions to respect limits

### 2. AI Analysis
The collected market data is sent to OpenAI with:
- Current market conditions for each item
- Bot's current auction status
- Available budget and constraints
- Market saturation information

### 3. Intelligent Decision Making
AI analyzes the data and decides:
- **Whether to create an auction** (based on market gaps and profitability)
- **What item to sell** (from available-items list)
- **Quantity** (considering market demand and stack sizes)
- **Price** (competitive pricing with profit margin)
- **Auction type** (fixed price vs bidding)

### 4. Execution
- Bot validates the AI decision against safety constraints
- Creates the auction using PlayerAuctions API
- Logs the transaction for monitoring

### 5. Safety Controls
- Respects `max-listings-per-item` to prevent market flooding
- Validates prices against `max-price` setting
- Ensures sufficient budget before creating auctions
- Only auctions items from `available-items` list

## 🛡️ Security & Safeguards

### Built-in Safety Features
- **Budget Control**: Bot respects maximum budget settings to prevent overspending
- **Price Limits**: Maximum auction price (`max-price`) prevents unrealistic pricing
- **Quantity Limits**: Maximum quantity per auction (`max-quantity`) for reasonable stack sizes
- **Market Saturation Protection**: `max-listings-per-item` prevents flooding market with same items
- **Whitelist Validation**: Only items in `available-items` list can be auctioned
- **Profit Margin Enforcement**: Ensures minimum profit margin before creating auctions
- **API Rate Limiting**: Built-in delays and retry mechanisms for OpenAI API calls

### Best Practices
- **Start Small**: Begin with a low budget (e.g., 1000 coins) for testing
- **Monitor Regularly**: Check bot behavior and logs for the first few days
- **Secure API Key**: Keep your OpenAI API key private and rotate it periodically
- **Gradual Expansion**: Add items to `available-items` gradually as you gain confidence
- **Regular Backups**: Backup your configuration and any important data

## 🐛 Troubleshooting

### Common Issues

**🚫 Bot doesn't create any auctions**
```
Possible causes:
✓ Check OpenAI API key is valid and has credit
✓ Verify bot player UUID is correct
✓ Ensure available-items list is not empty
✓ Check if max-listings-per-item limit is reached
✓ Verify virtual-mode is enabled if bot has no inventory
✓ Check server logs for error messages
```

**💰 "Insufficient budget" errors**
```
Solutions:
✓ Increase bot.budget in configuration
✓ Check current market prices aren't too high
✓ Lower max-price if needed
✓ Review profit margin settings
```

**🔑 OpenAI API errors**
```
Common fixes:
✓ Verify API key is correct and active
✓ Check OpenAI account has sufficient credits
✓ Ensure API key has proper permissions
✓ Try using gpt-4o-mini model (more cost-effective)
```

**🏗️ Compilation errors**
```
Requirements check:
✓ Java 17 or higher installed
✓ Maven properly configured
✓ PlayerAuctions API dependency installed correctly
✓ All imports and dependencies resolved
```

**⚠️ Plugin won't load**
```
Verification steps:
✓ PlayerAuctions plugin is installed and working
✓ Server is running Spigot/Paper 1.21+
✓ AuctionBot JAR is in plugins folder
✓ No conflicting plugins installed
✓ Check server startup logs for errors
```

### Debug Mode

Enable detailed logging by setting `monitoring.debug: true`:

```yaml
monitoring:
  debug: true
```

This provides verbose information about:
- Market analysis data sent to OpenAI
- AI decision-making process
- Auction creation attempts
- Error details and stack traces

### Log Analysis

**Important log messages:**
- `AI decided to create auction` - Bot is creating an auction
- `Skipping auction - already have enough listings` - Market saturation protection active
- `Not enough budget` - Budget limit reached
- `Invalid AI decision received` - OpenAI returned malformed response

## 📊 Performance & Monitoring

### Key Metrics to Monitor
- **Successful auctions created per day**
- **Average profit margin achieved**
- **Market saturation levels** (items at max listings)
- **OpenAI API costs and usage**
- **Bot's success rate** (created vs attempted auctions)

### Optimization Tips
- **Adjust monitoring interval**: Lower `interval-minutes` for more active markets
- **Fine-tune available items**: Remove unprofitable items from the list
- **Optimize profit margins**: Balance competitiveness with profitability
- **Monitor competitor behavior**: Adjust strategy based on other players' auction patterns

## 🔄 Updates & Maintenance

### Regular Maintenance
1. **Update OpenAI model**: Consider upgrading to newer models when available
2. **Review item profitability**: Remove items that consistently don't sell
3. **Adjust price strategies**: Update profit margins based on market changes
4. **Monitor API costs**: Track OpenAI usage and optimize model settings
5. **Check for plugin updates**: Keep both AuctionBot and PlayerAuctions updated

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Install PlayerAuctions API dependency
3. Build with Maven: `mvn clean package`
4. Test on a development server

### Bug Reports
Please include:
- Server version (Spigot/Paper)
- Plugin versions (AuctionBot, PlayerAuctions)
- Configuration file (remove API keys)
- Error logs and stack traces
- Steps to reproduce the issue

## ⚠️ Disclaimer

- This bot modifies your server's auction house automatically
- Monitor its behavior carefully, especially in the beginning
- The AI makes decisions based on market data but cannot predict all market conditions
- Always test thoroughly on a development server first
- Use at your own risk - the authors are not responsible for any economic impact

---

**OpenAI API errors:**
- Check credit on OpenAI account
- Verify API key is valid
- Check rate limits

### Logs

Plugin logs to:
- Spigot/Paper server logs
- `plugins/AuctionBot/` folder (if debug mode is enabled)

### Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes and test
4. Create a pull request

## Support

- GitHub Issues: [Report a problem](https://github.com/your-username/auctionbot/issues)

## Changelog

### 1.0.0
- Initial release
- OpenAI integration
- Basic auction management
- Configurable settings
