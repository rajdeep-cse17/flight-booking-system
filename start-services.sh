#!/bin/bash

echo "🚀 Starting Services for Manual Integration Testing"
echo "=================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

echo "✅ Docker is running"

# Create DynamoDB data directory with proper permissions
echo "📁 Creating DynamoDB data directory..."
DYNAMODB_DATA_DIR="./dynamodb-data"
mkdir -p "$DYNAMODB_DATA_DIR"
chmod 755 "$DYNAMODB_DATA_DIR"

# Stop and remove existing containers if they exist
echo "🧹 Cleaning up existing containers..."
docker stop dynamodb-local redis-local 2>/dev/null || true
docker rm dynamodb-local redis-local 2>/dev/null || true

# Start DynamoDB Local with proper volume mounting
echo "📊 Starting DynamoDB Local..."
docker run -d --name dynamodb-local \
    -p 8000:8000 \
    -v "$(pwd)/$DYNAMODB_DATA_DIR:/home/dynamodblocal/data" \
    amazon/dynamodb-local:latest \
    -jar DynamoDBLocal.jar -sharedDb -dbPath /home/dynamodblocal/data

if [ $? -eq 0 ]; then
    echo "✅ DynamoDB Local started on port 8000"
else
    echo "❌ Failed to start DynamoDB Local"
    exit 1
fi

# Start Redis
echo "🔴 Starting Redis..."
docker run -d --name redis-local \
    -p 6379:6379 \
    redis:7-alpine

if [ $? -eq 0 ]; then
    echo "✅ Redis started on port 6379"
else
    echo "❌ Failed to start Redis"
    exit 1
fi

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check service health
echo "🔍 Checking service health..."

# Check DynamoDB
echo "📊 Checking DynamoDB Local..."
if curl -s http://localhost:8000/shell > /dev/null; then
    echo "✅ DynamoDB Local is responding"
else
    echo "❌ DynamoDB Local is not responding"
    echo "🔍 Checking DynamoDB logs..."
    docker logs dynamodb-local
fi

# Check Redis
echo "🔴 Checking Redis..."
if docker exec redis-local redis-cli ping 2>/dev/null | grep -q "PONG"; then
    echo "✅ Redis is responding"
else
    echo "❌ Redis is not responding"
    echo "🔍 Checking Redis logs..."
    docker logs redis-local
fi

echo ""
echo "🎉 Services are ready for integration testing!"
echo ""
echo "📋 Next steps:"
echo "   1. Run the basic validation test:"
echo "      cd search-service && mvn test -Dtest=BasicValidationTest"
echo ""
echo "   2. Run integration tests:"
echo "      mvn test -Dtest=ServiceIntegrationTest -Dspring.profiles.active=integration-test"
echo ""
echo "   3. Stop services when done:"
echo "      ../stop-services.sh"
echo ""
echo "🔍 To view logs:"
echo "   docker logs -f dynamodb-local"
echo "   docker logs -f redis-local"
echo ""
echo "🗂️  DynamoDB data directory: $DYNAMODB_DATA_DIR" 