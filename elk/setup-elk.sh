#!/bin/bash

# ELK Stack Setup Script for Bookstore Application
# This script sets up and verifies the ELK stack

set -e

echo "=========================================="
echo "ELK Stack Setup for Bookstore Application"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Check if Docker is running
echo "Checking prerequisites..."
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi
print_success "Docker is running"

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    print_error "docker-compose is not installed. Please install it and try again."
    exit 1
fi
print_success "docker-compose is available"

echo ""
echo "Starting ELK stack..."
echo ""

# Navigate to project root
cd "$(dirname "$0")/.."

# Create log directories if they don't exist
print_info "Creating log directories..."
mkdir -p backend/auth-service/logs
mkdir -p backend/book-service/logs
mkdir -p backend/cart-service/logs
mkdir -p backend/inventory-service/logs
mkdir -p backend/notification-service/logs
print_success "Log directories created"

# Start ELK stack
print_info "Starting ELK services (this may take a few minutes)..."
docker-compose -f docker-compose-elk.yml up -d

echo ""
print_info "Waiting for services to be healthy..."
echo ""

# Wait for Elasticsearch
echo -n "Waiting for Elasticsearch..."
for i in {1..60}; do
    if curl -s http://localhost:9200/_cluster/health > /dev/null 2>&1; then
        echo ""
        print_success "Elasticsearch is ready"
        break
    fi
    echo -n "."
    sleep 2
    if [ $i -eq 60 ]; then
        echo ""
        print_error "Elasticsearch failed to start within 2 minutes"
        exit 1
    fi
done

# Wait for Logstash
echo -n "Waiting for Logstash..."
for i in {1..60}; do
    if curl -s http://localhost:9600/_node/stats > /dev/null 2>&1; then
        echo ""
        print_success "Logstash is ready"
        break
    fi
    echo -n "."
    sleep 2
    if [ $i -eq 60 ]; then
        echo ""
        print_error "Logstash failed to start within 2 minutes"
        exit 1
    fi
done

# Wait for Kibana
echo -n "Waiting for Kibana..."
for i in {1..60}; do
    if curl -s http://localhost:5601/api/status > /dev/null 2>&1; then
        echo ""
        print_success "Kibana is ready"
        break
    fi
    echo -n "."
    sleep 2
    if [ $i -eq 60 ]; then
        echo ""
        print_error "Kibana failed to start within 2 minutes"
        exit 1
    fi
done

echo ""
echo "=========================================="
print_success "ELK Stack is ready!"
echo "=========================================="
echo ""
echo "Access URLs:"
echo "  • Elasticsearch: http://localhost:9200"
echo "  • Kibana:        http://localhost:5601"
echo "  • Logstash:      http://localhost:9600"
echo ""
echo "Next steps:"
echo "  1. Open Kibana at http://localhost:5601"
echo "  2. Go to Management → Stack Management → Index Patterns"
echo "  3. Create index pattern: bookstore-logs-*"
echo "  4. Select @timestamp as the time field"
echo "  5. Start your microservices to generate logs"
echo ""
echo "For more information, see ELK_LOGGING_GUIDE.md"
echo ""

# Check service status
echo "Service Status:"
docker-compose -f docker-compose-elk.yml ps

echo ""
print_info "To view logs: docker-compose -f docker-compose-elk.yml logs -f"
print_info "To stop ELK:  docker-compose -f docker-compose-elk.yml down"
echo ""

# Made with Bob
