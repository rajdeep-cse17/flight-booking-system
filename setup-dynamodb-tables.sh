#!/bin/bash

echo "🚀 Setting up DynamoDB Tables for Integration Testing"
echo "======================================================"

# Check if DynamoDB Local is running
if ! curl -s http://localhost:8000 > /dev/null; then
    echo "❌ DynamoDB Local is not running on port 8000"
    echo "   Please start it first with: ./start-services.sh"
    exit 1
fi

echo "✅ DynamoDB Local is running"

# Check if AWS CLI is available
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI is not installed or not in PATH"
    echo "   Please install AWS CLI first"
    exit 1
fi

echo "✅ AWS CLI is available"

# Set local endpoint
export AWS_ENDPOINT_URL=http://localhost:8000
export AWS_DEFAULT_REGION=us-east-1
export AWS_ACCESS_KEY_ID=local
export AWS_SECRET_ACCESS_KEY=local

echo "🔧 Creating DynamoDB tables..."

# Create Inventory table
echo "📊 Creating inventory table..."
aws dynamodb create-table \
  --table-name inventory \
  --attribute-definitions AttributeName=inventoryId,AttributeType=S \
  --key-schema AttributeName=inventoryId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url $AWS_ENDPOINT_URL \
  --region $AWS_DEFAULT_REGION

if [ $? -eq 0 ]; then
    echo "✅ Inventory table created successfully"
else
    echo "⚠️  Inventory table might already exist or creation failed"
fi

# Create Bookings table
echo "📋 Creating bookings table..."
aws dynamodb create-table \
  --table-name bookings \
  --attribute-definitions AttributeName=bookingId,AttributeType=S \
  --key-schema AttributeName=bookingId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url $AWS_ENDPOINT_URL \
  --region $AWS_DEFAULT_REGION

if [ $? -eq 0 ]; then
    echo "✅ Bookings table created successfully"
else
    echo "⚠️  Bookings table might already exist or creation failed"
fi

# Create Flights table
echo "✈️  Creating flights table..."
aws dynamodb create-table \
  --table-name flights \
  --attribute-definitions AttributeName=flightId,AttributeType=S \
  --key-schema AttributeName=flightId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url $AWS_ENDPOINT_URL \
  --region $AWS_DEFAULT_REGION

if [ $? -eq 0 ]; then
    echo "✅ Flights table created successfully"
else
    echo "⚠️  Flights table might already exist or creation failed"
fi

# Wait for tables to be active
echo "⏳ Waiting for tables to be active..."
sleep 5

# List all tables to verify
echo "📋 Listing all tables..."
aws dynamodb list-tables \
  --endpoint-url $AWS_ENDPOINT_URL \
  --region $AWS_DEFAULT_REGION

echo ""
echo "🎉 DynamoDB Tables Setup Complete!"
echo ""
echo "📋 Next steps:"
echo "   1. Start your services with integration-test profile:"
echo "      cd search-service && java -jar target/search-service-1.0.0.jar --spring.profiles.active=integration-test"
echo "      cd booking-service && java -jar target/booking-service-1.0.0.jar --spring.profiles.active=integration-test"
echo ""
echo "   2. Test the integration:"
echo "      curl 'http://localhost:8081/flights?source=DEL&destination=BOM&userId=user123'"
echo ""
echo "   3. Stop services when done:"
echo "      ../stop-services.sh" 