# Pain.001/Pain.002 Saga Architecture Diagram

## Overview
This document contains detailed Mermaid diagrams for the complete ISO 20022 pain.001/pain.002 saga architecture, including all components, services, and data flows.

## Complete Saga Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        CLIENT[Payment Client]
        API[REST API Gateway]
    end
    
    subgraph "Saga Orchestration Layer"
        SAGA[Pain001Pain002Saga]
        SAGA_STATE[Saga State Manager]
        SAGA_EVENTS[Saga Event Publisher]
    end
    
    subgraph "Event Streaming Layer"
        KAFKA[Apache Kafka]
        TOPICS[Kafka Topics]
    end
    
    subgraph "Processing Services"
        PARSER[Pain001MessageParser]
        BUILDER[Pain002MessageBuilder]
        VALIDATOR[Iso20022Validator]
        MARSHALLER[Iso20022MarshallerService]
    end
    
    subgraph "Payment Processing Services"
        PAYMENT_SVC[Payment Processing Service]
        ROUTING_SVC[Routing Service]
        VALIDATION_SVC[Validation Service]
        CLEARING_SVC[Clearing Service]
    end
    
    subgraph "Data Layer"
        DB[(PostgreSQL Database)]
        PAIN001_TBL[pain001_messages]
        PAIN002_TBL[pain002_status_reports]
        CORRELATION_TBL[pain001_pain002_correlation]
        SAGA_TBL[saga_states]
    end
    
    subgraph "External Systems"
        SWIFT[SWIFT Network]
        SAMOS[SAMOS Clearing]
        BANKSERV[BankservAfrica]
        RTC[RTC Clearing]
        PAYSHAP[PayShap]
    end
    
    %% Client Flow
    CLIENT -->|pain.001 XML| API
    API -->|Start Saga| SAGA
    
    %% Saga Flow
    SAGA -->|Parse| PARSER
    SAGA -->|Validate| VALIDATOR
    SAGA -->|Persist| SAGA_STATE
    SAGA -->|Publish Events| SAGA_EVENTS
    
    %% Event Flow
    SAGA_EVENTS -->|Events| KAFKA
    KAFKA -->|payment.processing.initiated| PAYMENT_SVC
    PAYMENT_SVC -->|payment.processing.completed| KAFKA
    KAFKA -->|Events| SAGA
    
    %% Processing Flow
    PAYMENT_SVC --> ROUTING_SVC
    ROUTING_SVC --> VALIDATION_SVC
    VALIDATION_SVC --> CLEARING_SVC
    
    %% Clearing Flow
    CLEARING_SVC --> SWIFT
    CLEARING_SVC --> SAMOS
    CLEARING_SVC --> BANKSERV
    CLEARING_SVC --> RTC
    CLEARING_SVC --> PAYSHAP
    
    %% Data Flow
    SAGA_STATE --> DB
    PARSER --> PAIN001_TBL
    BUILDER --> PAIN002_TBL
    SAGA --> CORRELATION_TBL
    SAGA_STATE --> SAGA_TBL
    
    %% Response Flow
    SAGA -->|Generate pain.002| BUILDER
    BUILDER -->|pain.002 XML| API
    API -->|Response| CLIENT
    
    %% Styling
    classDef client fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef saga fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef event fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef processing fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef data fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    classDef external fill:#f1f8e9,stroke:#558b2f,stroke-width:2px
    
    class CLIENT,API client
    class SAGA,SAGA_STATE,SAGA_EVENTS saga
    class KAFKA,TOPICS event
    class PARSER,BUILDER,VALIDATOR,MARSHALLER,PAYMENT_SVC,ROUTING_SVC,VALIDATION_SVC,CLEARING_SVC processing
    class DB,PAIN001_TBL,PAIN002_TBL,CORRELATION_TBL,SAGA_TBL data
    class SWIFT,SAMOS,BANKSERV,RTC,PAYSHAP external
```

## Detailed Saga Flow with Error Handling

```mermaid
sequenceDiagram
    participant C as Client
    participant API as REST API
    participant S as Saga Orchestrator
    participant P as Pain001Parser
    participant V as Validator
    participant DB as Database
    participant K as Kafka
    participant PS as Payment Service
    participant B as Pain002Builder
    
    C->>API: POST /api/payments/pain001
    API->>S: startPain001ProcessingSaga()
    
    Note over S: Step 1: Parse pain.001
    S->>V: validate(xml, PAIN_001)
    V-->>S: ValidationResult
    alt Validation Failed
        S->>S: compensateSagaSteps()
        S-->>API: SagaExecutionException
        API-->>C: 400 Bad Request
    else Validation Success
        S->>P: parseToCanonicalModel()
        P-->>S: CanonicalPaymentModel
        S->>DB: markSagaStepCompleted(PAIN001_PARSED)
    end
    
    Note over S: Step 2: Persist pain.001
    S->>DB: savePain001Message()
    alt Persistence Failed
        S->>S: compensateSagaSteps()
        S-->>API: SagaExecutionException
        API-->>C: 500 Internal Server Error
    else Persistence Success
        S->>DB: markSagaStepCompleted(PAIN001_PERSISTED)
    end
    
    Note over S: Step 3: Initiate Payment Processing
    S->>K: publishPaymentProcessingInitiated()
    S->>DB: markSagaStepCompleted(PAYMENT_PROCESSING_INITIATED)
    S-->>API: sagaId
    API-->>C: 202 Accepted + sagaId
    
    Note over K,PS: Asynchronous Processing
    K->>PS: PaymentProcessingInitiatedEvent
    PS->>PS: processPayment()
    alt Processing Success
        PS->>K: publishPaymentProcessingCompleted()
    else Processing Failed
        PS->>K: publishPaymentProcessingFailed()
    end
    
    Note over S,B: Saga Completion
    K->>S: PaymentProcessingCompletedEvent
    S->>B: build(canonicalModel)
    B-->>S: pain.002 XML
    S->>DB: savePain002Response()
    S->>DB: markSagaCompleted()
    S->>K: publishSagaCompleted()
    
    Note over C,API: Status Check
    C->>API: GET /api/payments/{sagaId}/status
    API->>DB: getSagaState(sagaId)
    DB-->>API: SagaState
    API-->>C: 200 OK + pain.002 XML
```

## Saga State Transitions

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : Start Saga
    
    ACTIVE --> PAIN001_PARSED : Parse pain.001
    PAIN001_PARSED --> PAIN001_PERSISTED : Persist pain.001
    PAIN001_PERSISTED --> PAYMENT_PROCESSING_INITIATED : Initiate Payment Processing
    
    PAYMENT_PROCESSING_INITIATED --> PAYMENT_PROCESSING_COMPLETED : Payment Success
    PAYMENT_PROCESSING_INITIATED --> PAYMENT_PROCESSING_FAILED : Payment Failure
    PAYMENT_PROCESSING_INITIATED --> SAGA_TIMEOUT : Timeout
    
    PAYMENT_PROCESSING_COMPLETED --> PAIN002_GENERATED : Generate pain.002
    PAIN002_GENERATED --> PAIN002_PERSISTED : Persist pain.002
    PAIN002_PERSISTED --> SAGA_COMPLETED : Complete Saga
    
    PAYMENT_PROCESSING_FAILED --> COMPENSATING : Start Compensation
    SAGA_TIMEOUT --> COMPENSATING : Start Compensation
    
    COMPENSATING --> PAYMENT_PROCESSING_COMPENSATED : Compensate Payment Processing
    PAYMENT_PROCESSING_COMPENSATED --> PAIN001_PERSISTENCE_COMPENSATED : Compensate Persistence
    PAIN001_PERSISTENCE_COMPENSATED --> SAGA_COMPENSATED : Complete Compensation
    
    SAGA_COMPLETED --> [*]
    SAGA_COMPENSATED --> [*]
    
    note right of COMPENSATING
        Compensation steps are executed
        in reverse order of the original
        saga steps
    end note
```

## Event Flow Architecture

```mermaid
graph LR
    subgraph "Saga Events"
        SE1[Saga Started]
        SE2[Saga Completed]
        SE3[Saga Compensated]
        SE4[Saga Timeout]
    end
    
    subgraph "Payment Events"
        PE1[Payment Processing Initiated]
        PE2[Payment Processing Completed]
        PE3[Payment Processing Failed]
        PE4[Payment Processing Cancelled]
    end
    
    subgraph "Kafka Topics"
        T1[payment.saga.started]
        T2[payment.saga.completed]
        T3[payment.saga.compensated]
        T4[payment.saga.timeout]
        T5[payment.processing.initiated]
        T6[payment.processing.completed]
        T7[payment.processing.failed]
        T8[payment.processing.cancelled]
    end
    
    subgraph "Event Handlers"
        H1[SagaEventHandler]
        H2[PaymentEventHandler]
        H3[NotificationEventHandler]
    end
    
    SE1 --> T1
    SE2 --> T2
    SE3 --> T3
    SE4 --> T4
    PE1 --> T5
    PE2 --> T6
    PE3 --> T7
    PE4 --> T8
    
    T1 --> H1
    T2 --> H1
    T3 --> H1
    T4 --> H1
    T5 --> H2
    T6 --> H2
    T7 --> H2
    T8 --> H2
    
    T2 --> H3
    T3 --> H3
    T6 --> H3
    T7 --> H3
    
    classDef sagaEvent fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef paymentEvent fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef topic fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef handler fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    
    class SE1,SE2,SE3,SE4 sagaEvent
    class PE1,PE2,PE3,PE4 paymentEvent
    class T1,T2,T3,T4,T5,T6,T7,T8 topic
    class H1,H2,H3 handler
```

## Future Extension Points

### 1. Multi-Currency Saga
```mermaid
graph TD
    A[Parse pain.001] --> B[Currency Validation]
    B --> C[Exchange Rate Lookup]
    C --> D[Currency Conversion]
    D --> E[Persist pain.001]
    E --> F[Initiate Payment Processing]
    
    %% Compensation for currency steps
    F -->|Failure| G[Compensate Payment Processing]
    G --> H[Compensate Currency Conversion]
    H --> I[Compensate Exchange Rate Lookup]
    I --> J[Compensate Currency Validation]
    J --> K[Saga Compensated]
```

### 2. Compliance Saga
```mermaid
graph TD
    A[Parse pain.001] --> B[AML/KYC Check]
    B --> C[Sanctions Screening]
    C --> D[Regulatory Compliance]
    D --> E[Risk Assessment]
    E --> F[Persist pain.001]
    F --> G[Initiate Payment Processing]
    
    %% Compensation for compliance steps
    G -->|Failure| H[Compensate Payment Processing]
    H --> I[Compensate Risk Assessment]
    I --> J[Compensate Regulatory Compliance]
    J --> K[Compensate Sanctions Screening]
    K --> L[Compensate AML/KYC Check]
    L --> M[Saga Compensated]
```

### 3. Settlement Saga
```mermaid
graph TD
    A[Payment Processing Completed] --> B[Initiate Settlement]
    B --> C[Settlement Validation]
    C --> D[Settlement Execution]
    D --> E[Settlement Confirmation]
    E --> F[Generate pain.002]
    F --> G[Persist pain.002]
    G --> H[Complete Saga]
    
    %% Compensation for settlement steps
    D -->|Failure| I[Compensate Settlement Execution]
    I --> J[Compensate Settlement Validation]
    J --> K[Compensate Settlement Initiation]
    K --> L[Saga Compensated]
```

## Usage Instructions

### Editing Diagrams
1. **Main Saga Flow**: Modify the first diagram to add new steps
2. **Architecture Diagram**: Update the second diagram for new services
3. **Sequence Diagram**: Add new interactions and error handling
4. **State Diagram**: Add new states and transitions
5. **Event Flow**: Add new events and handlers

### Adding New Features
1. **Identify Extension Points**: Determine where new steps fit
2. **Update Saga Steps**: Add new steps to the saga orchestrator
3. **Add Compensation**: Implement compensation logic for new steps
4. **Create Events**: Define new Kafka topics and events
5. **Update Handlers**: Add new event handlers
6. **Update Diagrams**: Modify all relevant diagrams

### Version Control
- Keep diagrams in sync with code changes
- Document breaking changes in version history
- Tag major architectural changes
- Maintain backward compatibility where possible

## Related Documents
- [Saga Pattern Implementation](../saga-pattern-implementation.md)
- [Event Schema Documentation](../event-schemas.md)
- [Database Schema](../database-schema.md)
- [API Documentation](../api-documentation.md)
- [Deployment Guide](../deployment-guide.md)
