#!/bin/bash

# Flight Booking Microservices - End-to-End Test Script
# This script automates the entire E2E testing process

set -e  # Exit on any error

echo "🚀 Starting Flight Booking Microservices E2E Test"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command_exists java; then
    print_error "Java is not installed. Please install Java 17+"
    exit 1
fi

if ! command_exists mvn; then
    print_error "Maven is not installed. Please install Maven"
    exit 1
fi

if ! command_exists docker; then
    print_error "Docker is not installed. Please install Docker"
    exit 1
fi

if ! command_exists aws; then
    print_error "AWS CLI is not installed. Please install AWS CLI"
    exit 1
fi

print_success "All prerequisites are met!"

# Navigate to project root
cd "$(dirname "$0")"
print_status "Working directory: $(pwd)"

# Step 1: Build all services
echo -e "\n🔨 Building all services..."
print_status "Building search service..."
cd search-service
mvn clean package -DskipTests -q
print_success "Search service built successfully"

print_status "Building booking service..."
cd ../booking-service
mvn clean package -DskipTests -q
print_success "Booking service built successfully"

print_status "Building payment service..."
cd ../payment-service
mvn clean package -DskipTests -q
print_success "Payment service built successfully"

cd ..

# Step 2: Start infrastructure services
echo -e "\n🐳 Starting infrastructure services..."

print_status "Starting DynamoDB Local..."
docker run -d --name dynamodb-local -p 8000:8000 amazon/dynamodb-local -jar DynamoDBLocal.jar -sharedDb >/dev/null 2>&1
sleep 3

print_status "Starting Redis..."
docker run -d --name redis-local -p 6379:6379 redis:7-alpine >/dev/null 2>&1
sleep 3

print_status "Verifying infrastructure services..."
if docker ps | grep -q "dynamodb-local" && docker ps | grep -q "redis-local"; then
    print_success "Infrastructure services started successfully"
else
    print_error "Failed to start infrastructure services"
    exit 1
fi

# Step 3: Create DynamoDB tables
echo -e "\n🗄️ Creating DynamoDB tables..."

export AWS_ACCESS_KEY_ID=dummy
export AWS_SECRET_ACCESS_KEY=dummy
export AWS_DEFAULT_REGION=us-east-1

print_status "Creating flights table..."
aws dynamodb create-table \
    --table-name flights \
    --attribute-definitions AttributeName=flightId,AttributeType=S \
    --key-schema AttributeName=flightId,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000 >/dev/null 2>&1

print_status "Creating inventory table..."
aws dynamodb create-table \
    --table-name inventory \
    --attribute-definitions AttributeName=inventoryId,AttributeType=S \
    --key-schema AttributeName=inventoryId,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000 >/dev/null 2>&1

print_status "Creating bookings table..."
aws dynamodb create-table \
    --table-name bookings \
    --attribute-definitions AttributeName=bookingId,AttributeType=S \
    --key-schema AttributeName=bookingId,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000 >/dev/null 2>&1

sleep 5  # Wait for tables to be created

print_status "Verifying tables created..."
if aws dynamodb list-tables --endpoint-url http://localhost:8000 | grep -q "flights"; then
    print_success "DynamoDB tables created successfully"
else
    print_error "Failed to create DynamoDB tables"
    exit 1
fi

# Step 4: Insert sample data
echo -e "\n📝 Inserting sample data..."

print_status "Inserting sample flights..."
aws dynamodb put-item \
    --table-name flights \
    --item '{"flightId":{"S":"F001"},"daysOfWeek":{"S":"Monday,Tuesday,Wednesday"},"source":{"S":"DEL"},"destination":{"S":"BOM"},"cost":{"S":"299.99"}}' \
    --endpoint-url http://localhost:8000 >/dev/null 2>&1

aws dynamodb put-item \
    --table-name flights \
    --item '{"flightId":{"S":"F002"},"daysOfWeek":{"S":"Wednesday,Thursday"},"source":{"S":"DEL"},"destination":{"S":"BOM"},"cost":{"S":"349.99"}}' \
    --endpoint-url http://localhost:8000 >/dev/null 2>&1

aws dynamodb put-item \
    --table-name flights \
    --item '{"flightId":{"S":"F003"},"daysOfWeek":{"S":"Monday,Friday"},"source":{"S":"DEL"},"destination":{"S":"BLR"},"cost":{"S":"199.99"}}' \
    --endpoint-url http://localhost:8000 >/dev/null 2>&1

print_status "Inserting sample inventory..."
aws dynamodb put-item \
    --table-name inventory \
    --item '{"inventoryId":{"S":"INV001"},"flightId":{"S":"F001"},"date":{"S":"2024-01-15"},"numberOfSeatsLeft":{"S":"20"},"version":{"S":"1"}}' \
    --endpoint-url http://localhost:8000 >/dev/null 2>&1

aws dynamodb put-item \
    --table-name inventory \
    --item '{"inventoryId":{"S":"INV002"},"flightId":{"S":"F002"},"date":{"S":"2024-01-16"},"numberOfSeatsLeft":{"S":"15"},"version":{"S":"1"}}' \
    --endpoint-url http://localhost:8000 >/dev/null 2>&1

aws dynamodb put-item \
    --table-name inventory \
    --item '{"inventoryId":{"S":"INV003"},"flightId":{"S":"F003"},"date":{"S":"2024-01-17"},"numberOfSeatsLeft":{"S":"25"},"version":{"S":"1"}}' \
    --endpoint-url http://localhost:8000 >/dev/null 2>&1

print_success "Sample data inserted successfully"

# Step 5: Start microservices
echo -e "\n🚀 Starting microservices..."

print_status "Starting payment service..."
cd payment-service
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     -jar target/payment-service-1.0.0.jar >/dev/null 2>&1 &
PAYMENT_PID=$!
cd ..

print_status "Starting search service..."
cd search-service
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     -jar target/search-service-1.0.0.jar \
     --spring.profiles.active=integration-test \
     --server.port=8081 >/dev/null 2>&1 &
SEARCH_PID=$!
cd ..

print_status "Starting booking service..."
cd booking-service
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     -jar target/booking-service-1.0.0.jar \
     --spring.profiles.active=integration-test \
     --server.port=8082 >/dev/null 2>&1 &
BOOKING_PID=$!
cd ..

# Step 6: Wait for services to start
echo -e "\n⏳ Waiting for services to start..."
sleep 10

print_status "Checking service status..."
if ps -p $PAYMENT_PID >/dev/null && ps -p $SEARCH_PID >/dev/null && ps -p $BOOKING_PID >/dev/null; then
    print_success "All services are running"
else
    print_error "Some services failed to start"
    exit 1
fi

# Step 7: Sanity checks
echo -e "\n🔍 Running sanity checks..."

print_status "Testing payment service..."
if curl -s -X POST "http://localhost:8083/api/payment/pay" \
     -H "Content-Type: application/json" \
     -d '{"amount":100.0}' | grep -q "SUCCESS"; then
    print_success "Payment service is working"
else
    print_error "Payment service sanity check failed"
    exit 1
fi

print_status "Testing search service..."
if curl -s "http://localhost:8081/flights?userId=user123&source=DEL&destination=BOM&preference=cheapest" | grep -q "flightId"; then
    print_success "Search service is working"
else
    print_error "Search service sanity check failed"
    exit 1
fi

print_status "Testing booking service..."
if curl -s "http://localhost:8082/api/booking/test/inventory" | grep -q "Success"; then
    print_success "Booking service is working"
else
    print_error "Booking service sanity check failed"
    exit 1
fi

# Step 8: End-to-End test
echo -e "\n🎯 Running end-to-end test..."

print_status "Step 1: Searching for flights..."
SEARCH_RESPONSE=$(curl -s "http://localhost:8081/flights?userId=user123&source=DEL&destination=BOM&preference=cheapest")
echo "Search Response: $SEARCH_RESPONSE"

print_status "Step 2: Creating a booking..."
BOOKING_RESPONSE=$(curl -s -X POST "http://localhost:8082/api/booking/flight/book" \
     -H "Content-Type: application/json" \
     -d '{"userId":"user123","flightIds":["F001"],"date":"2024-01-15","source":"DEL","destination":"BOM","numberOfPassengers":1}')
echo "Booking Response: $BOOKING_RESPONSE"

# Extract booking ID
BOOKING_ID=$(echo $BOOKING_RESPONSE | grep -o '"bookingId":"[^"]*"' | cut -d'"' -f4)
if [ -z "$BOOKING_ID" ]; then
    print_error "Failed to extract booking ID"
    exit 1
fi
echo "Booking ID: $BOOKING_ID"

print_status "Step 3: Waiting for payment processing..."
sleep 5

print_status "Step 4: Checking booking status..."
BOOKING_STATUS=$(curl -s "http://localhost:8082/api/booking/$BOOKING_ID")
echo "Final Booking Status: $BOOKING_STATUS"

print_status "Step 5: Verifying booking persistence in DynamoDB..."
echo "🔍 Checking if booking was created in database..."

# Verify booking exists in DynamoDB
BOOKING_DB_CHECK=$(aws dynamodb scan \
    --table-name bookings \
    --filter-expression "bookingId = :bookingId" \
    --expression-attribute-values "{\":bookingId\":{\"S\":\"$BOOKING_ID\"}}" \
    --endpoint-url http://localhost:8000 \
    2>/dev/null)

if echo "$BOOKING_DB_CHECK" | grep -q "$BOOKING_ID"; then
    print_success "✅ Booking successfully persisted to DynamoDB"
else
    print_error "❌ Booking NOT found in DynamoDB"
    echo "This indicates a critical data persistence issue!"
fi

# Final status check
echo -e "\n🎉 E2E Test Results:"
if echo "$BOOKING_STATUS" | grep -q "SUCCESS"; then
    print_success "🎯 END-TO-END TEST PASSED! 🎯"
    echo "✅ All services are working correctly"
    echo "✅ Complete booking flow executed successfully"
    echo "✅ Payment processing completed"
else
    print_error "❌ END-TO-END TEST FAILED ❌"
    echo "Please check the logs above for details"
fi

echo -e "\n📋 Test Summary:"
echo "• Search Service: ✅ Working"
echo "• Booking Service: ✅ Working"
echo "• Payment Service: ✅ Working"
echo "• DynamoDB Persistence: $(if echo "$BOOKING_DB_CHECK" | grep -q "$BOOKING_ID"; then echo "✅ Working"; else echo "❌ Failed"; fi)"
echo "• E2E Flow: $(if echo "$BOOKING_STATUS" | grep -q "SUCCESS"; then echo "✅ PASSED"; else echo "❌ FAILED"; fi)"

echo -e "\n🧹 To clean up, run: ./cleanup.sh"
echo "📖 For detailed instructions, see: README.md"

# ⚠️ IMPORTANT WARNING ABOUT CLEANUP
echo -e "\n⚠️  IMPORTANT WARNING ⚠️"
echo "=========================================="
echo "🚨 BEFORE RUNNING E2E TESTS AGAIN, YOU MUST RUN CLEANUP! 🚨"
echo "=========================================="
echo ""
echo "❌ Without cleanup, you will encounter:"
echo "   • Port conflicts (services won't start)"
echo "   • Database conflicts (existing data interferes)"
echo "   • Container conflicts (Docker containers exist)"
echo "   • Process conflicts (previous instances running)"
echo ""
echo "✅ ALWAYS run cleanup first:"
echo "   ./cleanup.sh"
echo ""
echo "💡 Then you can safely run E2E tests again:"
echo "   ./run-e2e-test.sh"
echo "=========================================="

# Keep services running for manual testing
echo -e "\n💡 Services are still running for manual testing."
echo "   Use Ctrl+C to stop this script and keep services running,"
echo "   or run the cleanup script to stop everything." 