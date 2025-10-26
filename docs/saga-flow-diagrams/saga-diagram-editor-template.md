# Saga Flow Diagram Editor Template

## Quick Edit Template

Use this template to quickly create or modify saga flow diagrams.

### Basic Saga Flow Template
```mermaid
graph TD
    A[Start] --> B[Step 1]
    B --> C[Step 2]
    C --> D[Step 3]
    D --> E[Step 4]
    E --> F[Complete]
    
    %% Add compensation flow
    D -->|Failure| G[Compensate Step 3]
    G --> H[Compensate Step 2]
    H --> I[Compensate Step 1]
    I --> J[Compensated]
    
    %% Add decision points
    C --> K{Decision Point}
    K -->|Success| D
    K -->|Failure| G
    
    %% Styling
    classDef step fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef compensation fill:#ffebee,stroke:#b71c1c,stroke-width:2px
    classDef decision fill:#fff3e0,stroke:#e65100,stroke-width:2px
    
    class A,B,C,D,E,F step
    class G,H,I,J compensation
    class K decision
```

### Event-Driven Saga Template
```mermaid
graph TD
    A[Start Saga] --> B[Step 1]
    B --> C[Publish Event 1]
    C --> D[Wait for Response]
    D --> E{Event Response}
    E -->|Success| F[Step 2]
    E -->|Failure| G[Compensate]
    F --> H[Publish Event 2]
    H --> I[Wait for Response]
    I --> J{Event Response}
    J -->|Success| K[Complete Saga]
    J -->|Failure| G
    
    %% Event flow
    C --> L[Kafka Topic 1]
    L --> M[Service 1]
    M --> N[Kafka Topic 2]
    N --> O[Saga Handler]
    O --> F
    
    H --> P[Kafka Topic 3]
    P --> Q[Service 2]
    Q --> R[Kafka Topic 4]
    R --> S[Saga Handler]
    S --> K
    
    %% Styling
    classDef saga fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef event fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef service fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef compensation fill:#ffebee,stroke:#b71c1c,stroke-width:2px
    classDef decision fill:#fff3e0,stroke:#e65100,stroke-width:2px
    
    class A,B,F,K saga
    class C,H,L,N,P,R event
    class M,Q service
    class G compensation
    class E,J decision
```

### Multi-Service Saga Template
```mermaid
graph TD
    subgraph "Saga Orchestrator"
        SAGA[Saga Orchestrator]
    end
    
    subgraph "Service 1"
        S1[Service 1]
        S1_EVENT[Service 1 Events]
    end
    
    subgraph "Service 2"
        S2[Service 2]
        S2_EVENT[Service 2 Events]
    end
    
    subgraph "Service 3"
        S3[Service 3]
        S3_EVENT[Service 3 Events]
    end
    
    subgraph "Event Bus"
        KAFKA[Kafka]
    end
    
    SAGA -->|Start| S1
    S1 --> S1_EVENT
    S1_EVENT --> KAFKA
    KAFKA -->|Event 1| SAGA
    SAGA -->|Continue| S2
    S2 --> S2_EVENT
    S2_EVENT --> KAFKA
    KAFKA -->|Event 2| SAGA
    SAGA -->|Continue| S3
    S3 --> S3_EVENT
    S3_EVENT --> KAFKA
    KAFKA -->|Event 3| SAGA
    SAGA -->|Complete| SAGA
    
    %% Error handling
    S1 -->|Error| COMP1[Compensate Service 1]
    S2 -->|Error| COMP2[Compensate Service 2]
    S3 -->|Error| COMP3[Compensate Service 3]
    
    COMP1 --> SAGA
    COMP2 --> SAGA
    COMP3 --> SAGA
```

## Common Patterns

### 1. Sequential Saga
```mermaid
graph LR
    A[Step 1] --> B[Step 2]
    B --> C[Step 3]
    C --> D[Step 4]
    D --> E[Complete]
```

### 2. Parallel Saga
```mermaid
graph TD
    A[Start] --> B[Step 1]
    A --> C[Step 2]
    A --> D[Step 3]
    B --> E[Wait for All]
    C --> E
    D --> E
    E --> F[Complete]
```

### 3. Conditional Saga
```mermaid
graph TD
    A[Start] --> B[Step 1]
    B --> C{Condition}
    C -->|True| D[Step 2A]
    C -->|False| E[Step 2B]
    D --> F[Step 3]
    E --> F
    F --> G[Complete]
```

### 4. Retry Saga
```mermaid
graph TD
    A[Start] --> B[Step 1]
    B --> C[Step 2]
    C --> D{Success?}
    D -->|No| E[Retry Step 2]
    E --> D
    D -->|Yes| F[Step 3]
    F --> G[Complete]
```

## Styling Guidelines

### Color Scheme
- **Saga Steps**: `#e3f2fd` (Light Blue)
- **Events**: `#f3e5f5` (Light Purple)
- **Services**: `#fff3e0` (Light Orange)
- **Compensation**: `#ffebee` (Light Red)
- **Decisions**: `#fff3e0` (Light Orange)
- **External Systems**: `#f1f8e9` (Light Green)

### Node Shapes
- **Rectangles**: Regular steps
- **Diamonds**: Decision points
- **Circles**: Start/End points
- **Rounded Rectangles**: Subprocesses

### Edge Styles
- **Solid**: Normal flow
- **Dashed**: Error/compensation flow
- **Thick**: Critical path
- **Dotted**: Optional flow

## Quick Commands

### Add New Step
```mermaid
graph TD
    A[Existing Step] --> B[New Step]
    B --> C[Next Step]
```

### Add Compensation
```mermaid
graph TD
    A[Step] --> B[Next Step]
    A -->|Failure| C[Compensate Step]
    C --> D[Compensated]
```

### Add Decision Point
```mermaid
graph TD
    A[Step] --> B{Decision}
    B -->|Option 1| C[Path 1]
    B -->|Option 2| D[Path 2]
    C --> E[Continue]
    D --> E
```

### Add Event Flow
```mermaid
graph TD
    A[Step] --> B[Publish Event]
    B --> C[Kafka Topic]
    C --> D[Service Handler]
    D --> E[Next Step]
```

## Best Practices

1. **Keep it Simple**: Start with basic flow, add complexity gradually
2. **Use Consistent Styling**: Apply the same colors and shapes throughout
3. **Show Error Handling**: Always include compensation flows
4. **Document Decisions**: Explain decision points and conditions
5. **Version Control**: Keep diagrams in sync with code changes
6. **Test Scenarios**: Include both success and failure paths
7. **Performance Considerations**: Show timeouts and retry logic

## Tools for Editing

### Online Editors
- [Mermaid Live Editor](https://mermaid.live/)
- [Draw.io](https://app.diagrams.net/)
- [Lucidchart](https://www.lucidchart.com/)

### VS Code Extensions
- Mermaid Preview
- Mermaid Markdown Syntax Highlighting
- Draw.io Integration

### Command Line
```bash
# Install Mermaid CLI
npm install -g @mermaid-js/mermaid-cli

# Generate PNG from Mermaid file
mmdc -i input.mmd -o output.png

# Generate SVG from Mermaid file
mmdc -i input.mmd -o output.svg
```

## Version History
- **v1.0** - Initial template creation
- **v1.1** - Added event-driven patterns
- **v1.2** - Added multi-service patterns
- **v1.3** - Added styling guidelines
- **v1.4** - Added best practices and tools
