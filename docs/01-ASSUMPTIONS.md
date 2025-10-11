# Payments Engine - Comprehensive Assumptions

## Document Purpose
This document lists ALL assumptions made during the architecture design. Review each assumption and modify as needed to adjust the design accordingly.

---

## 1. Business Context Assumptions

### 1.1 Organization
- **ASSUMPTION**: Building for a licensed financial institution in South Africa
- **ASSUMPTION**: Organization has existing banking license from SARB (South African Reserve Bank)
- **ASSUMPTION**: Organization is a registered participant in South African clearing systems
- **ASSUMPTION**: Organization has multiple existing core banking systems managing different account types
- **RATIONALE**: Required for direct integration with SAMOS, BankservAfrica, RTC

### 1.2 Payment Types in Scope
- **ASSUMPTION**: Supporting domestic South African payments only (no cross-border)
- **ASSUMPTION**: Payment types include:
  - Retail EFT (Electronic Funds Transfer)
  - Real-Time Clearing (RTC) payments
  - High-value RTGS via SAMOS
  - Card payments via SASWITCH
  - Debit orders via DebiCheck
- **ASSUMPTION**: NOT supporting:
  - International SWIFT payments (out of scope)
  - Cryptocurrency (out of scope)
  - Cash deposits/withdrawals (out of scope)

### 1.3 Transaction Volumes
- **ASSUMPTION**: Expected volume: 10,000 transactions per second (peak)
- **ASSUMPTION**: Daily transaction volume: ~50 million transactions
- **ASSUMPTION**: Average transaction value: R1,000 - R50,000
- **ASSUMPTION**: High-value transactions (>R5m): < 1% of volume
- **RATIONALE**: Determines infrastructure sizing, caching strategy, database sharding

### 1.4 User Base
- **ASSUMPTION**: 10 million active users
- **ASSUMPTION**: Users are primarily individuals and SMEs
- **ASSUMPTION**: Users access via web and mobile applications
- **ASSUMPTION**: Support for both technical (APIs) and non-technical users (UI)

### 1.4.1 Customer Limits and Controls
- **ASSUMPTION**: Each customer has configurable transaction limits
- **ASSUMPTION**: Limits are enforced at multiple levels:
  - **Per Payment Type**: Different limits for EFT, RTC, RTGS, Debit Orders
  - **Daily Limits**: Maximum amount per day across all payment types
  - **Monthly Limits**: Maximum amount per month across all payment types
  - **Per Transaction Limits**: Maximum amount per single transaction
  - **Transaction Count Limits**: Maximum number of transactions per period
- **ASSUMPTION**: Default limits by customer profile:
  - **Individual - Standard**: Daily R50,000, Monthly R200,000
  - **Individual - Premium**: Daily R100,000, Monthly R500,000
  - **SME**: Daily R500,000, Monthly R2,000,000
  - **Corporate**: Daily R5,000,000, Monthly R50,000,000
- **ASSUMPTION**: Limits are checked in real-time before payment execution
- **ASSUMPTION**: Used limits are tracked and updated immediately after successful payment
- **ASSUMPTION**: Limits reset automatically based on period (daily at midnight, monthly on 1st)
- **ASSUMPTION**: Payments fail if used limit + payment amount exceeds configured limit
- **ASSUMPTION**: Customers can view their limits and usage via API/UI
- **ASSUMPTION**: Limit increases require approval workflow (out of scope for initial release)
- **RATIONALE**: Risk management, regulatory compliance, fraud prevention

### 1.5 External Core Banking Systems
- **ASSUMPTION**: Accounts are NOT stored in the payments engine
- **ASSUMPTION**: Multiple external "Store of Value" systems exist:
  - **Current Accounts System**: Manages transactional current accounts
  - **Savings Accounts System**: Manages savings accounts
  - **Investment Accounts System**: Manages investment portfolios
  - **Card Accounts System**: Manages credit/debit card accounts
  - **Home Loan System**: Manages home loan accounts
  - **Car Loan System**: Manages vehicle finance accounts
  - **Personal Loan System**: Manages personal loans
  - **Business Banking System**: Manages corporate accounts
- **ASSUMPTION**: Each system exposes REST APIs for:
  - Account inquiry (balance, status, details)
  - Debit operations (withdraw funds)
  - Credit operations (deposit funds)
  - Account holds/reserves (temporary locks)
- **ASSUMPTION**: Each system manages its own:
  - Account balances
  - Transaction history
  - Account limits and rules
  - Account ownership and KYC
- **ASSUMPTION**: APIs are synchronous (REST) with < 500ms response time
- **ASSUMPTION**: All systems support idempotency via idempotency keys
- **ASSUMPTION**: All systems provide real-time balance updates
- **ASSUMPTION**: Each system may have different availability (99.9%+ expected)
- **RATIONALE**: Common enterprise banking architecture with specialized systems

---

## 2. Technical Architecture Assumptions

### 2.1 Cloud Provider
- **ASSUMPTION**: Microsoft Azure is the chosen cloud provider
- **ASSUMPTION**: Primary region: South Africa North (Johannesburg)
- **ASSUMPTION**: Secondary region: South Africa West (Cape Town)
- **RATIONALE**: Data sovereignty requirements, Azure's presence in SA
- **ALTERNATIVE**: Could use AWS or GCP with similar services

### 2.2 Technology Stack

#### Backend
- **ASSUMPTION**: Java 17+ and Spring Boot 3.x for all backend services
- **ASSUMPTION**: Spring Cloud for microservices patterns (Gateway, Config, Discovery)
- **ASSUMPTION**: JPA/Hibernate for ORM
- **RATIONALE**: Enterprise-grade, mature ecosystem, excellent tooling
- **ALTERNATIVE**: Could use .NET, Go, or Node.js

#### Frontend
- **ASSUMPTION**: React 18+ with TypeScript
- **ASSUMPTION**: Single Page Application (SPA) architecture
- **ASSUMPTION**: Progressive Web App (PWA) for mobile
- **RATIONALE**: Modern, component-based, excellent ecosystem
- **ALTERNATIVE**: Could use Angular, Vue, or Next.js

#### Databases
- **ASSUMPTION**: PostgreSQL for transactional data (ACID compliance)
- **ASSUMPTION**: CosmosDB for audit logs (high-write scenarios)
- **ASSUMPTION**: Redis for caching and session management
- **RATIONALE**: PostgreSQL = proven reliability, CosmosDB = scalability
- **ALTERNATIVE**: Could use MySQL, SQL Server, or MongoDB

### 2.3 Communication Patterns
- **ASSUMPTION**: Azure Service Bus for asynchronous messaging
- **ASSUMPTION**: REST APIs for synchronous communication
- **ASSUMPTION**: gRPC for high-performance inter-service calls
- **ASSUMPTION**: WebSockets for real-time notifications to frontend
- **RATIONALE**: Balance between ease of use and performance

### 2.4 Microservices Size
- **ASSUMPTION**: Each microservice < 500 lines of core business logic
- **ASSUMPTION**: Each microservice can be built in 2-4 hours by an AI agent
- **ASSUMPTION**: Total of ~20 microservices
- **RATIONALE**: Optimal for AI agent comprehension and development

---

## 3. South African Clearing System Assumptions

### 3.1 SAMOS Integration
- **ASSUMPTION**: Have SWIFT connectivity and SAMOS credentials
- **ASSUMPTION**: Using ISO 20022 messages (pacs.008, pacs.002)
- **ASSUMPTION**: Processing high-value payments only (> R5 million)
- **ASSUMPTION**: Settlement windows: 08:00-15:30 CAT (South African time)
- **RATIONALE**: SARB SAMOS operational hours

### 3.2 BankservAfrica Integration
- **ASSUMPTION**: Direct participation agreement with BankservAfrica
- **ASSUMPTION**: Supporting ACH/EFT batch processing
- **ASSUMPTION**: Using BankservAfrica proprietary format + ISO 8583
- **ASSUMPTION**: Batch cutoff times: 08:00, 10:00, 12:00, 14:00 CAT
- **ASSUMPTION**: Settlement T+0 for RTC, T+1 for ACH
- **RATIONALE**: Standard BankservAfrica operating procedures

### 3.3 Real-Time Clearing (RTC)
- **ASSUMPTION**: RTC available 24/7/365
- **ASSUMPTION**: Maximum transaction limit: R5 million
- **ASSUMPTION**: Response time: < 10 seconds
- **ASSUMPTION**: Using ISO 20022 messages
- **ASSUMPTION**: Instant settlement (gross settlement)

### 3.4 SASWITCH (Card Payments)
- **ASSUMPTION**: PCI DSS Level 1 compliance achieved
- **ASSUMPTION**: Using ISO 8583 standard for card transactions
- **ASSUMPTION**: Supporting local South African card schemes
- **ASSUMPTION**: Not tokenizing card data (using third-party tokenization)
- **RATIONALE**: Reduce PCI scope, use specialized providers

---

## 4. Security & Compliance Assumptions

### 4.1 Regulatory Compliance
- **ASSUMPTION**: Must comply with:
  - POPIA (Protection of Personal Information Act)
  - FICA (Financial Intelligence Centre Act)
  - SARB Position Papers
  - NPS Act (National Payment System Act)
  - PCI DSS Level 1 (for card payments)
- **ASSUMPTION**: Audit trails must be retained for 7 years
- **ASSUMPTION**: Transaction data must remain in South Africa (data sovereignty)

### 4.2 Authentication & Authorization
- **ASSUMPTION**: Using OAuth2 + OpenID Connect (OIDC)
- **ASSUMPTION**: Multi-factor authentication (MFA) required for all users
- **ASSUMPTION**: Azure AD B2C for identity management
- **ASSUMPTION**: JWT tokens with 15-minute expiry, refresh tokens 7 days
- **ASSUMPTION**: Role-based access control (RBAC) with fine-grained permissions

### 4.3 Data Encryption
- **ASSUMPTION**: TLS 1.3 for data in transit
- **ASSUMPTION**: AES-256 encryption for data at rest
- **ASSUMPTION**: Azure Key Vault for key management
- **ASSUMPTION**: Secrets rotation every 90 days
- **ASSUMPTION**: No sensitive data in logs or events

### 4.4 Fraud Prevention
- **ASSUMPTION**: Integration with external third-party fraud scoring API
- **ASSUMPTION**: Fraud API provider: Third-party SaaS (e.g., Simility, Feedzai, SAS, or similar)
- **ASSUMPTION**: Real-time fraud scoring for ALL transactions before execution
- **ASSUMPTION**: Fraud API response time SLA: < 500ms (p95)
- **ASSUMPTION**: Fraud scoring is synchronous (REST API call)
- **ASSUMPTION**: Fraud score range: 0.0 (no risk) to 1.0 (highest risk)
- **ASSUMPTION**: Risk thresholds:
  - Score 0.0-0.3: LOW risk (auto-approve)
  - Score 0.3-0.6: MEDIUM risk (auto-approve with monitoring)
  - Score 0.6-0.8: HIGH risk (require additional verification)
  - Score 0.8-1.0: CRITICAL risk (auto-reject)
- **ASSUMPTION**: Fraud API provides:
  - Fraud score
  - Risk level
  - Fraud indicators/reasons
  - Recommended actions
- **ASSUMPTION**: Fallback if fraud API unavailable:
  - Allow transactions to proceed (fail-open strategy)
  - OR Use rule-based fraud detection (configurable)
- **ASSUMPTION**: Fraud API authentication: API Key or OAuth 2.0
- **ASSUMPTION**: Fraud API input includes:
  - Customer ID
  - Account numbers (source, destination)
  - Transaction amount
  - Payment type
  - Device fingerprint
  - IP address
  - Geolocation
  - Historical transaction patterns
- **ASSUMPTION**: Velocity checks performed by fraud API
- **ASSUMPTION**: Fraud API cost: $0.01-0.05 per API call
- **ASSUMPTION**: Circuit breaker for fraud API (fallback to rule-based)
- **RATIONALE**: Real-time fraud prevention, reduce chargebacks and losses

---

## 5. Performance & Scalability Assumptions

### 5.1 Performance Targets
- **ASSUMPTION**: API response time: < 200ms (p95)
- **ASSUMPTION**: End-to-end payment completion: < 10 seconds for RTC
- **ASSUMPTION**: System availability: 99.95% (4.38 hours downtime/year)
- **ASSUMPTION**: Database query time: < 50ms (p95)

### 5.2 Scalability Strategy
- **ASSUMPTION**: Horizontal scaling for all services
- **ASSUMPTION**: Auto-scaling based on CPU (70% threshold) and queue depth
- **ASSUMPTION**: Maximum 50 pod replicas per service in Kubernetes
- **ASSUMPTION**: Database read replicas for read-heavy services

### 5.3 Caching Strategy
- **ASSUMPTION**: Redis cache for:
  - Account balance lookups (TTL: 30 seconds)
  - Validation rules (TTL: 5 minutes)
  - Routing tables (TTL: 1 hour)
  - User sessions (TTL: per JWT expiry)
- **ASSUMPTION**: Cache hit ratio target: > 80%

---

## 6. Data Management Assumptions

### 6.1 Database Strategy
- **ASSUMPTION**: Database per service pattern (no shared databases)
- **ASSUMPTION**: Each service owns its data schema
- **ASSUMPTION**: No direct database access between services
- **ASSUMPTION**: Data sharing via events and APIs only

### 6.2 Data Consistency
- **ASSUMPTION**: Eventual consistency is acceptable for most operations
- **ASSUMPTION**: Strong consistency required for:
  - Account balance updates
  - Transaction state changes
  - Settlement calculations
- **ASSUMPTION**: Using distributed transactions (Saga pattern) for multi-service flows
- **ASSUMPTION**: Idempotency for all API operations (using idempotency keys)

### 6.3 Event Sourcing
- **ASSUMPTION**: Event sourcing for critical payment entities
- **ASSUMPTION**: Events are immutable and append-only
- **ASSUMPTION**: Event retention: indefinite (compliance requirement)
- **ASSUMPTION**: Event replay capability for debugging and auditing

### 6.4 Data Retention
- **ASSUMPTION**: Transactional data: 7 years (regulatory requirement)
- **ASSUMPTION**: Audit logs: 7 years
- **ASSUMPTION**: User session data: 30 days
- **ASSUMPTION**: Application logs: 90 days
- **ASSUMPTION**: Archived data moved to cold storage after 2 years

---

## 7. Integration Assumptions

### 7.1 External Core Banking Systems Integration
- **ASSUMPTION**: Each core banking system provides REST APIs with JSON payload
- **ASSUMPTION**: Standard API contract across all systems (unified interface)
- **ASSUMPTION**: Authentication via OAuth 2.0 client credentials or mTLS
- **ASSUMPTION**: Response time SLA: < 500ms (p95) for balance inquiry, < 2s for debit/credit
- **ASSUMPTION**: Each system supports circuit breaker pattern for resilience
- **ASSUMPTION**: Retry logic: 3 attempts with exponential backoff for transient errors
- **ASSUMPTION**: Timeout for core banking calls: 5 seconds
- **ASSUMPTION**: Each system provides health check endpoints
- **ASSUMPTION**: Account numbers are unique across ALL systems (or prefixed by system ID)
- **ASSUMPTION**: API versioning supported (v1, v2, etc.)

#### Core Banking API Contract Example
```json
// Debit Request
POST /api/v1/accounts/{accountNumber}/debit
{
  "idempotencyKey": "uuid",
  "amount": 1000.00,
  "currency": "ZAR",
  "reference": "PAY-2025-XXXXXX",
  "description": "Payment to merchant"
}

// Credit Request
POST /api/v1/accounts/{accountNumber}/credit
{
  "idempotencyKey": "uuid",
  "amount": 1000.00,
  "currency": "ZAR",
  "reference": "PAY-2025-XXXXXX",
  "description": "Payment received"
}

// Balance Inquiry
GET /api/v1/accounts/{accountNumber}/balance

// Hold/Reserve Funds
POST /api/v1/accounts/{accountNumber}/holds
{
  "amount": 1000.00,
  "reference": "PAY-2025-XXXXXX",
  "expiryMinutes": 30
}
```

### 7.2 Clearing System Integration
- **ASSUMPTION**: All clearing systems provide API/message-based integration
- **ASSUMPTION**: Clearing systems may be synchronous or asynchronous
- **ASSUMPTION**: Timeout for clearing calls: 30 seconds
- **ASSUMPTION**: Retry logic: 3 attempts with exponential backoff

### 7.3 Message Formats
- **ASSUMPTION**: ISO 20022 for SAMOS and RTC
- **ASSUMPTION**: ISO 8583 for card payments
- **ASSUMPTION**: Proprietary formats for some BankservAfrica messages
- **ASSUMPTION**: Internal services use JSON for events
- **ASSUMPTION**: Core banking systems use JSON/REST (not ISO 20022)

### 7.4 Idempotency
- **ASSUMPTION**: All external system calls are idempotent
- **ASSUMPTION**: Using unique transaction IDs for deduplication
- **ASSUMPTION**: Idempotency key stored for 24 hours
- **ASSUMPTION**: Core banking systems deduplicate using idempotency keys

---

## 8. AI Agent Development Assumptions

### 8.1 AI Agent Capabilities
- **ASSUMPTION**: AI agents can understand OpenAPI specifications
- **ASSUMPTION**: AI agents can write unit tests (80%+ coverage)
- **ASSUMPTION**: AI agents can follow coding standards and conventions
- **ASSUMPTION**: AI agents can generate sample data and mocks
- **ASSUMPTION**: Each AI agent works independently without coordination

### 8.2 Module Independence
- **ASSUMPTION**: Each module has:
  - Clear interface contract (OpenAPI/AsyncAPI)
  - No circular dependencies
  - Mock implementations for dependencies
  - Self-contained tests
  - README with setup instructions

### 8.3 Development Timeline
- **ASSUMPTION**: Each service takes 2-4 hours to build by AI agent
- **ASSUMPTION**: Total of 20 services = 40-80 agent-hours
- **ASSUMPTION**: Consolidation takes 8-16 hours
- **ASSUMPTION**: Total project time: 50-100 agent-hours

### 8.4 Quality Standards
- **ASSUMPTION**: All code must pass:
  - Unit tests (80%+ coverage)
  - Integration tests
  - Static code analysis (SonarQube)
  - Security scanning (OWASP Dependency Check)
  - API contract validation

---

## 9. Operational Assumptions

### 9.1 Deployment
- **ASSUMPTION**: Kubernetes (AKS) for container orchestration
- **ASSUMPTION**: Blue-green deployment strategy
- **ASSUMPTION**: Canary releases for critical services
- **ASSUMPTION**: Automated rollback on failure
- **ASSUMPTION**: Zero-downtime deployments

### 9.2 Monitoring & Observability
- **ASSUMPTION**: Distributed tracing with OpenTelemetry
- **ASSUMPTION**: Centralized logging with Azure Log Analytics
- **ASSUMPTION**: Metrics collection with Prometheus/Azure Monitor
- **ASSUMPTION**: Alerting via Azure Monitor
- **ASSUMPTION**: APM with Application Insights

### 9.3 Incident Response
- **ASSUMPTION**: 24/7 on-call support rotation
- **ASSUMPTION**: Incident response time: < 15 minutes (P1), < 1 hour (P2)
- **ASSUMPTION**: Automated incident creation from alerts
- **ASSUMPTION**: Post-incident reviews for all P1/P2 incidents

### 9.4 Disaster Recovery
- **ASSUMPTION**: RTO (Recovery Time Objective): 1 hour
- **ASSUMPTION**: RPO (Recovery Point Objective): 5 minutes
- **ASSUMPTION**: Multi-region active-passive deployment
- **ASSUMPTION**: Automated failover for critical services
- **ASSUMPTION**: Monthly DR drills

---

## 10. Cost Assumptions

### 10.1 Infrastructure Costs
- **ASSUMPTION**: Azure budget: ~$50,000/month for production
- **ASSUMPTION**: Cost breakdown:
  - Compute (AKS): 40%
  - Databases: 30%
  - Networking: 15%
  - Storage: 10%
  - Other services: 5%

### 10.2 Third-Party Costs
- **ASSUMPTION**: Clearing system fees: per-transaction basis
- **ASSUMPTION**: Fraud detection: monthly license + per-transaction
- **ASSUMPTION**: SMS/Email: per-message pricing
- **ASSUMPTION**: Monitoring tools: per-user/per-GB pricing

---

## 11. Frontend Assumptions

### 11.1 User Interface
- **ASSUMPTION**: Responsive design (mobile-first)
- **ASSUMPTION**: Support for modern browsers (Chrome, Firefox, Safari, Edge)
- **ASSUMPTION**: No support for IE11
- **ASSUMPTION**: Accessibility compliance (WCAG 2.1 Level AA)
- **ASSUMPTION**: Internationalization (i18n) with English and Afrikaans

### 11.2 State Management
- **ASSUMPTION**: Redux Toolkit for global state
- **ASSUMPTION**: RTK Query for API caching
- **ASSUMPTION**: Local storage for user preferences
- **ASSUMPTION**: Session storage for temporary data

### 11.3 Real-Time Updates
- **ASSUMPTION**: WebSocket connection for transaction status
- **ASSUMPTION**: Fallback to polling (every 5 seconds) if WebSocket unavailable
- **ASSUMPTION**: Push notifications for mobile (PWA)

---

## 12. Testing Assumptions

### 12.1 Testing Strategy
- **ASSUMPTION**: Pyramid testing approach:
  - 70% unit tests
  - 20% integration tests
  - 10% end-to-end tests
- **ASSUMPTION**: Minimum 80% code coverage
- **ASSUMPTION**: Contract testing for inter-service communication
- **ASSUMPTION**: Performance testing for all critical paths

### 12.2 Test Environments
- **ASSUMPTION**: Environments: Dev, Test, Staging, Production
- **ASSUMPTION**: Production-like data in Staging
- **ASSUMPTION**: Synthetic data in Dev/Test
- **ASSUMPTION**: Automated testing in CI/CD pipeline

### 12.3 Load Testing
- **ASSUMPTION**: Load tests simulating 2x peak load
- **ASSUMPTION**: Stress tests to find breaking points
- **ASSUMPTION**: Soak tests running for 24 hours
- **ASSUMPTION**: Chaos engineering (random failures)

---

## 13. Documentation Assumptions

### 13.1 Technical Documentation
- **ASSUMPTION**: All APIs documented with OpenAPI 3.0
- **ASSUMPTION**: All events documented with AsyncAPI 2.0
- **ASSUMPTION**: Architecture Decision Records (ADRs) for major decisions
- **ASSUMPTION**: README.md for each service
- **ASSUMPTION**: Database schema documentation

### 13.2 User Documentation
- **ASSUMPTION**: User guides for frontend applications
- **ASSUMPTION**: API documentation for developers
- **ASSUMPTION**: Integration guides for third parties
- **ASSUMPTION**: Troubleshooting guides

---

## 14. Limitations & Known Issues

### 14.1 Current Limitations
- **ASSUMPTION**: No support for offline transactions
- **ASSUMPTION**: No support for batch reversals (manual process)
- **ASSUMPTION**: Limited support for complex reconciliation rules
- **ASSUMPTION**: No AI-based fraud detection (using rule-based initially)

### 14.2 Future Enhancements (Out of Scope)
- Cross-border payments
- Cryptocurrency support
- Advanced analytics and ML
- White-label solutions
- Mobile SDK for third parties

---

## 15. Risk Assumptions

### 15.1 Technical Risks
- **ASSUMPTION**: Risk: External clearing systems downtime
  - **MITIGATION**: Queue messages, retry with exponential backoff
- **ASSUMPTION**: Risk: Database performance degradation
  - **MITIGATION**: Read replicas, caching, database tuning
- **ASSUMPTION**: Risk: Message loss in event bus
  - **MITIGATION**: Persistent messages, duplicate detection

### 15.2 Business Risks
- **ASSUMPTION**: Risk: Regulatory changes
  - **MITIGATION**: Configurable validation rules, external config
- **ASSUMPTION**: Risk: Fraud attacks
  - **MITIGATION**: Multi-layer fraud detection, rate limiting
- **ASSUMPTION**: Risk: Vendor lock-in (Azure)
  - **MITIGATION**: Abstraction layers, multi-cloud capability (future)

---

## Review Checklist

Before proceeding with implementation, review:

1. ☐ Business context aligns with your organization
2. ☐ Payment types match your requirements
3. ☐ Volume assumptions are realistic
4. ☐ Technology stack is approved
5. ☐ Clearing system integrations are feasible
6. ☐ Compliance requirements are complete
7. ☐ Performance targets are achievable
8. ☐ Security measures are sufficient
9. ☐ Cost estimates are within budget
10. ☐ AI agent capabilities match available tooling

---

## Change Management

To modify an assumption:
1. Document the change reason
2. Update this document
3. Review impacted architecture components
4. Update dependent documents (API contracts, schemas, etc.)
5. Notify all AI agents working on affected modules

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Author**: MAANG Software Architect
