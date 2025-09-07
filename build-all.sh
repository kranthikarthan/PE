#!/bin/bash

# Payment Engine Build Script
# Builds all components of the payment engine system

set -e

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

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command_exists docker; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! command_exists docker-compose; then
        print_error "Docker Compose is not installed or not in PATH"
        exit 1
    fi
    
    if ! command_exists mvn; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    if ! command_exists node; then
        print_error "Node.js is not installed or not in PATH"
        exit 1
    fi
    
    if ! command_exists npm; then
        print_error "npm is not installed or not in PATH"
        exit 1
    fi
    
    print_success "All prerequisites are installed"
}

# Build shared components
build_shared() {
    print_status "Building shared components..."
    
    cd services/shared
    mvn clean install -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "Shared components built successfully"
    else
        print_error "Failed to build shared components"
        exit 1
    fi
    
    cd ../..
}

# Build backend services
build_backend() {
    print_status "Building backend services..."
    
    # Build Core Banking Service
    print_status "Building Core Banking Service..."
    cd services/core-banking
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "Core Banking Service built successfully"
    else
        print_error "Failed to build Core Banking Service"
        exit 1
    fi
    cd ../..
    
    # Build Payment Processing Service
    print_status "Building Payment Processing Service..."
    cd services/payment-processing
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "Payment Processing Service built successfully"
    else
        print_error "Failed to build Payment Processing Service"
        exit 1
    fi
    cd ../..
    
    # Build API Gateway
    print_status "Building API Gateway..."
    cd services/api-gateway
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "API Gateway built successfully"
    else
        print_error "Failed to build API Gateway"
        exit 1
    fi
    cd ../..
}

# Build frontend
build_frontend() {
    print_status "Building React frontend..."
    
    cd frontend
    
    # Install dependencies
    print_status "Installing npm dependencies..."
    npm ci
    
    if [ $? -eq 0 ]; then
        print_success "npm dependencies installed successfully"
    else
        print_error "Failed to install npm dependencies"
        exit 1
    fi
    
    # Run tests
    print_status "Running frontend tests..."
    npm test -- --coverage --watchAll=false
    
    # Build application
    print_status "Building React application..."
    npm run build
    
    if [ $? -eq 0 ]; then
        print_success "React frontend built successfully"
    else
        print_error "Failed to build React frontend"
        exit 1
    fi
    
    cd ..
}

# Build Docker images
build_docker_images() {
    print_status "Building Docker images..."
    
    # Build Core Banking Service image
    print_status "Building Core Banking Service Docker image..."
    docker build -t payment-engine/core-banking:latest -f services/core-banking/Dockerfile services/
    
    if [ $? -eq 0 ]; then
        print_success "Core Banking Service Docker image built successfully"
    else
        print_error "Failed to build Core Banking Service Docker image"
        exit 1
    fi
    
    # Build Payment Processing Service image
    print_status "Building Payment Processing Service Docker image..."
    docker build -t payment-engine/payment-processing:latest -f services/payment-processing/Dockerfile services/
    
    if [ $? -eq 0 ]; then
        print_success "Payment Processing Service Docker image built successfully"
    else
        print_error "Failed to build Payment Processing Service Docker image"
        exit 1
    fi
    
    # Build API Gateway image
    print_status "Building API Gateway Docker image..."
    docker build -t payment-engine/api-gateway:latest -f services/api-gateway/Dockerfile services/
    
    if [ $? -eq 0 ]; then
        print_success "API Gateway Docker image built successfully"
    else
        print_error "Failed to build API Gateway Docker image"
        exit 1
    fi
    
    # Build Frontend image
    print_status "Building Frontend Docker image..."
    docker build -t payment-engine/frontend:latest frontend/
    
    if [ $? -eq 0 ]; then
        print_success "Frontend Docker image built successfully"
    else
        print_error "Failed to build Frontend Docker image"
        exit 1
    fi
}

# Start infrastructure services
start_infrastructure() {
    print_status "Starting infrastructure services..."
    
    # Start PostgreSQL, Kafka, Redis, etc.
    docker-compose up -d postgres kafka redis zookeeper prometheus grafana
    
    if [ $? -eq 0 ]; then
        print_success "Infrastructure services started successfully"
        print_status "Waiting for services to be ready..."
        sleep 30
    else
        print_error "Failed to start infrastructure services"
        exit 1
    fi
}

# Run tests
run_tests() {
    print_status "Running integration tests..."
    
    # Wait for services to be ready
    print_status "Waiting for database to be ready..."
    until docker-compose exec postgres pg_isready -U payment_user -d payment_engine; do
        sleep 2
    done
    
    print_status "Waiting for Kafka to be ready..."
    until docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092; do
        sleep 2
    done
    
    # Run backend tests
    cd services/core-banking
    mvn test -Dspring.profiles.active=integration
    
    if [ $? -eq 0 ]; then
        print_success "Backend integration tests passed"
    else
        print_warning "Some backend tests failed"
    fi
    
    cd ../..
}

# Deploy to local environment
deploy_local() {
    print_status "Deploying to local environment..."
    
    # Start all services
    docker-compose up -d
    
    if [ $? -eq 0 ]; then
        print_status "Waiting for database to be ready..."
        sleep 10
        
        # Run database migrations
        print_status "Running database migrations..."
        docker-compose exec -T postgres psql -U payment_user -d payment_engine -f /docker-entrypoint-initdb.d/03-run-migrations.sql
        
        if [ $? -eq 0 ]; then
            print_success "Database migrations completed successfully"
        else
            print_warning "Database migrations may have already been applied"
        fi
        
        print_success "All services deployed successfully"
        
        print_status "Service URLs:"
        echo "  Frontend:        http://localhost:3000"
        echo "  API Gateway:     http://localhost:8080"
        echo "  Core Banking:    http://localhost:8081"
        echo "  Payment Processing:      http://localhost:8082"
        echo "  Prometheus:      http://localhost:9090"
        echo "  Grafana:         http://localhost:3000 (admin/admin)"
        echo "  Kafka UI:        http://localhost:8080"
        echo "  PostgreSQL:      localhost:5432 (payment_user/payment_pass)"
        
    else
        print_error "Failed to deploy services"
        exit 1
    fi
}

# Main execution
main() {
    echo "=================================================================="
    echo "           Payment Engine - Build & Deployment Script            "
    echo "=================================================================="
    echo ""
    
    # Parse command line arguments
    SKIP_TESTS=false
    SKIP_DOCKER=false
    DEPLOY=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --skip-docker)
                SKIP_DOCKER=true
                shift
                ;;
            --deploy)
                DEPLOY=true
                shift
                ;;
            --help)
                echo "Usage: $0 [options]"
                echo ""
                echo "Options:"
                echo "  --skip-tests    Skip running tests"
                echo "  --skip-docker   Skip building Docker images"
                echo "  --deploy        Deploy to local environment after build"
                echo "  --help          Show this help message"
                echo ""
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                echo "Use --help for usage information"
                exit 1
                ;;
        esac
    done
    
    # Execute build steps
    check_prerequisites
    
    print_status "Starting build process..."
    echo ""
    
    build_shared
    build_backend
    build_frontend
    
    if [ "$SKIP_DOCKER" = false ]; then
        build_docker_images
    else
        print_warning "Skipping Docker image builds"
    fi
    
    if [ "$DEPLOY" = true ]; then
        start_infrastructure
        
        if [ "$SKIP_TESTS" = false ]; then
            run_tests
        else
            print_warning "Skipping tests"
        fi
        
        deploy_local
    else
        print_status "Build completed. Use --deploy to start local environment."
    fi
    
    echo ""
    echo "=================================================================="
    print_success "Payment Engine build completed successfully!"
    echo "=================================================================="
    
    if [ "$DEPLOY" = true ]; then
        echo ""
        print_status "Next steps:"
        echo "  1. Open http://localhost:3000 to access the frontend"
        echo "  2. Use admin/admin to login"
        echo "  3. Check service health at http://localhost:8080/actuator/health"
        echo "  4. Monitor metrics at http://localhost:9090 (Prometheus)"
        echo "  5. View dashboards at http://localhost:3000 (Grafana)"
        echo ""
        print_status "To stop all services: docker-compose down"
    fi
}

# Execute main function
main "$@"