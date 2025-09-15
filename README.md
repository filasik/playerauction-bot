# AuctionBot

AuctionBot is an intelligent Minecraft plugin that automatically manages auctions in the PlayerAuctions plugin using OpenAI artificial intelligence.

## Features

- 🤖 **AI-driven decision making**: Uses OpenAI GPT to analyze the market and decide on auction creation
- 📊 **Market analysis**: Monitors active auctions and identifies opportunities
- 💰 **Intelligent pricing**: Sets competitive prices with desired profit margins
- ⏰ **Automatic monitoring**: Regularly checks the auction house and reacts to changes
- 🛠️ **Configurable**: Fully customizable settings for different servers

## Requirements

- **Minecraft Server**: Spigot/Paper 1.21+
- **Java**: 17 or higher
- **PlayerAuctions Plugin**: Version 2.2.4+
- **OpenAI API Key**: For AI functionality

## Installation

### 1. Build from source

```bash
# Clone the repository
git clone https://github.com/your-username/auctionbot.git
cd auctionbot

# Install PlayerAuctions API dependency (required!)
# Download the PlayerAuctions plugin JAR and install it to local Maven repository:
mvn install:install-file -Dfile=path/to/PlayerAuctions.jar -DgroupId=com.olziedev -DartifactId=playerauctions-api -Dversion=2.2.4 -Dpackaging=jar

# Build using Maven
mvn clean package

# Or use the build script
./build.sh
```

### 2. Server installation

1. Copy `target/AuctionBot-1.0.0.jar` to your server's `plugins/` folder
2. Make sure you have the PlayerAuctions plugin installed
3. Start the server to generate configuration files
4. Stop the server and edit the configuration

### 3. Configuration

Edit `plugins/AuctionBot/config.yml`:

```yaml
openai:
  api-key: "sk-your-openai-api-key-here"  # Required!
  model: "gpt-3.5-turbo"
  temperature: 0.7

bot:
  player-uuid: "your-bot-player-uuid"     # Required!
  virtual-mode: true                       # Enable virtual mode for bots without physical items
  budget: 10000.0
  min-profit-margin: 15.0
  available-items:
    - "DIAMOND"
    - "EMERALD"
    - "IRON_INGOT"
    # ... more items

monitoring:
  interval-minutes: 30
  debug: false

auction:
  max-price: 5000.0
  duration-hours: 24
  allow-bidding: true
```

### 4. Getting required data

**OpenAI API Key:**
1. Go to https://platform.openai.com/api-keys
2. Sign in or create an account
3. Create a new API key
4. Copy it to the configuration

**Bot Player UUID:**
1. Log in to the server with the player you want to use as a bot
2. Use the command `/auctionbot uuid` (or an online UUID generator)
3. Copy the UUID to the configuration

### 5. Virtual Mode Setup

AuctionBot supports **Virtual Mode** for bot players that will never physically hold items:

**What is Virtual Mode?**
- The bot doesn't check for actual items in the player's inventory
- Perfect for dedicated bot accounts that manage auctions without physical gameplay
- Bot validates items against the `available-items` list in config instead of inventory

**Configuration:**
```yaml
bot:
  virtual-mode: true  # Enable virtual mode
  available-items:    # List items the bot can "sell"
    - "DIAMOND"
    - "EMERALD"
    - "IRON_INGOT"
    # Add more items as needed
```

**Setting up a Virtual Bot:**
1. Create or use a player account dedicated for the bot
2. Get the player's UUID (they don't need to play actively)
3. Set `virtual-mode: true` in config
4. List all items the bot should be able to auction in `available-items`
5. The bot will create auctions for these items without needing physical inventory

## Usage

### Basic commands

- `/auctionbot status` - Show bot status
- `/auctionbot stats` - Show market statistics
- `/auctionbot reload` - Reload configuration
- `/auctionbot uuid` - Show current player's UUID

### Permissions

- `auctionbot.admin` - Access to all commands
- `auctionbot.reload` - Ability to reload configuration

## How it works

1. **Monitoring**: Bot regularly checks active auctions in the auction house
2. **Analysis**: Collected data is sent to OpenAI for analysis
3. **Decision making**: AI analyzes the market and recommends:
   - Whether to create a new auction
   - What item to sell
   - How many items
   - At what price
   - Auction type (fixed price vs. bidding)
4. **Execution**: Bot creates auctions based on AI recommendations
5. **Repeat**: Process repeats according to configured interval

## Security and limits

### Security measures
- Maximum auction price
- Maximum item quantity
- Minimum profit margin
- Budget limits

### Recommendations
- Start with a small budget and test
- Monitor logs for errors
- Regularly check created auctions
- Protect your OpenAI API key

## Troubleshooting

### Common issues

**Bot doesn't create auctions:**
- Check OpenAI API key
- Verify bot player UUID is correct
- Check that bot has available items
- Check logs for errors

**Compilation errors:**
- Make sure you have Java 17+
- Check that Maven is properly installed
- Verify internet connection for downloading dependencies

**OpenAI API errors:**
- Check credit on OpenAI account
- Verify API key is valid
- Check rate limits

### Logs

Plugin logs to:
- Spigot/Paper server logs
- `plugins/AuctionBot/` folder (if debug mode is enabled)

## Development

### Project structure

```
playerauction-bot/
├── src/main/java/me/skerik/auctionbot/
│   ├── AuctionBot.java                    # Main plugin class
│   ├── managers/
│   │   ├── AuctionManager.java           # Auction management
│   │   └── OpenAIManager.java            # OpenAI integration
│   ├── tasks/
│   │   └── AuctionMonitorTask.java       # Monitoring task
│   ├── config/
│   │   └── ConfigManager.java            # Configuration management
│   ├── models/
│   │   ├── AuctionData.java              # Auction data model
│   │   └── AuctionDecision.java          # AI decision model
│   └── utils/
│       └── ItemUtils.java                # Utility functions
├── src/main/resources/
│   ├── plugin.yml                        # Plugin manifest
│   └── config.yml                        # Default configuration
├── pom.xml                               # Maven configuration
└── build.sh                             # Build script
```

### Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes and test
4. Create a pull request

## License

This project is licensed under the MIT License.

## Support

- GitHub Issues: [Report a problem](https://github.com/your-username/auctionbot/issues)
- Discord: [Discord Server](https://discord.gg/yourserver)

## Changelog

### 1.0.0
- Initial release
- OpenAI integration
- Basic auction management
- Configurable settings
