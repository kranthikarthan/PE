# Payments Engine

A comprehensive payments processing platform implementing domain-driven design principles, microservices architecture, and event-driven patterns for modern financial systems.

## Architecture Overview

The Payments Engine consists of domain models, microservices, and supporting infrastructure:

### Domain Models
- **Shared**: Common value objects and types
- **Tenant Management**: Multi-tenant support and isolation
- **Payment Initiation**: Payment request processing
- **Transaction Processing**: Double-entry bookkeeping and ledger management
- **Validation**: Business rules and compliance validation
- **Clearing Adapter**: External clearing system integration
- **Account Adapter**: Core banking system integration
- **Saga Orchestrator**: Distributed transaction coordination

### Microservices
- **Payment Initiation Service**: Payment request creation and validation
- **Validation Service**: Business rules and compliance enforcement
- **Account Adapter Service**: Core banking system integration
- **Routing Service**: Intelligent payment routing decisions
- **Transaction Processing Service**: Double-entry bookkeeping and ledger management
- **Saga Orchestrator Service**: Distributed transaction coordination

## Project Structure

```
payments-engine/
├── domain-models/                    # Domain model modules
│   ├── shared/                      # Shared value objects and types
│   ├── tenant-management/           # Tenant aggregate and related entities
│   ├── payment-initiation/          # Payment aggregate and events
│   ├── transaction-processing/      # Transaction aggregate and ledger entries
│   ├── validation/                  # Payment validation rules
│   ├── clearing-adapter/            # Clearing system integration
│   ├── account-adapter/             # Account system integration
│   └── saga-orchestrator/           # Saga orchestration
├── payment-initiation-service/       # Payment initiation microservice
├── validation-service/              # Validation microservice
├── account-adapter-service/        # Account adapter microservice
├── routing-service/                # Routing microservice
├── transaction-processing-service/  # Transaction processing microservice
├── saga-orchestrator/              # Saga orchestrator microservice
├── shared-config/                  # Shared configuration
├── shared-telemetry/               # Shared observability
├── contracts/                      # API contracts and DTOs
├── schema-verification/            # Flyway migration validation
├── jpa-verification/               # JPA entity mapping validation
├── database-migrations/            # Flyway SQL migration scripts
└── docker/                         # Docker configuration and deployment
```

## Prerequisites

- **Java 17** or higher
- **Maven 3.9+**
- **Docker** (for schema and JPA verification tests)

## Quick Start

### Running Microservices

The Payments Engine can be run as a complete microservices platform using Docker Compose:

```bash
# Start all services
docker-compose up -d

# Check service health
curl http://localhost:8081/payment-initiation/actuator/health
curl http://localhost:8082/validation/actuator/health
curl http://localhost:8083/account-adapter-service/actuator/health
curl http://localhost:8084/routing-service/actuator/health
curl http://localhost:8085/transaction-processing/actuator/health
curl http://localhost:8086/saga-orchestrator/actuator/health
```

### Service Ports

| Service | Port | Health Check |
|---------|------|--------------|
| Payment Initiation | 8081 | `/payment-initiation/actuator/health` |
| Validation | 8082 | `/validation/actuator/health` |
| Account Adapter | 8083 | `/account-adapter-service/actuator/health` |
| Routing | 8084 | `/routing-service/actuator/health` |
| Transaction Processing | 8085 | `/transaction-processing/actuator/health` |
| Saga Orchestrator | 8086 | `/saga-orchestrator/actuator/health` |

### Local Development (without Docker)

```bash
# Compile and run unit tests (skips Docker-dependent tests)
mvn clean compile test

# Run specific module tests
mvn -pl domain-models/tenant-management test
```

### Full Verification (with Docker)

```bash
# Run all tests including schema and JPA verification
mvn clean test

# Run with CI profile (enforces Docker requirement)
mvn -Pci clean test
```

## Schema and JPA Verification

### Local Development

The project includes comprehensive schema and JPA verification tests that require Docker:

- **Schema Verification**: Validates Flyway migrations against PostgreSQL
- **JPA Verification**: Validates entity mappings against the migrated schema
- **Happy-path Tests**: Tests successful persistence operations
- **Negative-path Tests**: Tests domain validation and error handling

#### Running Verification Tests

```bash
# Run schema verification only
mvn -pl schema-verification test

# Run JPA verification only  
mvn -pl jpa-verification test

# Run all verification tests
mvn -pl schema-verification,jpa-verification test
```

#### Skipping Docker-dependent Tests

If Docker is not available locally, tests will be skipped automatically:

```bash
# Tests will be skipped if Docker is not running
mvn test
```

### CI/CD Pipeline

The CI profile enforces Docker availability:

```bash
# CI pipeline command (fails if Docker unavailable)
mvn -Pci clean test
```

This ensures that:
- Schema migrations are validated against PostgreSQL
- JPA entity mappings are verified against the actual database schema
- All domain model persistence operations are tested

## Code Quality

### Spotless Formatting

The project uses Spotless for code formatting:

```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply

# Format specific module
mvn -pl domain-models/tenant-management spotless:apply
```

### Build Verification

```bash
# Full build with all checks
mvn clean verify

# Build with CI profile
mvn -Pci clean verify
```

## Technology Stack

- **Java 17** - Target runtime
- **Maven** - Build tool and dependency management
- **Lombok** - Reduces boilerplate code
- **JPA (Jakarta Persistence)** - Object-relational mapping
- **Hibernate** - JPA implementation
- **Flyway** - Database migration management
- **Testcontainers** - Docker-based integration testing
- **PostgreSQL** - Database for verification tests
- **JUnit 5** - Testing framework
- **Spotless** - Code formatting

## Domain Model Overview

### Aggregates

- **Tenant**: Multi-tenant management with business units and configurations
- **Payment**: Payment initiation and lifecycle management
- **Transaction**: Financial transaction processing with double-entry bookkeeping

### Key Features

- **Multi-tenancy**: Row-level security and tenant isolation
- **Domain Events**: Event-driven architecture for loose coupling
- **Value Objects**: Immutable domain concepts (Money, AccountNumber, etc.)
- **Entity Mapping**: Comprehensive JPA mappings aligned with database schema
- **Validation**: Domain-level validation with custom exceptions

## Development Guidelines

1. **Domain Models**: Follow DDD principles with clear aggregate boundaries
2. **JPA Mappings**: Ensure entity mappings align with Flyway migrations
3. **Testing**: Write both happy-path and negative-path tests
4. **Code Quality**: Maintain Spotless formatting standards
5. **CI/CD**: Ensure all tests pass with Docker enforcement in CI

## Troubleshooting

### Docker Issues

If Docker is not available:
- Tests will be automatically skipped
- Use `mvn -Pci test` to enforce Docker requirement
- Ensure Docker Desktop is running for local development

### Build Issues

```bash
# Clean and rebuild
mvn clean compile

# Check for formatting issues
mvn spotless:check

# Run specific module
mvn -pl domain-models/tenant-management clean compile test
```

### Database Issues

```bash
# Verify Flyway migrations
mvn -pl schema-verification test

# Verify JPA mappings
mvn -pl jpa-verification test
```