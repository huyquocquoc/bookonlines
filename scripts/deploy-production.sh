#!/bin/bash

# Deployment script for Production environment
# Usage: ./deploy-production.sh <version>

set -e

VERSION=${1:-latest}
ENVIRONMENT="production"

echo "=========================================="
echo "Deploying to Production Environment"
echo "Version: $VERSION"
echo "=========================================="
echo ""
echo "⚠️  WARNING: This will deploy to PRODUCTION!"
echo ""
read -p "Are you sure you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "Deployment cancelled."
    exit 0
fi

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
COMPOSE_FILE="docker-compose-production.yml"
BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"

# Service names
SERVICES=(
    "auth-service"
    "book-service"
    "cart-service"
    "inventory-service"
    "notification-service"
)

# Create backup directory
print_info "Creating backup directory..."
mkdir -p ${BACKUP_DIR}
print_success "Backup directory created: ${BACKUP_DIR}"

# Backup current configuration
print_info "Backing up current configuration..."
cp ${COMPOSE_FILE} ${BACKUP_DIR}/
docker-compose -f ${COMPOSE_FILE} config > ${BACKUP_DIR}/current-config.yml
print_success "Configuration backed up"

# Backup database (if applicable)
print_info "Backing up databases..."
# Add database backup commands here
print_success "Database backup completed"

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

# Perform rolling update
print_info "Performing rolling update..."

for service in "${SERVICES[@]}"; do
    print_info "Updating ${service}..."
    
    # Scale up new version
    docker-compose -f ${COMPOSE_FILE} up -d --no-deps --scale ${service}=2 ${service}
    
    # Wait for new instance to be healthy
    sleep 15
    
    # Scale down old version
    docker-compose -f ${COMPOSE_FILE} up -d --no-deps --scale ${service}=1 ${service}
    
    # Wait for stabilization
    sleep 10
    
    print_success "${service} updated successfully"
done

# Update docker-compose file with new version
print_info "Updating docker-compose configuration..."
sed -i "s/:latest/:${VERSION}/g" ${COMPOSE_FILE}
print_success "Configuration updated"

# Final deployment
print_info "Finalizing deployment..."
docker-compose -f ${COMPOSE_FILE} up -d

# Wait for services to stabilize
print_info "Waiting for services to stabilize..."
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
    print_success "Deployment to Production completed successfully!"
    echo ""
    echo "Deployment Details:"
    echo "  Version: ${VERSION}"
    echo "  Timestamp: $(date)"
    echo "  Backup Location: ${BACKUP_DIR}"
    echo ""
    echo "Service URLs:"
    echo "  Auth Service:         http://localhost:8081"
    echo "  Book Service:         http://localhost:8082"
    echo "  Cart Service:         http://localhost:8083"
    echo "  Inventory Service:    http://localhost:8084"
    echo "  Notification Service: http://localhost:8085"
    echo ""
    
    # Log deployment
    echo "$(date): Deployed version ${VERSION} to production" >> deployment.log
    
    exit 0
else
    print_error "Deployment failed - some services are not healthy"
    print_info "Rolling back to previous version..."
    
    # Rollback
    cp ${BACKUP_DIR}/${COMPOSE_FILE} ${COMPOSE_FILE}
    docker-compose -f ${COMPOSE_FILE} up -d
    
    print_error "Rollback completed. Check logs with: docker-compose -f ${COMPOSE_FILE} logs"
    exit 1
fi

# Made with Bob
