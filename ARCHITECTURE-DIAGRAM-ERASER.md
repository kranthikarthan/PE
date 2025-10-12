# Payments Engine - Architecture Diagram (Eraser.ai)

## Overview

This document contains the Eraser.ai diagram code for the complete Payments Engine architecture with all 20 microservices.

---
<img width="4133" height="5532" alt="17602438426981324194820087386356" src="https://github.com/user-attachments/assets/7cc23307-a86c-4e8d-9e76-ae60e074e294" />

## How to Use

1. Go to https://app.eraser.io/
2. Create a new diagram
3. Select "Diagram as Code"
4. Copy the code below and paste it into Eraser.ai
5. The diagram will be generated automatically

---

## Eraser.ai Diagram Code

### Main Architecture Diagram

```eraser
// Payments Engine - Complete Architecture (20 Microservices)
// Generated: 2025-10-11

title Payments Engine - Complete Architecture (South Africa)

// === CHANNEL LAYER ===
cloud Channels {
  component "Web Portal" as web [
    React 18+
    Redux
    Tailwind
  ]
  
  component "Mobile App" as mobile [
    React Native
    PWA
  ]
  
  component "API Gateway" as apigw [
    Azure APIM
    OAuth 2.0
    Rate Limiting
  ]
  
  component "SFTP Server" as sftp [
    Batch File Upload
    Corporate Clients
  ]
}

// === API GATEWAY & ORCHESTRATION ===
cloud "API & Orchestration Layer" as gateway_layer {
  component "API Gateway Service" as api_gateway [
    #18
    Spring Cloud Gateway
    Authentication
    Routing
  ]
  
  component "Saga Orchestrator" as saga [
    #6
    Payment Flow
    Compensation Logic
    State Machine
  ]
}

// === CORE PAYMENT SERVICES ===
cloud "Core Payment Services" as core {
  component "Payment Initiation" as payment_init [
    #1
    REST API
    Payment Creation
    Initial Validation
  ]
  
  component "Validation Service" as validation [
    #2
    Limits Check
    Fraud Scoring
    Duplicate Check
  ]
  
  component "Account Adapter" as account [
    #3
    Core Banking Orchestration
    Balance Check
    Debit/Credit
  ]
  
  component "Routing Service" as routing [
    #4
    Route Determination
    Clearing Selection
    Rules Engine
  ]
  
  component "Transaction Processing" as transaction [
    #5
    Payment Execution
    State Management
    Workflow
  ]
}

// === BATCH PROCESSING ===
cloud "Batch Processing" as batch_layer {
  component "Batch Processing Service" as batch [
    #12 - NEW
    Spring Batch
    10K-100K txns/min
    CSV/Excel/XML/JSON
    Parallel Processing
  ]
}

// === CLEARING ADAPTERS ===
cloud "Clearing Adapters (5)" as clearing {
  component "SAMOS Adapter" as samos [
    #7
    High-Value RTGS
    ISO 20022
    SARB
  ]
  
  component "BankservAfrica Adapter" as bankserv [
    #8
    EFT Batch
    ISO 8583
    ACH
  ]
  
  component "RTC Adapter" as rtc [
    #9
    Real-Time Clearing
    ISO 20022
    BankservAfrica
  ]
  
  component "PayShap Adapter" as payshap [
    #10 - NEW
    Instant P2P
    Proxy Registry
    ISO 20022
    R 3,000 limit
  ]
  
  component "SWIFT Adapter" as swift [
    #11 - NEW
    International
    Sanctions Screening
    MT103 / pacs.008
    FX Conversion
  ]
}

// === SETTLEMENT & RECONCILIATION ===
cloud "Settlement & Reconciliation" as settlement_layer {
  component "Settlement Service" as settlement [
    #13
    Nostro/Vostro
    Settlement Calc
    Confirmations
  ]
  
  component "Reconciliation Service" as recon [
    #14
    Daily Recon
    Exception Handling
    Discrepancy Resolution
  ]
}

// === PLATFORM SERVICES ===
cloud "Platform Services (6)" as platform {
  component "Tenant Management" as tenant [
    #15
    Multi-Tenancy
    Tenant Hierarchy
    Configuration
  ]
  
  component "Notification Service" as notification [
    #16
    SMS/Email/Push
    IBM MQ Option
    Templates
  ]
  
  component "Reporting Service" as reporting [
    #17
    Reports
    Analytics
    Compliance
  ]
  
  component "IAM Service" as iam [
    #19
    Azure AD B2C
    RBAC/ABAC
    Token Mgmt
  ]
  
  component "Audit Service" as audit [
    #20
    Immutable Log
    7-Year Retention
    CosmosDB
  ]
}

// === EXTERNAL SYSTEMS ===
cloud "External Systems" as external {
  component "Core Banking Systems" as cbs [
    Current Accounts
    Savings Accounts
    Cards
    Loans
  ]
  
  component "Fraud Scoring API" as fraud [
    ML-Based Scoring
    Real-Time Risk
    Third-Party
  ]
  
  component "Notifications Engine" as notif_engine [
    Remote Engine
    IBM MQ
    Multi-Channel
  ]
}

// === SOUTH AFRICAN CLEARING SYSTEMS ===
cloud "SA Clearing Systems (4)" as sa_clearing {
  component "SAMOS" as samos_system [
    SARB
    High-Value RTGS
    Real-Time Settlement
  ]
  
  component "BankservAfrica" as bankserv_system [
    EFT (Batch)
    RTC (Real-Time)
    PayShap (Instant)
  ]
}

// === INTERNATIONAL ===
cloud "International Systems" as international {
  component "SWIFTNet" as swiftnet [
    11,000+ Banks
    200+ Countries
    ISO 20022
  ]
  
  component "Sanctions Lists" as sanctions [
    OFAC (US)
    UN Security Council
    EU Sanctions
  ]
  
  component "FX Rate Provider" as fx [
    Bloomberg / Reuters
    Real-Time Rates
  ]
}

// === DATA LAYER ===
cloud "Data & Messaging Layer" as data {
  database "PostgreSQL (18 DBs)" as postgres [
    Primary OLTP
    Row-Level Security
    Multi-Tenant
  ]
  
  database "Redis" as redis [
    Caching
    Session Store
    Rate Limiting
  ]
  
  database "CosmosDB" as cosmos [
    Audit Logs
    Immutable
    Global Distribution
  ]
  
  queue "Azure Service Bus" as servicebus [
    Event Streaming
    Pub/Sub
    Dead Letter
  ]
  
  queue "Kafka (Option)" as kafka [
    High-Throughput
    Event Sourcing
    Exactly-Once
  ]
}

// === AZURE INFRASTRUCTURE ===
cloud "Azure Infrastructure" as azure {
  component "AKS Cluster" as aks [
    Kubernetes 1.28
    3 Node Pools
    10-50 Nodes
    Istio Service Mesh
  ]
  
  component "Application Gateway" as appgw [
    WAF
    SSL Termination
    Load Balancing
  ]
  
  component "Azure AD B2C" as aad [
    Authentication
    Multi-Tenant
    OAuth 2.0
  ]
  
  component "Key Vault" as keyvault [
    Secrets
    Certificates
    Encryption Keys
  ]
  
  component "Azure Monitor" as monitor [
    Metrics
    Logs
    Alerts
    Dashboards
  ]
}

// === OBSERVABILITY ===
cloud "Observability Stack" as observability {
  component "Prometheus" as prometheus [
    Metrics Collection
    Time-Series DB
  ]
  
  component "Grafana" as grafana [
    Dashboards
    Visualization
    Alerting
  ]
  
  component "Jaeger" as jaeger [
    Distributed Tracing
    OpenTelemetry
    Request Flow
  ]
}

// ============================================
// CONNECTIONS - CHANNEL TO GATEWAY
// ============================================

web --> api_gateway
mobile --> api_gateway
apigw --> api_gateway
sftp --> batch

// ============================================
// CONNECTIONS - GATEWAY TO CORE SERVICES
// ============================================

api_gateway --> payment_init
api_gateway --> iam

payment_init --> validation
payment_init --> saga

validation --> account
validation --> fraud

account --> cbs

saga --> transaction
saga --> routing

routing --> samos
routing --> bankserv
routing --> rtc
routing --> payshap
routing --> swift

transaction --> settlement

// ============================================
// CONNECTIONS - CLEARING ADAPTERS TO SYSTEMS
// ============================================

samos --> samos_system
bankserv --> bankserv_system
rtc --> bankserv_system
payshap --> bankserv_system
swift --> swiftnet
swift --> sanctions
swift --> fx

// ============================================
// CONNECTIONS - SETTLEMENT & PLATFORM
// ============================================

settlement --> recon
settlement --> cbs

saga --> notification
payment_init --> audit
validation --> audit
transaction --> audit

notification -.-> notif_engine

tenant --> postgres
reporting --> postgres

// ============================================
// CONNECTIONS - BATCH PROCESSING
// ============================================

batch --> payment_init
batch --> postgres
batch --> notification

// ============================================
// CONNECTIONS - DATA LAYER
// ============================================

payment_init --> postgres
validation --> postgres
validation --> redis
account --> postgres
transaction --> postgres
saga --> postgres
settlement --> postgres
recon --> postgres
tenant --> postgres
reporting --> postgres
iam --> postgres
audit --> cosmos

payment_init --> servicebus
validation --> servicebus
transaction --> servicebus
saga --> servicebus
settlement --> servicebus

// ============================================
// CONNECTIONS - OBSERVABILITY
// ============================================

aks --> monitor
aks --> prometheus
prometheus --> grafana
aks --> jaeger

// ============================================
// LEGEND
// ============================================

note "LEGEND" as legend [
  Total: 20 Microservices
  
  Core Payment: 6 services (#1-#6)
  Clearing: 5 adapters (#7-#11)
  Batch: 1 service (#12)
  Settlement: 2 services (#13-#14)
  Platform: 6 services (#15-#20)
  
  NEW Services:
  - #10: PayShap Adapter (SA Instant)
  - #11: SWIFT Adapter (International)
  - #12: Batch Processing (Bulk Files)
  
  Clearing Coverage:
  - SA Domestic: 4/4 (100%)
  - International: 1/1 (100%)
  - Batch: 2/2 (100%)
]
```

---

## Simplified View - Payment Flow

```eraser
// Simplified Payment Flow - End-to-End

title Payment Flow - Happy Path

// Actors
actor Customer
actor Bank

// Channel
Customer -> "Web/Mobile App": 1. Initiate Payment

"Web/Mobile App" -> "API Gateway (#18)": 2. POST /payments

"API Gateway (#18)" -> "IAM Service (#19)": 3. Authenticate

"IAM Service (#19)" -> "API Gateway (#18)": Token Valid

"API Gateway (#18)" -> "Payment Initiation (#1)": 4. Create Payment

"Payment Initiation (#1)" -> "Saga Orchestrator (#6)": 5. Start Saga

// Validation Phase
"Saga Orchestrator (#6)" -> "Validation Service (#2)": 6. Validate

"Validation Service (#2)" -> "Limit Check": Check Limits
"Validation Service (#2)" -> "Fraud Scoring API": Score Risk
"Fraud Scoring API" -> "Validation Service (#2)": Risk Score: Low

"Validation Service (#2)" -> "Saga Orchestrator (#6)": Validated ✓

// Account Check Phase
"Saga Orchestrator (#6)" -> "Account Adapter (#3)": 7. Check Balance

"Account Adapter (#3)" -> "Core Banking System": Get Balance
"Core Banking System" -> "Account Adapter (#3)": Balance: R 50,000

"Account Adapter (#3)" -> "Saga Orchestrator (#6)": Sufficient ✓

// Routing Phase
"Saga Orchestrator (#6)" -> "Routing Service (#4)": 8. Route Payment

"Routing Service (#4)" -> "Saga Orchestrator (#6)": Route: PayShap

// Processing Phase
"Saga Orchestrator (#6)" -> "Transaction Processing (#5)": 9. Process

"Transaction Processing (#5)" -> "PayShap Adapter (#10)": Send to PayShap

"PayShap Adapter (#10)" -> "PayShap System": pacs.008 (ISO 20022)

"PayShap System" -> "PayShap Adapter (#10)": pacs.002 (Accepted)

"PayShap Adapter (#10)" -> "Transaction Processing (#5)": Success ✓

// Settlement Phase
"Transaction Processing (#5)" -> "Settlement Service (#13)": 10. Settle

"Settlement Service (#13)" -> "Account Adapter (#3)": Debit/Credit

"Account Adapter (#3)" -> "Core Banking System": Update Accounts

"Core Banking System" -> "Account Adapter (#3)": Done ✓

// Notification Phase
"Settlement Service (#13)" -> "Notification Service (#16)": 11. Notify

"Notification Service (#16)" -> Customer: SMS: Payment Successful

// Audit Phase
"Transaction Processing (#5)" -> "Audit Service (#20)": 12. Log Transaction

"Audit Service (#20)" -> "CosmosDB": Immutable Record

// Final Response
"Payment Initiation (#1)" -> Customer: Payment Complete ✓

note "Total Time: < 5 seconds (PayShap instant settlement)" as timing
```

---

## Batch Processing Flow

```eraser
// Batch Processing Flow - Corporate Payroll

title Batch Payment Processing - Salary Run

// Corporate client
actor "Corporate Client"

"Corporate Client" -> "SFTP Server": 1. Upload salary_oct_2025.csv
note "File: 10,000 employees" as filesize

"SFTP Server" -> "Batch Processing Service (#12)": 2. Auto-Pickup (every 5 min)

"Batch Processing Service (#12)" -> "File Parser": 3. Parse CSV

"File Parser" -> "Batch Processing Service (#12)": 10,000 payment records

"Batch Processing Service (#12)" -> "Spring Batch Job": 4. Create Batch Job

"Spring Batch Job" -> "Chunk 1 (500 records)": Process in parallel
"Spring Batch Job" -> "Chunk 2 (500 records)": Process in parallel
"Spring Batch Job" -> "Chunk N (500 records)": Process in parallel

note "20 parallel threads" as parallel

"Chunk 1 (500 records)" -> "Payment Initiation (#1)": Initiate Payments

"Payment Initiation (#1)" -> "Saga Orchestrator (#6)": Process Each

"Saga Orchestrator (#6)" -> "Validation Service (#2)": Validate
"Saga Orchestrator (#6)" -> "Account Adapter (#3)": Check Balance
"Saga Orchestrator (#6)" -> "Routing Service (#4)": Route (EFT)
"Saga Orchestrator (#6)" -> "Transaction Processing (#5)": Process

"Transaction Processing (#5)" -> "BankservAfrica Adapter (#8)": Send to EFT

"BankservAfrica Adapter (#8)" -> "BankservAfrica System": Batch File

"Spring Batch Job" -> "Result Aggregator": 5. Collect Results

"Result Aggregator" -> "Report Generator": 6. Generate Report

"Report Generator" -> "Result CSV": 9,950 Success, 50 Failed

"Batch Processing Service (#12)" -> "Notification Service (#16)": 7. Notify

"Notification Service (#16)" -> "Corporate Client": Email: Batch Complete

"Corporate Client" -> "Batch Processing Service (#12)": 8. Download Results

note "Total Time: ~10 minutes (10,000 payments)" as batch_time
```

---

## International SWIFT Payment Flow

```eraser
// International Payment via SWIFT

title SWIFT Payment Flow - Cross-Border

actor "Customer (SA)"
actor "Beneficiary (US)"

"Customer (SA)" -> "Web App": 1. Send $10,000 to US

"Web App" -> "API Gateway (#18)": POST /payments/international

"API Gateway (#18)" -> "Payment Initiation (#1)": Create Payment

"Payment Initiation (#1)" -> "Saga Orchestrator (#6)": Start International Flow

// Sanctions Screening (MANDATORY)
"Saga Orchestrator (#6)" -> "SWIFT Adapter (#11)": 2. Process SWIFT Payment

"SWIFT Adapter (#11)" -> "Sanctions Screening": 3. Screen (OFAC/UN/EU)

"Sanctions Screening" -> "OFAC List": Check Beneficiary
"Sanctions Screening" -> "UN List": Check Beneficiary
"Sanctions Screening" -> "EU List": Check Beneficiary

"Sanctions Screening" -> "SWIFT Adapter (#11)": No Hit ✓

// FX Conversion
"SWIFT Adapter (#11)" -> "FX Rate Service": 4. Get Rate (ZAR/USD)

"FX Rate Service" -> "SWIFT Adapter (#11)": Rate: 18.50

// Correspondent Routing
"SWIFT Adapter (#11)" -> "Correspondent Bank Service": 5. Determine Route

"Correspondent Bank Service" -> "SWIFT Adapter (#11)": Via JP Morgan Chase

// Build SWIFT Message
"SWIFT Adapter (#11)" -> "Message Builder": 6. Build MT103 / pacs.008

"Message Builder" -> "SWIFT Adapter (#11)": ISO 20022 Message

// Send to SWIFT Network
"SWIFT Adapter (#11)" -> "SWIFT Alliance Gateway": 7. Transmit

"SWIFT Alliance Gateway" -> "SWIFTNet": Send Message

"SWIFTNet" -> "JP Morgan Chase": Intermediary Bank

"JP Morgan Chase" -> "Citibank (US)": Beneficiary Bank

"Citibank (US)" -> "Beneficiary (US)": Credit Account

// Confirmation
"SWIFTNet" -> "SWIFT Alliance Gateway": MT910 (Confirmation)

"SWIFT Alliance Gateway" -> "SWIFT Adapter (#11)": Confirmed ✓

"SWIFT Adapter (#11)" -> "Settlement Service (#13)": Settle

"Settlement Service (#13)" -> "Reconciliation Service (#14)": Reconcile (2-5 days)

"SWIFT Adapter (#11)" -> "Notification Service (#16)": Notify

"Notification Service (#16)" -> "Customer (SA)": SMS: Payment Sent

note "Timeline: 2-5 business days for settlement" as swift_timing
note "Sanctions screening is MANDATORY for all SWIFT payments" as compliance
```

---

## Cell-Based Architecture (Scalability)

```eraser
// Cell-Based Architecture - Unlimited Scale

title Cell-Based Architecture - 10 Cells (100+ Banks)

cloud "Global Router" as router {
  component "Tenant Router" as tenant_router [
    Route by Tenant ID
    Health-Based
    Geographic
  ]
}

cloud "Cell 1 (Tenants 1-10)" as cell1 {
  component "AKS Cluster 1" as aks1 [
    20 Microservices
    PostgreSQL
    Kafka
    10 Banks
  ]
  
  note "Capacity: 87.5K req/sec" as cap1
}

cloud "Cell 2 (Tenants 11-20)" as cell2 {
  component "AKS Cluster 2" as aks2 [
    20 Microservices
    PostgreSQL
    Kafka
    10 Banks
  ]
  
  note "Capacity: 87.5K req/sec" as cap2
}

cloud "Cell 3 (Tenants 21-30)" as cell3 {
  component "AKS Cluster 3" as aks3 [
    20 Microservices
    PostgreSQL
    Kafka
    10 Banks
  ]
}

cloud "Cell 10 (Tenants 91-100)" as cell10 {
  component "AKS Cluster 10" as aks10 [
    20 Microservices
    PostgreSQL
    Kafka
    10 Banks
  ]
}

// Shared Services
cloud "Shared Services (All Cells)" as shared {
  component "Clearing Systems" as clearing_shared [
    SAMOS
    BankservAfrica
    PayShap
    SWIFT
  ]
  
  component "Monitoring" as monitoring_shared [
    Prometheus
    Grafana
    Jaeger
  ]
  
  component "Azure AD B2C" as aad_shared [
    Authentication
    Multi-Tenant
  ]
}

tenant_router --> aks1
tenant_router --> aks2
tenant_router --> aks3
tenant_router --> aks10

aks1 --> clearing_shared
aks2 --> clearing_shared
aks3 --> clearing_shared
aks10 --> clearing_shared

aks1 --> monitoring_shared
aks2 --> monitoring_shared
aks3 --> monitoring_shared
aks10 --> monitoring_shared

note "Total Capacity: 875K+ req/sec (10 cells × 87.5K)" as total_cap
note "Blast Radius: 10% (1 cell failure affects only 10 banks)" as blast
note "Scalability: Add cells for unlimited growth" as scale
```

---

## Technology Stack Overview

```eraser
// Technology Stack - Complete View

title Technology Stack & Tools

cloud "Frontend" as frontend_tech {
  component "React 18+" as react
  component "Redux / Zustand" as state
  component "Tailwind CSS" as tailwind
  component "React Query" as query
}

cloud "Backend" as backend_tech {
  component "Java 17" as java
  component "Spring Boot 3.x" as spring
  component "Spring Batch" as spring_batch
  component "Spring Cloud Gateway" as spring_gateway
  component "Spring WebFlux" as spring_reactive
}

cloud "Databases" as db_tech {
  database "PostgreSQL 15" as pg [
    18 Services
    Row-Level Security
  ]
  
  database "Redis 7" as redis_tech [
    Caching
    Rate Limiting
  ]
  
  database "CosmosDB" as cosmos_tech [
    Audit Logs
    Global Distribution
  ]
}

cloud "Messaging" as messaging_tech {
  queue "Azure Service Bus" as asb_tech [
    Default Option
    Enterprise
  ]
  
  queue "Confluent Kafka" as kafka_tech [
    High-Throughput Option
    Event Sourcing
  ]
  
  queue "IBM MQ" as ibmmq_tech [
    Notifications Option
    Non-Persistent
  ]
}

cloud "Azure Services" as azure_tech {
  component "AKS" as aks_tech [
    Kubernetes 1.28
    Istio Service Mesh
  ]
  
  component "Application Gateway" as appgw_tech [
    WAF
    Load Balancer
  ]
  
  component "Azure AD B2C" as aad_tech [
    Authentication
    OAuth 2.0
  ]
  
  component "Key Vault" as kv_tech [
    Secrets Management
  ]
  
  component "Azure Monitor" as monitor_tech [
    Logs & Metrics
  ]
  
  component "Application Insights" as appinsights_tech [
    APM
  ]
  
  component "Azure Synapse" as synapse_tech [
    Analytics
    Reporting
  ]
}

cloud "Observability" as obs_tech {
  component "Prometheus" as prom_tech [
    Metrics
  ]
  
  component "Grafana" as grafana_tech [
    Dashboards
  ]
  
  component "Jaeger" as jaeger_tech [
    Tracing
  ]
  
  component "OpenTelemetry" as otel_tech [
    Instrumentation
  ]
}

cloud "DevOps & CI/CD" as devops_tech {
  component "Azure DevOps" as azdo_tech [
    Pipelines
    Repos
  ]
  
  component "GitHub Actions" as gh_tech [
    Alternative CI/CD
  ]
  
  component "ArgoCD" as argo_tech [
    GitOps
    Deployments
  ]
  
  component "Helm" as helm_tech [
    K8s Packaging
  ]
  
  component "Terraform" as tf_tech [
    Infrastructure as Code
  ]
}

cloud "Testing" as testing_tech {
  component "JUnit 5" as junit_tech
  component "Testcontainers" as testcontainers_tech
  component "REST Assured" as restassured_tech
  component "Gatling" as gatling_tech [
    Performance Testing
  ]
  component "Pact" as pact_tech [
    Contract Testing
  ]
  component "Chaos Mesh" as chaos_tech [
    Chaos Engineering
  ]
}

cloud "Security" as security_tech {
  component "OAuth 2.0 / OIDC" as oauth_tech
  component "Azure AD B2C" as aad_sec_tech
  component "Key Vault" as kv_sec_tech
  component "OWASP ZAP" as zap_tech [
    Security Testing
  ]
  component "SonarQube" as sonar_tech [
    Code Quality
  ]
}
```

---

## Deployment Architecture

```eraser
// Deployment Pipeline - CI/CD

title CI/CD Pipeline - Zero-Downtime Deployment

actor Developer

Developer -> "Git Commit": 1. Push Code

"Git Commit" -> "Azure DevOps": 2. Trigger Pipeline

// Build Stage
"Azure DevOps" -> "Build Stage": 3. Maven Build

"Build Stage" -> "Unit Tests": Run Tests
"Build Stage" -> "SonarQube": Code Quality
"Build Stage" -> "OWASP Scan": Security Scan

"Build Stage" -> "Docker Build": 4. Build Image

"Docker Build" -> "Azure Container Registry": 5. Push Image

// Deploy to Dev
"Azure Container Registry" -> "ArgoCD": 6. Update GitOps

"ArgoCD" -> "AKS Dev": 7. Deploy to Dev

"AKS Dev" -> "Smoke Tests": 8. Run Tests

"Smoke Tests" -> "ArgoCD": Tests Passed ✓

// Deploy to Staging
"ArgoCD" -> "AKS Staging": 9. Deploy to Staging

"AKS Staging" -> "Integration Tests": 10. Full Test Suite

"Integration Tests" -> "Performance Tests": 11. Gatling

"Performance Tests" -> "Security Tests": 12. OWASP ZAP

"Security Tests" -> "ArgoCD": All Tests Passed ✓

// Deploy to Production (Canary)
"ArgoCD" -> "AKS Production": 13. Canary Deploy (10%)

"AKS Production" -> "Monitor Metrics": 14. Monitor

"Monitor Metrics" -> "Error Rate Check": Error Rate OK?
"Monitor Metrics" -> "Latency Check": Latency OK?

"Latency Check" -> "ArgoCD": Healthy ✓

"ArgoCD" -> "AKS Production": 15. Roll Out (100%)

"AKS Production" -> "Developer": Deployment Complete ✓

note "Total Time: ~15-20 minutes (automated)" as deploy_time
note "Zero-Downtime: Rolling update with health checks" as zero_down
note "Rollback: < 1 minute (automated)" as rollback
```

---

## Instructions

1. Copy each diagram code block above
2. Go to https://app.eraser.io/
3. Create new diagram
4. Select "Diagram as Code"
5. Paste the code
6. Eraser.ai will generate the visual diagram
7. Export as PNG/SVG/PDF

---

## Available Diagrams

1. **Main Architecture Diagram** - Complete system (all 20 services)
2. **Payment Flow** - End-to-end happy path
3. **Batch Processing Flow** - Corporate payroll example
4. **SWIFT Payment Flow** - International payment with sanctions
5. **Cell-Based Architecture** - Scalability (10 cells)
6. **Technology Stack** - All tools and technologies
7. **Deployment Architecture** - CI/CD pipeline

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Tool**: Eraser.ai (https://eraser.io)
