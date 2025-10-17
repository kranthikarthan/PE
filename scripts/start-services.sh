#!/bin/bash

# Payments Engine Local Development Startup Script
# This script starts all services in the correct order

set -e

echo "🚀 Starting Payments Engine Services..."

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

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker Desktop and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

print_status "Starting infrastructure services..."

# Start infrastructure services first
docker-compose up -d postgres redis zookeeper kafka

print_status "Waiting for infrastructure services to be healthy..."

# Wait for PostgreSQL to be ready
print_status "Waiting for PostgreSQL..."
until docker-compose exec postgres pg_isready -U payments_user -d payments_engine > /dev/null 2>&1; do
    sleep 2
done
print_success "PostgreSQL is ready"

# Wait for Redis to be ready
print_status "Waiting for Redis..."
until docker-compose exec redis redis-cli ping > /dev/null 2>&1; do
    sleep 2
done
print_success "Redis is ready"

# Wait for Kafka to be ready
print_status "Waiting for Kafka..."
until docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
    sleep 5
done
print_success "Kafka is ready"

print_status "Starting monitoring services..."

# Start monitoring services
docker-compose up -d prometheus grafana jaeger

print_status "Building and starting Payments Engine services..."

# Build and start all services
docker-compose up -d --build

print_status "Waiting for services to be healthy..."

# Wait for services to be healthy
services=("payment-initiation-service" "validation-service" "account-adapter-service" "routing-service" "transaction-processing-service" "saga-orchestrator")

for service in "${services[@]}"; do
    print_status "Waiting for $service..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if docker-compose exec $service curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_success "$service is healthy"
            break
        fi
        sleep 5
        timeout=$((timeout - 5))
    done
    
    if [ $timeout -le 0 ]; then
        print_warning "$service may not be fully ready yet"
    fi
done

print_success "All services started successfully!"

echo ""
echo "📊 Service Endpoints:"
echo "===================="
echo "Payment Initiation:    http://localhost:8081/actuator/health"
echo "Validation:            http://localhost:8082/actuator/health"
echo "Account Adapter:       http://localhost:8083/actuator/health"
echo "Routing:               http://localhost:8084/actuator/health"
echo "Transaction Processing: http://localhost:8085/actuator/health"
echo "Saga Orchestrator:     http://localhost:8086/actuator/health"
echo ""
echo "🔍 Monitoring:"
echo "============="
echo "Prometheus:            http://localhost:9090"
echo "Grafana:               http://localhost:3000 (admin/admin)"
echo "Jaeger:                http://localhost:16686"
echo ""
echo "📚 API Documentation:"
echo "===================="
echo "Payment Initiation:    http://localhost:8081/swagger-ui.html"
echo "Validation:            http://localhost:8082/swagger-ui.html"
echo "Account Adapter:       http://localhost:8083/swagger-ui.html"
echo "Routing:               http://localhost:8084/swagger-ui.html"
echo "Transaction Processing: http://localhost:8085/swagger-ui.html"
echo "Saga Orchestrator:     http://localhost:8086/swagger-ui.html"
echo ""
echo "To view logs: docker-compose logs -f"
echo "To stop services: docker-compose down"
echo ""
print_success "Payments Engine is ready for development! 🎉"
