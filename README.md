# Payment Engine - ISO 20022 Financial Messaging System

A comprehensive payment processing system built with Spring Boot and React, supporting ISO 20022 standards for financial messaging with advanced resiliency, security, and air-gapped deployment capabilities.

## 🚀 Features

### Core Functionality
- **ISO 20022 Message Support**: pain.001, pacs.008, pacs.002, pain.002, pacs.028, pacs.004, pacs.007, camt.054, camt.055, camt.056, camt.029, status
- **Multi-Scheme Processing**: Support for various clearing systems and payment schemes
- **UETR Management**: Unique End-to-End Transaction Reference tracking and reconciliation
- **Advanced Payload Mapping**: Flexible value assignment with static, derived, and auto-generated values
- **Fraud & Risk Monitoring**: Configurable integration with external fraud APIs
- **Core Banking Integration**: REST API and gRPC support for external core banking systems
- **Tenant Cloning & Migration**: Complete tenant configuration management with versioning, cloning, and environment migration capabilities
- **Advanced Authentication**: JWS (JSON Web Signature) support as alternative to JWT with configurable client headers for outgoing requests

### Architecture & Infrastructure
- **Microservices Architecture**: Service mesh with Istio, dedicated services for auth, config, and monitoring
- **Multi-Tenant Istio**: Host-based routing with automatic conflict resolution for same host + same port scenarios
- **Event Sourcing & CQRS**: Event-driven architecture with event store and projections
- **Resiliency & Self-Healing**: Circuit breakers, retry mechanisms, bulkhead patterns, and automated recovery
- **Security**: OAuth2/JWT/JWS, message encryption, digital signatures, configurable client headers, and comprehensive audit logging
- **Monitoring & Observability**: Prometheus, Grafana, Jaeger, ELK Stack with custom metrics and alerting
- **Air-Gapped Deployment**: Complete offline build and deployment capabilities

### Technology Stack
- **Backend**: Spring Boot 3.x, Spring Data JPA, Spring Security, Spring Cloud OpenFeign
- **Frontend**: React 18, TypeScript, Material-UI, Redux Toolkit, React Query
- **Database**: PostgreSQL 15 with HikariCP connection pooling
- **Message Queue**: Apache Kafka with Schema Registry and Dead Letter Queues
- **Containerization**: Docker with multi-stage builds and security hardening
- **Orchestration**: Kubernetes with Helm charts and Istio service mesh
- **CI/CD**: Azure DevOps with comprehensive testing and security scanning

## 📁 Project Structure

```
payment-engine/
├── services/                    # Backend microservices
│   ├── payment-processing/             # API Gateway and payment-processing services
│   ├── payment-engine/         # Core payment processing service
│   ├── auth-service/           # Authentication and authorization
│   ├── config-service/         # Configuration management
│   └── monitoring-service/     # Metrics and monitoring
├── frontend/                   # React frontend application
├── database/                   # Database migrations and schemas
├── infrastructure/             # Infrastructure as Code
│   ├── kubernetes/            # Kubernetes manifests
│   ├── helm/                  # Helm charts
│   ├── docker/                # Docker configurations
│   └── air-gapped/            # Air-gapped deployment scripts
├── azure-pipelines/           # CI/CD pipeline definitions
├── docs/                      # Documentation
└── tests/                     # Test suites and test data
```

## 🛠️ Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Kubernetes cluster (or Docker Desktop with Kubernetes)
- Helm 3.8+

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd payment-engine
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres redis kafka
   ```

3. **Run backend services**
   ```bash
   cd services
   mvn spring-boot:run -pl payment-processing
   mvn spring-boot:run -pl payment-engine
   ```

4. **Run frontend**
   ```bash
   cd frontend
   npm install
   npm start
   ```

5. **Access the application**
   - Frontend: http://localhost:3000
   - API Gateway: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html

### Production Deployment

#### Standard Deployment
```bash
# Build and deploy using Helm
helm upgrade --install payment-engine helm/payment-engine \
  --values helm/values-production.yaml \
  --namespace payment-engine \
  --create-namespace
```

#### Air-Gapped Deployment
```bash
# Set up air-gapped environment
sudo ./infrastructure/air-gapped/registry-mirror-setup.sh
sudo ./infrastructure/air-gapped/nexus-setup.sh
sudo ./infrastructure/air-gapped/package-repository-setup.sh

# Deploy in air-gapped environment
sudo ./infrastructure/air-gapped/offline-deploy.sh
```

## 📚 Documentation

### Architecture & Design
- [System Architecture](docs/ARCHITECTURE.md) - Overall system design and components
- [Microservices Architecture](docs/MICROSERVICES_ARCHITECTURE.md) - Service decomposition and communication
- [Security Architecture](docs/SECURITY_ARCHITECTURE.md) - Security patterns and implementations
- [Resiliency & Self-Healing](RESILIENCY_AND_SELF_HEALING_GUIDE.md) - Fault tolerance and recovery mechanisms

### Deployment & Operations
- [Air-Gapped Deployment Guide](AIR_GAPPED_DEPLOYMENT_GUIDE.md) - Complete offline deployment procedures
- [Kubernetes Deployment](docs/KUBERNETES_DEPLOYMENT.md) - Container orchestration and management
- [Monitoring & Observability](docs/MONITORING_GUIDE.md) - Metrics, logging, and alerting setup
- [Disaster Recovery](docs/DISASTER_RECOVERY.md) - Backup, restore, and business continuity

### Development & Integration
- [API Documentation](docs/API_DOCUMENTATION.md) - REST API specifications and examples
- [ISO 20022 Message Formats](docs/ISO_20022_MESSAGES.md) - Supported message types and schemas
- [Core Banking Integration](docs/CORE_BANKING_INTEGRATION.md) - External system integration patterns
- [Fraud & Risk Integration](docs/FRAUD_RISK_INTEGRATION.md) - Fraud detection and risk management
- [Tenant Cloning and Migration Guide](TENANT_CLONING_AND_MIGRATION_GUIDE.md) - Complete tenant configuration management
- [JWS and Client Headers Implementation Guide](JWS_AND_CLIENT_HEADERS_IMPLEMENTATION_GUIDE.md) - Advanced authentication and client header configuration
- [Istio Multi-Tenancy Solution](ISTIO_MULTITENANCY_SOLUTION.md) - Multi-tenant Istio configuration with conflict resolution

### Configuration & Customization
- [Configuration Management](docs/CONFIGURATION_GUIDE.md) - Environment-specific configurations
- [Payload Mapping](docs/PAYLOAD_MAPPING.md) - Advanced mapping system documentation
- [UETR Management](docs/UETR_MANAGEMENT.md) - Transaction reference tracking
- [Multi-Tenancy](docs/MULTI_TENANCY.md) - Tenant isolation and configuration

## 🔧 Configuration

### Environment Variables
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=payment_engine
DB_USER=payment_user
DB_PASSWORD=secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_SCHEMA_REGISTRY_URL=http://localhost:8081

# Security Configuration
JWT_SECRET=your-super-secret-jwt-key
JWS_SECRET=your-super-secret-jws-key
JWS_ALGORITHM=HS256
ENCRYPTION_KEY=your-encryption-key

# External Services
FRAUD_API_URL=https://fraud-api.company.com
CORE_BANKING_API_URL=https://core-banking.company.com
```

### Application Properties
```yaml
# application.yml
spring:
  profiles:
    active: production
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}

resilience4j:
  circuitbreaker:
    instances:
      fraud-api:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
```

## 🧪 Testing

### Unit Tests
```bash
# Run all unit tests
mvn test

# Run specific test suite
mvn test -Dtest=PaymentEngineServiceTest
```

### Integration Tests
```bash
# Run integration tests with TestContainers
mvn verify -P integration-tests
```

### Load Tests
```bash
# Run load tests with k6
k6 run tests/load/payment-processing-load-test.js
```

### Security Tests
```bash
# Run security scans
mvn org.owasp:dependency-check-maven:check
npm audit
```

## 🔐 Advanced Authentication & Security

### JWS (JSON Web Signature) Support
- **Enhanced Security**: JWS provides stronger cryptographic signatures compared to standard JWT
- **Algorithm Flexibility**: Support for HMAC (HS256/HS384/HS512) and RSA (RS256/RS384/RS512) algorithms
- **Public Key Verification**: External systems can verify tokens without shared secrets
- **Auto-Detection**: Automatic token type detection for seamless integration

### Configurable Client Headers
- **Tenant-Specific Configuration**: Per-tenant authentication and header configuration
- **Custom Header Names**: Configurable client ID and secret header names
- **Outgoing Request Enhancement**: Automatic inclusion of client credentials in HTTP requests
- **Multiple Auth Methods**: Support for JWT, JWS, OAuth2, API Key, and Basic Authentication

### Authentication Configuration Management
- **Frontend Interface**: React-based configuration management UI
- **Real-time Updates**: Dynamic configuration changes without service restart
- **Audit Trail**: Complete audit logging of configuration changes
- **Role-based Access**: Secure access control for configuration management

### Example Configuration
```json
{
  "tenantId": "tenant-001",
  "authMethod": "JWS",
  "jwsAlgorithm": "RS256",
  "includeClientHeaders": true,
  "clientId": "client-123",
  "clientSecret": "secret-456",
  "clientIdHeaderName": "X-Client-ID",
  "clientSecretHeaderName": "X-Client-Secret"
}
```

## 🌐 Multi-Tenant Istio Configuration

### Host-Based Routing
- **Tenant Isolation**: Each tenant gets unique subdomains (tenant-001.payment-engine.local)
- **Environment Separation**: Different environments (dev, staging, prod) with separate routing
- **Conflict Resolution**: Automatic resolution of same host + same port conflicts
- **Security Isolation**: Tenant-specific security policies and mTLS enforcement

### Deployment Commands
```bash
# Deploy multi-tenant Istio configuration
./scripts/deploy-multitenant-istio.sh

# Generate configuration for new tenant
./scripts/generate-tenant-istio-config.sh tenant-001 --environment dev

# Test multi-tenancy setup
./scripts/test-istio-multitenancy.sh --ingress-ip <gateway-ip>
```

### Access URLs
- **Tenant-001**: https://tenant-001.payment-engine.local
- **Tenant-002**: https://tenant-002.payment-engine.local
- **Development**: https://tenant-001.dev.payment-engine.local
- **Staging**: https://tenant-001.staging.payment-engine.local
- **Production**: https://tenant-001.prod.payment-engine.local

## 📊 Monitoring & Observability

### Metrics
- **Application Metrics**: Custom business metrics via Micrometer
- **Infrastructure Metrics**: System metrics via Prometheus
- **Performance Metrics**: Response times, throughput, and error rates

### Logging
- **Structured Logging**: JSON format with correlation IDs
- **Centralized Logging**: ELK Stack for log aggregation and analysis
- **Audit Logging**: Comprehensive audit trail for compliance

### Tracing
- **Distributed Tracing**: Jaeger for request flow analysis
- **Span Management**: Automatic instrumentation and context propagation
- **Trace Analytics**: Performance bottleneck identification

### Alerting
- **Health Checks**: Application and infrastructure health monitoring
- **Custom Alerts**: Business logic and performance threshold alerts
- **Incident Response**: Automated escalation and notification

## 🔒 Security

### Authentication & Authorization
- **OAuth2/JWT**: Token-based authentication with refresh tokens
- **Role-Based Access Control**: Granular permissions and access control
- **Multi-Factor Authentication**: Enhanced security for sensitive operations

### Data Protection
- **Message Encryption**: AES-GCM encryption for sensitive data
- **Digital Signatures**: RSA signatures for message integrity
- **Key Management**: Azure Key Vault integration for secure key storage

### Compliance
- **Audit Logging**: Comprehensive audit trail for regulatory compliance
- **Data Retention**: Configurable data retention policies
- **Privacy Controls**: GDPR and data protection compliance

## 🚀 CI/CD Pipeline

### Azure DevOps Pipeline
- **Build Stages**: Multi-stage builds with dependency caching
- **Test Stages**: Unit, integration, and security testing
- **Deploy Stages**: Environment-specific deployments
- **Quality Gates**: Code quality and security checks

### Air-Gapped Pipeline
- **Offline Package Management**: Complete offline dependency resolution
- **Container Registry Mirroring**: Local registry for air-gapped environments
- **Deployment Packages**: Self-contained deployment artifacts

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow the existing code style and conventions
- Write comprehensive tests for new features
- Update documentation for any API changes
- Ensure all security checks pass

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

### Documentation
- [FAQ](docs/FAQ.md) - Frequently asked questions
- [Troubleshooting Guide](docs/TROUBLESHOOTING.md) - Common issues and solutions
- [Performance Tuning](docs/PERFORMANCE_TUNING.md) - Optimization guidelines

### Community
- [GitHub Issues](https://github.com/your-org/payment-engine/issues) - Bug reports and feature requests
- [Discussions](https://github.com/your-org/payment-engine/discussions) - Community discussions
- [Wiki](https://github.com/your-org/payment-engine/wiki) - Community-maintained documentation

### Professional Support
For enterprise support, training, or consulting services, please contact the development team.

---

**Built with ❤️ for the financial services industry**