#!/bin/bash

echo "🚀 Corrected Integration Testing Script"
echo "======================================="

# Check if we're in the right directory
if [ ! -f "pom.xml" ] || [ ! -d "search-service" ]; then
    echo "❌ Please run this script from the root directory (where pom.xml is located)"
    exit 1
fi

echo "✅ Running from correct directory"

# Step 1: Build all modules first (fixes shared-models compilation)
echo "🔨 Step 1: Building all modules..."
if command -v mvn &> /dev/null; then
    mvn clean compile
    if [ $? -ne 0 ]; then
        echo "❌ Build failed. Please fix compilation errors first."
        exit 1
    fi
    echo "✅ All modules built successfully"
else
    echo "⚠️  Maven not found. Please install Maven or use an IDE."
    echo "   brew install maven  # macOS"
    exit 1
fi

# Step 2: Run basic validation test
echo "🧪 Step 2: Running basic validation test..."
cd search-service
mvn test -Dtest=BasicValidationTest
if [ $? -ne 0 ]; then
    echo "❌ Basic validation test failed"
    exit 1
fi
echo "✅ Basic validation test passed"

# Step 3: Ask user if they want to continue with integration testing
echo ""
echo "📋 Next steps:"
echo "   1. ✅ Basic validation - COMPLETED"
echo "   2. 🔄 Start external services: ../start-services.sh"
echo "   3. 🔄 Run integration tests: mvn test -Dtest=ServiceIntegrationTest -Dspring.profiles.active=integration-test"
echo "   4. 🔄 Test API endpoints manually"
echo "   5. 🔄 Stop services: ../stop-services.sh"
echo ""

read -p "Do you want to start external services and run integration tests? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🚀 Starting external services..."
    cd ..
    ./start-services.sh
    
    if [ $? -eq 0 ]; then
        echo "✅ Services started successfully"
        echo "🧪 Running integration tests..."
        cd search-service
        mvn test -Dtest=ServiceIntegrationTest -Dspring.profiles.active=integration-test
        
        if [ $? -eq 0 ]; then
            echo "✅ Integration tests passed!"
            echo "🎉 You're ready for full integration testing!"
        else
            echo "❌ Integration tests failed"
        fi
    else
        echo "❌ Failed to start services"
    fi
else
    echo "ℹ️  Skipping integration tests. You can run them later with:"
    echo "   cd search-service"
    echo "   mvn test -Dtest=ServiceIntegrationTest -Dspring.profiles.active=integration-test"
fi

echo ""
echo "🎯 Testing completed!" 