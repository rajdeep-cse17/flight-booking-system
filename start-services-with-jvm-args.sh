#!/bin/bash

echo "🚀 Starting Services with JVM Arguments for Java 17+ Compatibility"
echo "=================================================================="

# Check if DynamoDB Local is running
if ! curl -s http://localhost:8000 > /dev/null; then
    echo "❌ DynamoDB Local is not running on port 8000"
    echo "   Please start it first with: ./start-services.sh"
    exit 1
fi

echo "✅ DynamoDB Local is running"

# JVM arguments to fix Java module system issues
JVM_ARGS="--add-opens java.base/java.lang=ALL-UNNAMED \
           --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
           --add-opens java.base/java.util=ALL-UNNAMED"

echo "🔧 Starting Search Service on port 8081..."
cd search-service
java $JVM_ARGS \
     -jar target/search-service-1.0.0.jar \
     --spring.profiles.active=integration-test &
SEARCH_PID=$!

echo "🔧 Starting Booking Service on port 8082..."
cd ../booking-service
java $JVM_ARGS \
     -jar target/booking-service-1.0.0.jar \
     --spring.profiles.active=integration-test &
BOOKING_PID=$!

# Wait for services to start
echo "⏳ Waiting for services to start..."
sleep 10

# Check if services are running
echo "🔍 Checking service status..."

if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "✅ Search Service is running on port 8081"
else
    echo "❌ Search Service failed to start"
fi

if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo "✅ Booking Service is running on port 8082"
else
    echo "❌ Booking Service failed to start"
fi

echo ""
echo "🎉 Services Started!"
echo ""
echo "📋 Service PIDs:"
echo "   Search Service: $SEARCH_PID"
echo "   Booking Service: $BOOKING_PID"
echo ""
echo "🔍 Test the integration:"
echo "   curl 'http://localhost:8081/flights?source=DEL&destination=BOM&userId=user123'"
echo ""
echo "🛑 To stop services:"
echo "   kill $SEARCH_PID $BOOKING_PID"
echo "   or use: ./stop-services.sh" 