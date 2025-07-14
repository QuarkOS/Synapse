#!/bin/bash

# Synapse Setup Script
# This script helps you set up Synapse for the first time

echo "🧠 Synapse Setup Script"
echo "======================="
echo

# Check if Java is installed
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "✅ Java found: $JAVA_VERSION"
else
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check if Maven is installed  
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✅ Maven found: $MVN_VERSION"
else
    echo "❌ Maven not found. Please install Apache Maven."
    exit 1
fi

echo

# Check if .env file exists
ENV_FILE="src/main/resources/.env"
if [ -f "$ENV_FILE" ]; then
    echo "✅ Configuration file found: $ENV_FILE"
else
    echo "📁 Creating configuration directory..."
    mkdir -p src/main/resources
    
    echo "📝 Creating configuration file from template..."
    cp .env.template "$ENV_FILE"
    
    echo "⚠️  Please edit $ENV_FILE and add your Google API key"
    echo "   You can get an API key from: https://makersuite.google.com/app/apikey"
    echo
fi

# Build the project
echo "🔨 Building Synapse..."
if mvn clean package -q; then
    echo "✅ Build successful!"
    echo
    echo "🚀 Ready to run Synapse!"
    echo "   java -jar target/synapse-1.0-SNAPSHOT.jar"
    echo
    echo "📚 For more information, see:"
    echo "   - README.md for usage instructions"
    echo "   - CONTRIBUTING.md for development guide" 
    echo "   - ROADMAP.md for strategic planning and feature specifications"
else
    echo "❌ Build failed. Please check for errors above."
    exit 1
fi