#!/bin/bash

# Deployment script for Staging environment
# Usage: ./deploy-staging.sh <version>

set -e

VERSION=${1:-latest}
ENVIRONMENT="staging"

echo "=========================================="
echo "Deploying to Staging Environment"
echo "Version: $VERSION"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Configuration
DOCKER_REGISTRY="docker.io/bookstore"
COMPOSE_FILE="docker-compose-staging.yml"

# Service names
SERVICES=(
    "auth-service"
    "book-service"
    "cart-service"
    "inventory-service"
    "notification-service"
)

# Pull latest images
print_info "Pulling Docker images..."
for service in "${SERVICES[@]}"; do
    echo "Pulling ${service}:${VERSION}..."
    docker pull ${DOCKER_REGISTRY}/${service}:${VERSION} || {
        print_error "Failed to pull ${service}"
        exit 1
    }
done
print_success "All images pulled successfully"

# Stop existing services
print_info "Stopping existing services..."
docker-compose -f ${COMPOSE_FILE} down || true
print_success "Services stopped"

# Update docker-compose file with new version
print_info "Updating docker-compose configuration..."
sed -i "s/:latest/:${VERSION}/g" ${COMPOSE_FILE}
print_success "Configuration updated"

# Start services
print_info "Starting services..."
docker-compose -f ${COMPOSE_FILE} up -d

# Wait for services to be healthy
print_info "Waiting for services to be healthy..."
sleep 30

# Check service health
print_info "Checking service health..."
HEALTHY=true

for service in "${SERVICES[@]}"; do
    PORT=""
    case $service in
        "auth-service") PORT="8081" ;;
        "book-service") PORT="8082" ;;
        "cart-service") PORT="8083" ;;
        "inventory-service") PORT="8084" ;;
        "notification-service") PORT="8085" ;;
    esac
    
    if curl -f http://localhost:${PORT}/actuator/health > /dev/null 2>&1; then
        print_success "${service} is healthy"
    else
        print_error "${service} is not healthy"
        HEALTHY=false
    fi
done

if [ "$HEALTHY" = true ]; then
    print_success "Deployment to Staging completed successfully!"
    echo ""
    echo "Service URLs:"
    echo "  Auth Service:         http://localhost:8081"
    echo "  Book Service:         http://localhost:8082"
    echo "  Cart Service:         http://localhost:8083"
    echo "  Inventory Service:    http://localhost:8084"
    echo "  Notification Service: http://localhost:8085"
    echo ""
    exit 0
else
    print_error "Deployment failed - some services are not healthy"
    echo "Check logs with: docker-compose -f ${COMPOSE_FILE} logs"
    exit 1
fi

# Made with Bob
