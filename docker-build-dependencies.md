# Docker Build Dependencies and Sequence

## Analysis of Current Docker Build Issues

Based on the analysis of the Dockerfiles and Maven dependencies, here's the dependency structure and optimal build sequence:

## Dependency Hierarchy

### Level 1: Foundation Modules (No Dependencies)
1. **domain-models/shared** - Core shared domain models
2. **contracts** - API contracts and DTOs
3. **shared-telemetry** - OpenTelemetry instrumentation
4. **shared-config** - Configuration management

### Level 2: Domain Models (Depend on Level 1)
5. **domain-models/payment-initiation** - Payment domain models
6. **domain-models/validation** - Validation domain models
7. **domain-models/account-adapter** - Account adapter domain models
8. **domain-models/transaction-processing** - Transaction domain models
9. **domain-models/saga-orchestrator** - Saga domain models
10. **domain-models/tenant-management** - Tenant management domain models
11. **domain-models/clearing-adapter** - Clearing adapter domain models

### Level 3: Services (Depend on Level 1 & 2)
12. **payment-initiation-service** - Depends on: contracts, payment-initiation domain
13. **validation-service** - Depends on: contracts, shared domain
14. **account-adapter-service** - Depends on: contracts, shared domain
15. **routing-service** - Depends on: contracts, shared domain
16. **transaction-processing-service** - Depends on: shared domain
17. **saga-orchestrator** - Depends on: shared domain

## Current Docker Build Issues

### Problem 1: Inconsistent Build Strategies
- **payment-initiation-service** and **validation-service**: Use comprehensive dependency installation
- **Other services**: Use simple module-only builds (will fail due to missing dependencies)

### Problem 2: Missing Dependency Installation
Most services try to build without installing their dependencies first:
```dockerfile
# This will fail for most services
RUN mvn -f service-name/pom.xml clean package -DskipTests
```

## Recommended Docker Build Sequence

### Phase 1: Foundation (Build in parallel)
```bash
# Build foundation modules first
docker build -f docker/shared-telemetry/Dockerfile .
docker build -f docker/shared-config/Dockerfile .
docker build -f docker/contracts/Dockerfile .
docker build -f docker/domain-models-shared/Dockerfile .
```

### Phase 2: Domain Models (Build in parallel)
```bash
# Build domain model modules
docker build -f docker/domain-models-payment-initiation/Dockerfile .
docker build -f docker/domain-models-validation/Dockerfile .
docker build -f docker/domain-models-account-adapter/Dockerfile .
docker build -f docker/domain-models-transaction-processing/Dockerfile .
docker build -f docker/domain-models-saga-orchestrator/Dockerfile .
```

### Phase 3: Services (Build in parallel)
```bash
# Build service modules
docker build -f docker/payment-initiation-service/Dockerfile .
docker build -f docker/validation-service/Dockerfile .
docker build -f docker/account-adapter-service/Dockerfile .
docker build -f docker/routing-service/Dockerfile .
docker build -f docker/transaction-processing-service/Dockerfile .
docker build -f docker/saga-orchestrator/Dockerfile .
```

## Immediate Fixes Required

### 1. Standardize All Dockerfiles
All service Dockerfiles need the same comprehensive dependency installation pattern:

```dockerfile
# Pre-build shared modules into local Maven repository
RUN mvn -N -f pom.xml install -DskipTests -Dspotless.skip=true \
    && mvn -N -f domain-models/pom.xml install -DskipTests -Dspotless.skip=true \
    && mvn -f domain-models/shared/pom.xml clean install -DskipTests -Dspotless.skip=true \
    && mvn -f contracts/pom.xml clean install -DskipTests -Dspotless.skip=true \
    && mvn -f shared-telemetry/pom.xml clean install -DskipTests -Dspotless.skip=true \
    && mvn -f shared-config/pom.xml clean install -DskipTests -Dspotless.skip=true \
    && mvn -f domain-models/[SERVICE-DOMAIN]/pom.xml clean install -DskipTests -Dspotless.skip=true \
    && mvn -f [SERVICE-NAME]/pom.xml clean package -DskipTests -Dspotless.skip=true
```

### 2. Create Missing Dockerfiles
Need to create Dockerfiles for:
- `docker/shared-telemetry/Dockerfile`
- `docker/shared-config/Dockerfile`
- `docker/contracts/Dockerfile`
- `docker/domain-models-shared/Dockerfile`

### 3. Update docker-compose.yml
Add build dependencies to ensure proper build order:

```yaml
services:
  payment-initiation-service:
    build:
      context: .
      dockerfile: docker/payment-initiation-service/Dockerfile
    depends_on:
      - postgres
      - redis
      - kafka
```

## Optimal Build Strategy

### Option A: Single Build Context (Current Approach)
- Copy entire project into Docker context
- Install all dependencies in each service build
- Pros: Simple, works for all services
- Cons: Large build context, slower builds

### Option B: Multi-Stage with Base Images
- Create base images for each dependency level
- Use multi-stage builds with FROM base images
- Pros: Faster builds, better caching
- Cons: More complex setup

### Option C: Build Script Approach
- Build all modules locally first
- Copy only built artifacts to Docker
- Pros: Fastest builds
- Cons: Requires local Maven installation

## Recommended Immediate Actions

1. **Fix all service Dockerfiles** to use the comprehensive dependency installation pattern
2. **Create missing foundation Dockerfiles** for shared modules
3. **Test the build sequence** with a single service first
4. **Implement proper build dependencies** in docker-compose.yml
5. **Add build caching** for better performance

## Build Performance Optimization

### Layer Caching Strategy
```dockerfile
# Copy POMs first for better caching
COPY pom.xml .
COPY domain-models/pom.xml domain-models/
COPY contracts/pom.xml contracts/
COPY shared-telemetry/pom.xml shared-telemetry/
COPY shared-config/pom.xml shared-config/
COPY [service]/pom.xml [service]/

# Install dependencies (this layer will be cached)
RUN mvn dependency:go-offline -f [service]/pom.xml

# Copy source code (this layer changes frequently)
COPY [service]/ [service]/

# Build application
RUN mvn -f [service]/pom.xml clean package -DskipTests -Dspotless.skip=true
```

This approach ensures that dependency downloads are cached and only source code changes trigger rebuilds.
