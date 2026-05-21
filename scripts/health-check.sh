#!/bin/bash

# Health check script for deployed services
# Usage: ./health-check.sh <environment>

set -e

ENVIRONMENT=${1:-staging}

echo "=========================================="
echo "Running Health Checks"
echo "Environment: $ENVIRONMENT"
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

# Service configuration
declare -A SERVICES
SERVICES=(
    ["auth-service"]="8081"
    ["book-service"]="8082"
    ["cart-service"]="8083"
    ["inventory-service"]="8084"
    ["notification-service"]="8085"
)

# Health check function
check_service_health() {
    local service=$1
    local port=$2
    local max_attempts=30
    local attempt=0
    
    print_info "Checking ${service}..."
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -f -s http://localhost:${port}/actuator/health > /dev/null 2>&1; then
            local response=$(curl -s http://localhost:${port}/actuator/health)
            local status=$(echo $response | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
            
            if [ "$status" = "UP" ]; then
                print_success "${service} is healthy (port ${port})"
                return 0
            fi
        fi
        
        attempt=$((attempt + 1))
        sleep 2
    done
    
    print_error "${service} is not healthy after ${max_attempts} attempts"
    return 1
}

# Check all services
ALL_HEALTHY=true

for service in "${!SERVICES[@]}"; do
    if ! check_service_health "$service" "${SERVICES[$service]}"; then
        ALL_HEALTHY=false
    fi
done

echo ""
echo "=========================================="

if [ "$ALL_HEALTHY" = true ]; then
    print_success "All services are healthy!"
    echo ""
    echo "Service Status:"
    for service in "${!SERVICES[@]}"; do
        echo "  ✓ ${service}: http://localhost:${SERVICES[$service]}"
    done
    echo ""
    exit 0
else
    print_error "Some services are not healthy!"
    echo ""
    echo "Troubleshooting:"
    echo "  1. Check service logs: docker-compose logs <service-name>"
    echo "  2. Check service status: docker-compose ps"
    echo "  3. Verify configuration: docker-compose config"
    echo ""
    exit 1
fi

# Made with Bob
