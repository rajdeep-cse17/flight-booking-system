#!/bin/bash

echo "🚀 Creating Test Data for Integration Testing"
echo "=============================================="

# Set local AWS credentials
export AWS_ACCESS_KEY_ID=local
export AWS_SECRET_ACCESS_KEY=local
export AWS_DEFAULT_REGION=us-east-1

echo "📊 Creating inventory record for flight F001..."

# Create inventory record for flight F001 on 2024-01-15
aws dynamodb put-item \
  --table-name inventory \
  --item '{
    "inventoryId": {"S": "INV001"},
    "flightId": {"S": "F001"},
    "date": {"S": "2024-01-15"},
    "numberOfSeatsLeft": {"N": "50"},
    "version": {"N": "1"}
  }' \
  --endpoint-url http://localhost:8000

if [ $? -eq 0 ]; then
    echo "✅ Inventory record created successfully"
else
    echo "❌ Failed to create inventory record"
fi

echo "✈️  Creating flight record for F001..."

# Create flight record for F001
aws dynamodb put-item \
  --table-name flights \
  --item '{
    "flightId": {"S": "F001"},
    "source": {"S": "DEL"},
    "destination": {"S": "BOM"},
    "cost": {"N": "299.99"},
    "daysOfWeek": {"L": [{"S": "Monday"}, {"S": "Tuesday"}, {"S": "Wednesday"}]}
  }' \
  --endpoint-url http://localhost:8000

if [ $? -eq 0 ]; then
    echo "✅ Flight record created successfully"
else
    echo "❌ Failed to create flight record"
fi

echo "📋 Verifying data..."

# List inventory records
echo "📊 Inventory records:"
aws dynamodb scan --table-name inventory --endpoint-url http://localhost:8000

echo ""
echo "✈️  Flight records:"
aws dynamodb scan --table-name flights --endpoint-url http://localhost:8000

echo ""
echo "🎉 Test data creation complete!"
echo ""
echo "📋 Now test the integration:"
echo "   curl -X POST 'http://localhost:8082/api/booking/flight/book' \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{"
echo "       \"userId\": \"user123\","
echo "       \"flightIds\": [\"F001\"],"
echo "       \"date\": \"2024-01-15\","
echo "       \"source\": \"DEL\","
echo "       \"destination\": \"BOM\","
echo "       \"numberOfPassengers\": 1"
echo "     }'" 