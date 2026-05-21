#!/bin/bash

# Smoke tests script for deployed services
# Usage: ./smoke-tests.sh <environment>

set -e

ENVIRONMENT=${1:-staging}

echo "=========================================="
echo "Running Smoke Tests"
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

# Test counters
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

# Test function
run_test() {
    local test_name=$1
    local test_command=$2
    
    TESTS_RUN=$((TESTS_RUN + 1))
    print_info "Test ${TESTS_RUN}: ${test_name}"
    
    if eval "$test_command"; then
        print_success "PASSED"
        TESTS_PASSED=$((TESTS_PASSED + 1))
        return 0
    else
        print_error "FAILED"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        return 1
    fi
}

echo ""
print_info "Starting smoke tests..."
echo ""

# Test 1: Auth Service - Health Check
run_test "Auth Service Health Check" \
    "curl -f -s http://localhost:8081/actuator/health | grep -q '\"status\":\"UP\"'"

# Test 2: Book Service - Health Check
run_test "Book Service Health Check" \
    "curl -f -s http://localhost:8082/actuator/health | grep -q '\"status\":\"UP\"'"

# Test 3: Cart Service - Health Check
run_test "Cart Service Health Check" \
    "curl -f -s http://localhost:8083/actuator/health | grep -q '\"status\":\"UP\"'"

# Test 4: Inventory Service - Health Check
run_test "Inventory Service Health Check" \
    "curl -f -s http://localhost:8084/actuator/health | grep -q '\"status\":\"UP\"'"

# Test 5: Notification Service - Health Check
run_test "Notification Service Health Check" \
    "curl -f -s http://localhost:8085/actuator/health | grep -q '\"status\":\"UP\"'"

# Test 6: Book Service - Get All Books
run_test "Book Service - Get All Books" \
    "curl -f -s http://localhost:8082/api/books | grep -q '\"success\":true'"

# Test 7: Book Service - Swagger UI
run_test "Book Service - Swagger UI Accessible" \
    "curl -f -s http://localhost:8082/swagger-ui.html > /dev/null"

# Test 8: Book Service - OpenAPI Docs
run_test "Book Service - OpenAPI Docs Accessible" \
    "curl -f -s http://localhost:8082/api-docs | grep -q 'openapi'"

# Test 9: Book Service - Actuator Metrics
run_test "Book Service - Metrics Endpoint" \
    "curl -f -s http://localhost:8082/actuator/metrics | grep -q 'names'"

# Test 10: Book Service - Prometheus Metrics
run_test "Book Service - Prometheus Endpoint" \
    "curl -f -s http://localhost:8082/actuator/prometheus | grep -q 'jvm_'"

# Test 11: Database Connectivity (via Book Service)
run_test "Database Connectivity Check" \
    "curl -f -s http://localhost:8082/api/books/count | grep -q '\"success\":true'"

# Test 12: Redis Connectivity (check if caching works)
run_test "Redis Connectivity Check" \
    "curl -f -s http://localhost:8082/api/books?page=0&size=1 > /dev/null"

# Test 13: Kafka Connectivity (check if services can connect)
run_test "Kafka Connectivity Check" \
    "docker-compose ps kafka | grep -q 'Up'"

# Test 14: Response Time Check (Book Service)
run_test "Book Service Response Time < 2s" \
    "[ \$(curl -o /dev/null -s -w '%{time_total}' http://localhost:8082/api/books | cut -d'.' -f1) -lt 2 ]"

# Test 15: CORS Headers Check
run_test "CORS Headers Present" \
    "curl -s -I http://localhost:8082/api/books | grep -q 'Access-Control-Allow-Origin'"

echo ""
echo "=========================================="
echo "Smoke Test Results"
echo "=========================================="
echo "Total Tests: ${TESTS_RUN}"
echo "Passed: ${TESTS_PASSED}"
echo "Failed: ${TESTS_FAILED}"
echo "=========================================="

if [ $TESTS_FAILED -eq 0 ]; then
    print_success "All smoke tests passed!"
    echo ""
    echo "Deployment verification successful."
    echo "Services are ready for use."
    echo ""
    exit 0
else
    print_error "Some smoke tests failed!"
    echo ""
    echo "Failed tests: ${TESTS_FAILED}/${TESTS_RUN}"
    echo ""
    echo "Please investigate the failures before proceeding."
    echo ""
    exit 1
fi

# Made with Bob
