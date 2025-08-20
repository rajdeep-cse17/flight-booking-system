#!/bin/bash

echo "🚀 Starting Simple Integration Test for Search Service"
echo "=================================================="

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    exit 1
fi

echo "✅ Java and Maven are available"

# Navigate to search service directory
cd search-service

echo "📁 Running in directory: $(pwd)"

# Run the simple integration test
echo "🧪 Running SimpleSearchServiceIntegrationTest..."
mvn test -Dtest=SimpleSearchServiceIntegrationTest -Dspring.profiles.active=integration-test

if [ $? -eq 0 ]; then
    echo "✅ Simple integration test passed!"
else
    echo "❌ Simple integration test failed!"
    exit 1
fi

echo "🎉 Integration test completed successfully!" 