# Payments Engine - Domain Models

A multi-module Maven project implementing a payments engine with domain-driven design principles, JPA entity mappings, and comprehensive test coverage.

## Project Structure

```
payments-engine/
├── domain-models/           # Domain model modules
│   ├── shared/             # Shared value objects and types
│   ├── tenant-management/  # Tenant aggregate and related entities
│   ├── payment-initiation/ # Payment aggregate and events
│   ├── transaction-processing/ # Transaction aggregate and ledger entries
│   ├── validation/         # Payment validation rules
│   ├── clearing-adapter/   # Clearing system integration
│   ├── account-adapter/    # Account system integration
│   └── saga-orchestrator/  # Saga orchestration
├── schema-verification/    # Flyway migration validation
├── jpa-verification/       # JPA entity mapping validation
└── database-migrations/    # Flyway SQL migration scripts
```

## Prerequisites

- **Java 17** or higher
- **Maven 3.9+**
- **Docker** (for schema and JPA verification tests)

## Quick Start

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