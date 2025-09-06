#!/bin/bash

# Test runner script for Payment Engine
# Runs all test suites including unit tests, integration tests, and multi-tenant tests

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

# Function to run tests for a service
run_service_tests() {
    local service_name=$1
    local service_path=$2
    
    print_status "Running tests for $service_name..."
    
    cd "$service_path"
    
    if [ -f "pom.xml" ]; then
        # Java/Maven service
        mvn clean test
        if [ $? -eq 0 ]; then
            print_success "$service_name tests passed"
        else
            print_error "$service_name tests failed"
            return 1
        fi
    elif [ -f "package.json" ]; then
        # Node.js service
        npm test
        if [ $? -eq 0 ]; then
            print_success "$service_name tests passed"
        else
            print_error "$service_name tests failed"
            return 1
        fi
    else
        print_warning "No test configuration found for $service_name"
    fi
    
    cd - > /dev/null
}

# Main execution
main() {
    echo "=================================================================="
    echo "           Payment Engine - Test Runner Script                   "
    echo "=================================================================="
    echo ""
    
    # Parse command line arguments
    RUN_UNIT_TESTS=true
    RUN_INTEGRATION_TESTS=true
    RUN_FRONTEND_TESTS=true
    GENERATE_COVERAGE=false
    SPECIFIC_SERVICE=""
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --unit-only)
                RUN_INTEGRATION_TESTS=false
                RUN_FRONTEND_TESTS=false
                shift
                ;;
            --integration-only)
                RUN_UNIT_TESTS=false
                RUN_FRONTEND_TESTS=false
                shift
                ;;
            --frontend-only)
                RUN_UNIT_TESTS=false
                RUN_INTEGRATION_TESTS=false
                shift
                ;;
            --coverage)
                GENERATE_COVERAGE=true
                shift
                ;;
            --service)
                SPECIFIC_SERVICE="$2"
                shift 2
                ;;
            --help)
                echo "Usage: $0 [OPTIONS]"
                echo ""
                echo "Options:"
                echo "  --unit-only        Run only unit tests"
                echo "  --integration-only Run only integration tests"
                echo "  --frontend-only    Run only frontend tests"
                echo "  --coverage         Generate test coverage reports"
                echo "  --service NAME     Run tests for specific service only"
                echo "  --help             Show this help message"
                echo ""
                echo "Services:"
                echo "  shared            Shared library tests"
                echo "  core-banking      Core banking service tests"
                echo "  middleware        Middleware service tests"
                echo "  api-gateway       API Gateway tests"
                echo "  frontend          Frontend tests"
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    # Set test environment
    export SPRING_PROFILES_ACTIVE=test
    export NODE_ENV=test
    
    print_status "Test configuration:"
    echo "  Unit Tests:        $RUN_UNIT_TESTS"
    echo "  Integration Tests: $RUN_INTEGRATION_TESTS"
    echo "  Frontend Tests:    $RUN_FRONTEND_TESTS"
    echo "  Coverage Reports:  $GENERATE_COVERAGE"
    echo "  Specific Service:  ${SPECIFIC_SERVICE:-All}"
    echo ""
    
    # Track test results
    FAILED_SERVICES=()
    
    # Run tests based on configuration
    if [ -n "$SPECIFIC_SERVICE" ]; then
        case "$SPECIFIC_SERVICE" in
            shared)
                run_service_tests "Shared Library" "services/shared" || FAILED_SERVICES+=("shared")
                ;;
            core-banking)
                run_service_tests "Core Banking" "services/core-banking" || FAILED_SERVICES+=("core-banking")
                ;;
            middleware)
                run_service_tests "Middleware" "services/middleware" || FAILED_SERVICES+=("middleware")
                ;;
            api-gateway)
                run_service_tests "API Gateway" "services/api-gateway" || FAILED_SERVICES+=("api-gateway")
                ;;
            frontend)
                run_service_tests "Frontend" "frontend" || FAILED_SERVICES+=("frontend")
                ;;
            *)
                print_error "Unknown service: $SPECIFIC_SERVICE"
                exit 1
                ;;
        esac
    else
        # Run all tests
        if [ "$RUN_UNIT_TESTS" = true ]; then
            print_status "Running unit tests..."
            
            # Test shared library
            run_service_tests "Shared Library" "services/shared" || FAILED_SERVICES+=("shared")
            
            # Test backend services
            run_service_tests "Core Banking" "services/core-banking" || FAILED_SERVICES+=("core-banking")
            run_service_tests "Middleware" "services/middleware" || FAILED_SERVICES+=("middleware")
            run_service_tests "API Gateway" "services/api-gateway" || FAILED_SERVICES+=("api-gateway")
        fi
        
        if [ "$RUN_FRONTEND_TESTS" = true ]; then
            print_status "Running frontend tests..."
            run_service_tests "Frontend" "frontend" || FAILED_SERVICES+=("frontend")
        fi
        
        if [ "$RUN_INTEGRATION_TESTS" = true ]; then
            print_status "Running integration tests..."
            
            # Start test infrastructure if needed
            print_status "Starting test infrastructure..."
            docker-compose -f docker-compose.test.yml up -d postgres redis kafka
            
            # Wait for services
            sleep 10
            
            # Run integration tests
            cd services/core-banking
            mvn test -Dtest=*IntegrationTest || FAILED_SERVICES+=("integration")
            cd ../..
            
            # Cleanup test infrastructure
            print_status "Cleaning up test infrastructure..."
            docker-compose -f docker-compose.test.yml down
        fi
    fi
    
    # Generate coverage reports if requested
    if [ "$GENERATE_COVERAGE" = true ]; then
        print_status "Generating coverage reports..."
        
        # Java services coverage
        for service in shared core-banking middleware api-gateway; do
            if [ -d "services/$service" ]; then
                cd "services/$service"
                mvn jacoco:report
                cd ../..
            fi
        done
        
        # Frontend coverage
        if [ -d "frontend" ]; then
            cd frontend
            npm run test:coverage
            cd ..
        fi
        
        print_success "Coverage reports generated in target/site/jacoco/ and frontend/coverage/"
    fi
    
    # Summary
    echo ""
    print_status "=================================================================="
    print_status "Test Results Summary"
    print_status "=================================================================="
    
    if [ ${#FAILED_SERVICES[@]} -eq 0 ]; then
        print_success "All tests passed successfully! ✅"
        echo ""
        print_success "Services tested:"
        if [ "$RUN_UNIT_TESTS" = true ]; then
            echo "  ✅ Shared Library"
            echo "  ✅ Core Banking Service"
            echo "  ✅ Middleware Service"
            echo "  ✅ API Gateway"
        fi
        if [ "$RUN_FRONTEND_TESTS" = true ]; then
            echo "  ✅ Frontend"
        fi
        if [ "$RUN_INTEGRATION_TESTS" = true ]; then
            echo "  ✅ Integration Tests"
        fi
        
        if [ "$GENERATE_COVERAGE" = true ]; then
            echo ""
            print_status "Coverage reports available:"
            echo "  📊 Backend: target/site/jacoco/index.html"
            echo "  📊 Frontend: frontend/coverage/lcov-report/index.html"
        fi
        
        exit 0
    else
        print_error "Some tests failed! ❌"
        echo ""
        print_error "Failed services:"
        for service in "${FAILED_SERVICES[@]}"; do
            echo "  ❌ $service"
        done
        
        echo ""
        print_status "Check the logs above for detailed error information."
        exit 1
    fi
}

# Run main function
main "$@"