#!/bin/bash

echo "🛑 Stopping Services for Integration Testing"
echo "==========================================="

# Stop and remove DynamoDB Local
echo "📊 Stopping DynamoDB Local..."
if docker ps -q -f name=dynamodb-local | grep -q .; then
    docker stop dynamodb-local
    docker rm dynamodb-local
    echo "✅ DynamoDB Local stopped and removed"
else
    echo "ℹ️  DynamoDB Local is not running"
fi

# Stop and remove Redis
echo "🔴 Stopping Redis..."
if docker ps -q -f name=redis-local | grep -q .; then
    docker stop redis-local
    docker rm redis-local
    echo "✅ Redis stopped and removed"
else
    echo "ℹ️  Redis is not running"
fi

# Clean up DynamoDB data directory
echo "🗂️  Cleaning up DynamoDB data directory..."
DYNAMODB_DATA_DIR="./dynamodb-data"
if [ -d "$DYNAMODB_DATA_DIR" ]; then
    rm -rf "$DYNAMODB_DATA_DIR"
    echo "✅ DynamoDB data directory removed"
else
    echo "ℹ️  DynamoDB data directory not found"
fi

# Check if any containers are still running
echo "🔍 Checking for remaining containers..."
if docker ps -q -f name="dynamodb-local|redis-local" | grep -q .; then
    echo "⚠️  Some containers are still running:"
    docker ps -f name="dynamodb-local|redis-local"
else
    echo "✅ All integration test containers have been stopped"
fi

echo ""
echo "🎉 Services stopped successfully!"
echo ""
echo "💡 To start services again, run:"
echo "   ./start-services.sh" 