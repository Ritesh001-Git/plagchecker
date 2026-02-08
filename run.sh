#!/bin/bash

# AI Content Detector - Build and Run Script

set -e  # Exit on error

echo "🚀 AI Content Detector - Build & Run"
echo "===================================="

# Check Java version
echo "📋 Checking Java version..."
java -version 2>&1 | head -n 1

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Error: Java 17 or higher required. Found: $JAVA_VERSION"
    exit 1
fi
echo "✅ Java version OK"

# Check Maven
echo ""
echo "📋 Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven not found. Please install Maven 3.6+"
    exit 1
fi
mvn -version | head -n 1
echo "✅ Maven OK"

# Clean and build
echo ""
echo "🔨 Building project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi
echo "✅ Build successful"

# Run tests (optional)
if [ "$1" == "--test" ]; then
    echo ""
    echo "🧪 Running tests..."
    mvn test
fi

# Start server
echo ""
echo "🚀 Starting server..."
echo "📡 Server will be available at http://localhost:8080"
echo "🌐 Browser will open automatically"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

mvn exec:java -Dexec.mainClass="com.detector.server.MiniServer"
