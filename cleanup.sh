#!/bin/bash

# Flight Booking Microservices - Cleanup Script
# This script stops all services and cleans up resources

echo "🧹 Starting cleanup process..."
echo "=============================="

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

# Stop all microservices
echo -e "\n🛑 Stopping microservices..."

print_status "Stopping search service..."
pkill -f search-service 2>/dev/null || print_warning "Search service not running"

print_status "Stopping booking service..."
pkill -f booking-service 2>/dev/null || print_warning "Booking service not running"

print_status "Stopping payment service..."
pkill -f payment-service 2>/dev/null || print_warning "Payment service not running"

# Wait a moment for services to stop
sleep 2

# Check if any services are still running
RUNNING_SERVICES=$(ps aux | grep -E "(search-service|booking-service|payment-service)" | grep -v grep | wc -l)
if [ "$RUNNING_SERVICES" -eq 0 ]; then
    print_success "All microservices stopped successfully"
else
    print_warning "Some services may still be running. Count: $RUNNING_SERVICES"
fi

# Stop and remove Docker containers
echo -e "\n🐳 Stopping Docker containers..."

print_status "Stopping all Docker containers..."
docker stop $(docker ps -q) 2>/dev/null || print_warning "No Docker containers running"

print_status "Removing Docker containers..."
docker rm $(docker ps -aq) 2>/dev/null || print_warning "No Docker containers to remove"

print_status "Checking Docker status..."
if [ "$(docker ps -q | wc -l)" -eq 0 ]; then
    print_success "All Docker containers stopped and removed"
else
    print_warning "Some Docker containers may still be running"
fi

# Clean up any temporary files (optional)
echo -e "\n🗂️ Cleaning up temporary files..."

print_status "Removing target directories..."
rm -rf */target/ 2>/dev/null || print_warning "No target directories found"

print_status "Removing any temporary files..."
find . -name "*.tmp" -delete 2>/dev/null || print_warning "No temporary files found"
find . -name "*.log" -delete 2>/dev/null || print_warning "No log files found"

# Final status check
echo -e "\n📊 Cleanup Summary:"

# Check if any services are still running
RUNNING_SERVICES=$(ps aux | grep -E "(search-service|booking-service|payment-service)" | grep -v grep | wc -l)
if [ "$RUNNING_SERVICES" -eq 0 ]; then
    echo "• Microservices: ✅ All stopped"
else
    echo "• Microservices: ⚠️ $RUNNING_SERVICES still running"
fi

# Check Docker status
if [ "$(docker ps -q | wc -l)" -eq 0 ]; then
    echo "• Docker Containers: ✅ All stopped and removed"
else
    echo "• Docker Containers: ⚠️ Some still running"
fi

# Check if ports are free
echo -e "\n🔌 Port Status Check:"
if lsof -i :8081 >/dev/null 2>&1; then
    echo "• Port 8081 (Search): ⚠️ Still in use"
else
    echo "• Port 8081 (Search): ✅ Free"
fi

if lsof -i :8082 >/dev/null 2>&1; then
    echo "• Port 8082 (Booking): ⚠️ Still in use"
else
    echo "• Port 8082 (Booking): ✅ Free"
fi

if lsof -i :8083 >/dev/null 2>&1; then
    echo "• Port 8083 (Payment): ⚠️ Still in use"
else
    echo "• Port 8083 (Payment): ✅ Free"
fi

if lsof -i :8000 >/dev/null 2>&1; then
    echo "• Port 8000 (DynamoDB): ⚠️ Still in use"
else
    echo "• Port 8000 (DynamoDB): ✅ Free"
fi

if lsof -i :6379 >/dev/null 2>&1; then
    echo "• Port 6379 (Redis): ⚠️ Still in use"
else
    echo "• Port 6379 (Redis): ✅ Free"
fi

echo -e "\n🎉 Cleanup completed!"
echo "💡 To run the E2E test again, use: ./run-e2e-test.sh"
echo "📖 For detailed instructions, see: README.md" 