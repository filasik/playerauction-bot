#!/bin/bash

# AuctionBot Build Script
# This script builds the AuctionBot plugin using Maven

echo "Starting AuctionBot build..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven first: https://maven.apache.org/install.html"
    exit 1
fi

# Check if PlayerAuctions API is installed in local Maven repository
echo "Checking PlayerAuctions API dependency..."
if ! mvn dependency:get -Dartifact=com.olziedev:playerauctions-api:2.2.4 -q >/dev/null 2>&1; then
    echo "Warning: PlayerAuctions API not found in local Maven repository"
    echo "You need to install it manually:"
    echo "1. Download PlayerAuctions plugin JAR file"
    echo "2. Run: mvn install:install-file -Dfile=path/to/PlayerAuctions.jar -DgroupId=com.olziedev -DartifactId=playerauctions-api -Dversion=2.2.4 -Dpackaging=jar"
    echo ""
    echo "Continue anyway? (y/N)"
    read -r response
    if [[ ! "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        exit 1
    fi
fi

# Navigate to the project directory
cd "$(dirname "$0")"

echo "Cleaning previous builds..."
mvn clean

echo "Compiling and packaging..."
mvn package

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📦 Plugin JAR created: target/AuctionBot-1.0.0.jar"
    echo ""
    echo "Installation instructions:"
    echo "1. Copy target/AuctionBot-1.0.0.jar to your server's plugins/ folder"
    echo "2. Make sure PlayerAuctions plugin is installed"
    echo "3. Start your server to generate configuration files"
    echo "4. Edit plugins/AuctionBot/config.yml with your settings"
    echo "5. Restart your server"
    echo ""
    echo "Configuration required:"
    echo "- Set your OpenAI API key in config.yml"
    echo "- Set your bot player UUID in config.yml"
    echo "- Configure available items and budget"
else
    echo ""
    echo "❌ Build failed!"
    echo "Check the output above for error details"
    exit 1
fi
