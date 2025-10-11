# Distributed Tracing with OpenTelemetry - Implementation Guide

## Overview

This document provides the **complete Distributed Tracing implementation** using OpenTelemetry for the Payments Engine. Distributed tracing allows you to track requests across all 17 microservices, identify bottlenecks, and debug distributed transactions.

---

## Why Distributed Tracing?

### The Problem Without Tracing

```
Customer Report: "My payment is slow"

Where is the bottleneck?
- API Gateway?
- Payment Service?
- Validation Service?
- Account Adapter?
- Core Banking System?
- Clearing System?

Traditional logs: Need to search across 17 services manually ❌
```

### With Distributed Tracing

```
Trace ID: abc-123-xyz

Timeline (320ms total):
├─ API Gateway (10ms)
├─ Payment Service (50ms)
├─ Validation Service (150ms) ⚠️ BOTTLENECK FOUND!
│  ├─ Limit Check (20ms)
│  ├─ Fraud API Call (100ms) ⚠️ THIS IS SLOW
│  └─ DB Update (10ms)
├─ Account Adapter (80ms)
└─ Saga Orchestrator (30ms)

Answer: Fraud API is slow (100ms) ✅
```

---

## OpenTelemetry Architecture

```
┌────────────────────────────────────────────────────────────┐
│                    SERVICES (17)                            │
│                                                             │
│  Each service has OpenTelemetry SDK:                       │
│  - Automatic instrumentation (Spring Boot, HTTP, DB)       │
│  - Manual spans for business logic                         │
│  - Trace context propagation (HTTP headers, Kafka)        │
└────────────────────┬───────────────────────────────────────┘
                     │
                     │ OTLP (OpenTelemetry Protocol)
                     ▼
┌────────────────────────────────────────────────────────────┐
│              OpenTelemetry Collector                        │
│                                                             │
│  - Receives traces from all services                       │
│  - Batches and exports to backends                         │
│  - Sampling (keep 10% of traces)                           │
│  - Filtering and transformation                            │
└────────────────────┬───────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         ▼                       ▼
┌─────────────────┐    ┌──────────────────┐
│  Jaeger         │    │  Azure Monitor   │
│  (Development)  │    │  (Production)    │
│                 │    │                  │
│  - Trace storage│    │  - Application   │
│  - UI           │    │    Insights      │
│  - Query API    │    │  - Integrated    │
└─────────────────┘    └──────────────────┘
```

---

## 1. OpenTelemetry SDK Setup

### Maven Dependencies

```xml
<!-- pom.xml -->
<dependencies>
    <!-- OpenTelemetry BOM (Bill of Materials) -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-bom</artifactId>
        <version>1.32.0</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
    
    <!-- OpenTelemetry API -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-api</artifactId>
    </dependency>
    
    <!-- OpenTelemetry SDK -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
    </dependency>
    
    <!-- OpenTelemetry Exporters -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporter-otlp</artifactId>
    </dependency>
    
    <!-- Auto-instrumentation for Spring Boot -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-spring-boot-starter</artifactId>
        <version>1.32.0-alpha</version>
    </dependency>
    
    <!-- Kafka instrumentation -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-kafka-clients-2.6</artifactId>
    </dependency>
    
    <!-- JDBC instrumentation -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-jdbc</artifactId>
    </dependency>
</dependencies>
```

### Configuration

```java
package com.payments.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

/**
 * OpenTelemetry Configuration
 * 
 * Sets up distributed tracing for the service
 */
@Configuration
@Slf4j
public class OpenTelemetryConfig {
    
    @Value("${spring.application.name}")
    private String serviceName;
    
    @Value("${otel.exporter.otlp.endpoint:http://localhost:4317}")
    private String otlpEndpoint;
    
    @Value("${otel.traces.sampler.probability:0.1}")  // Sample 10% by default
    private double samplingProbability;
    
    @Bean
    public OpenTelemetry openTelemetry() {
        log.info("Initializing OpenTelemetry for service: {}", serviceName);
        
        // 1. Define service resource
        Resource resource = Resource.getDefault()
            .merge(Resource.create(Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, serviceName)
                .put(ResourceAttributes.SERVICE_VERSION, getServiceVersion())
                .put(ResourceAttributes.SERVICE_INSTANCE_ID, getInstanceId())
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, getEnvironment())
                .put(ResourceAttributes.SERVICE_NAMESPACE, "payments-engine")
                // Custom attributes
                .put("tenant.isolation", "enabled")
                .put("cloud.provider", "azure")
                .put("cloud.region", "southafricanorth")
                .build()
            ));
        
        // 2. Configure span exporter (OTLP to collector)
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(otlpEndpoint)
            .setTimeout(Duration.ofSeconds(10))
            .build();
        
        // 3. Configure span processor (batching for performance)
        BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter)
            .setScheduleDelay(Duration.ofSeconds(5))  // Export every 5 seconds
            .setMaxQueueSize(2048)
            .setMaxExportBatchSize(512)
            .setExporterTimeout(Duration.ofSeconds(30))
            .build();
        
        // 4. Configure sampler (reduce volume in production)
        Sampler sampler = Sampler.traceIdRatioBased(samplingProbability);
        
        // 5. Build tracer provider
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .setSampler(sampler)
            .setResource(resource)
            .build();
        
        // 6. Build OpenTelemetry SDK
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(
                TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),  // Standard W3C
                    W3CBaggagePropagator.getInstance()        // For custom context
                )
            ))
            .buildAndRegisterGlobal();
        
        // 7. Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down OpenTelemetry");
            tracerProvider.close();
        }));
        
        log.info("OpenTelemetry initialized successfully");
        return openTelemetry;
    }
    
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, getServiceVersion());
    }
    
    private String getServiceVersion() {
        return System.getenv().getOrDefault("SERVICE_VERSION", "1.0.0");
    }
    
    private String getInstanceId() {
        return System.getenv().getOrDefault("HOSTNAME", "localhost");
    }
    
    private String getEnvironment() {
        return System.getenv().getOrDefault("ENVIRONMENT", "development");
    }
}
```

### Application Properties

```yaml
# application.yml
spring:
  application:
    name: payment-service  # Each service has unique name

# OpenTelemetry Configuration
otel:
  # Exporter
  exporter:
    otlp:
      endpoint: http://otel-collector:4317  # gRPC endpoint
      protocol: grpc
      timeout: 10s
  
  # Traces
  traces:
    sampler:
      probability: 0.1  # Sample 10% in production (100% in dev)
  
  # Resource attributes
  resource:
    attributes:
      service.name: ${spring.application.name}
      service.version: ${SERVICE_VERSION:1.0.0}
      deployment.environment: ${ENVIRONMENT:development}
  
  # Instrumentation
  instrumentation:
    # Auto-instrumentation
    spring-boot:
      enabled: true
    spring-web:
      enabled: true
    spring-webflux:
      enabled: true
    jdbc:
      enabled: true
    kafka:
      enabled: true
    redis:
      enabled: true
    
    # Manual instrumentation
    annotations:
      enabled: true
```

---

## 2. Automatic Instrumentation

OpenTelemetry automatically instruments common frameworks:

### Spring Boot REST Controllers

```java
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    
    /**
     * Automatically instrumented by OpenTelemetry
     * 
     * Span created with:
     * - Name: "POST /api/v1/payments"
     * - Attributes: http.method, http.url, http.status_code
     * - Duration: Automatically measured
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
        @RequestBody PaymentRequest request
    ) {
        // Your code here
        return ResponseEntity.ok(response);
    }
}
```

### Database Calls (JDBC)

```java
// Automatically instrumented
PaymentRepository.save(payment);

// Creates span:
// - Name: "INSERT payments"
// - Attributes: db.system, db.name, db.statement, db.operation
```

### Kafka Producer/Consumer

```java
// Automatically instrumented
kafkaTemplate.send("payment.initiated", event);

// Creates span:
// - Name: "payment.initiated send"
// - Attributes: messaging.system, messaging.destination, messaging.operation
```

---

## 3. Manual Instrumentation

For business logic, add manual spans:

### Using @WithSpan Annotation

```java
package com.payments.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

@Service
@Slf4j
public class PaymentService {
    
    @Autowired
    private Tracer tracer;
    
    /**
     * Simple manual span using @WithSpan annotation
     */
    @WithSpan("payment.initiate")  // Creates span named "payment.initiate"
    public Payment initiatePayment(
        @SpanAttribute("tenant.id") String tenantId,  // Adds attribute
        @SpanAttribute("payment.amount") BigDecimal amount,
        @SpanAttribute("payment.type") String paymentType,
        PaymentRequest request
    ) {
        log.info("Initiating payment for tenant: {}", tenantId);
        
        // Business logic
        Payment payment = createPayment(request);
        
        // Add custom event to current span
        Span.current().addEvent("payment.created", Attributes.of(
            AttributeKey.stringKey("payment.id"), payment.getId()
        ));
        
        return payment;
    }
    
    /**
     * Advanced manual span with programmatic API
     */
    public void processPayment(Payment payment) {
        // Create custom span
        Span span = tracer.spanBuilder("payment.process")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            // Add attributes
            span.setAttribute("payment.id", payment.getId());
            span.setAttribute("payment.status", payment.getStatus().toString());
            span.setAttribute("tenant.id", payment.getTenantId());
            
            // Step 1: Validate
            validate(payment);
            span.addEvent("payment.validated");
            
            // Step 2: Reserve funds
            reserveFunds(payment);
            span.addEvent("funds.reserved");
            
            // Step 3: Submit to clearing
            submitToClearing(payment);
            span.addEvent("clearing.submitted");
            
            // Success
            span.setStatus(StatusCode.OK);
            
        } catch (ValidationException e) {
            // Record exception
            span.setStatus(StatusCode.ERROR, "Validation failed: " + e.getMessage());
            span.recordException(e);
            throw e;
            
        } catch (Exception e) {
            // Record any other exception
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e, Attributes.of(
                AttributeKey.stringKey("error.type"), e.getClass().getSimpleName()
            ));
            throw e;
            
        } finally {
            // Always end span
            span.end();
        }
    }
    
    /**
     * Nested spans (parent-child relationship)
     */
    @WithSpan("payment.validate")
    private void validate(Payment payment) {
        // This creates a child span of "payment.process"
        
        // Add custom attributes to current span
        Span.current().setAttribute("validation.rules", "limit,fraud,compliance");
        
        // Validation logic
        limitService.checkLimits(payment);  // Creates child span
        fraudService.checkFraud(payment);   // Creates child span
    }
}
```

### Span Hierarchy Example

```
Span: payment.process (200ms)
├─ Span: payment.validate (150ms)
│  ├─ Span: limit.check (20ms)
│  └─ Span: fraud.check (100ms)
│     └─ Span: HTTP POST fraud-api (95ms)
├─ Span: funds.reserve (30ms)
│  └─ Span: HTTP POST core-banking (25ms)
└─ Span: clearing.submit (20ms)
```

---

## 4. Trace Context Propagation

### HTTP (Automatic)

OpenTelemetry automatically propagates trace context via HTTP headers:

```http
# Outgoing HTTP request
GET /api/v1/accounts/12345 HTTP/1.1
Host: account-service:8082
traceparent: 00-abc123...xyz-789def...uvw-01
tracestate: tenant=STD-001

# Account Service receives same trace context
# Its span becomes child of caller's span
```

### Kafka (Manual)

```java
package com.payments.kafka;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;

@Service
public class PaymentEventPublisher {
    
    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    
    @Autowired
    private TextMapPropagator propagator;
    
    /**
     * Publish event with trace context
     */
    public void publish(PaymentInitiatedEvent event) {
        ProducerRecord<String, PaymentEvent> record = new ProducerRecord<>(
            "payment.initiated",
            event.getPaymentId(),
            event
        );
        
        // Inject trace context into Kafka headers
        propagator.inject(
            Context.current(),
            record.headers(),
            KafkaHeadersSetter.INSTANCE
        );
        
        kafkaTemplate.send(record);
    }
    
    // Kafka Headers Setter
    private enum KafkaHeadersSetter implements TextMapSetter<Headers> {
        INSTANCE;
        
        @Override
        public void set(Headers headers, String key, String value) {
            if (headers != null && key != null && value != null) {
                headers.remove(key);
                headers.add(key, value.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}

@Service
public class PaymentEventConsumer {
    
    @Autowired
    private TextMapPropagator propagator;
    
    /**
     * Consume event and extract trace context
     */
    @KafkaListener(topics = "payment.initiated")
    public void onPaymentInitiated(
        ConsumerRecord<String, PaymentInitiatedEvent> record
    ) {
        // Extract trace context from Kafka headers
        Context context = propagator.extract(
            Context.current(),
            record.headers(),
            KafkaHeadersGetter.INSTANCE
        );
        
        // Process event within extracted context
        try (Scope scope = context.makeCurrent()) {
            // This span becomes child of producer's span
            Span span = tracer.spanBuilder("payment.initiated.consume")
                .setSpanKind(SpanKind.CONSUMER)
                .startSpan();
            
            try {
                processEvent(record.value());
                span.setStatus(StatusCode.OK);
            } catch (Exception e) {
                span.setStatus(StatusCode.ERROR, e.getMessage());
                span.recordException(e);
                throw e;
            } finally {
                span.end();
            }
        }
    }
    
    // Kafka Headers Getter
    private enum KafkaHeadersGetter implements TextMapGetter<Headers> {
        INSTANCE;
        
        @Override
        public Iterable<String> keys(Headers headers) {
            List<String> keys = new ArrayList<>();
            headers.forEach(header -> keys.add(header.key()));
            return keys;
        }
        
        @Override
        public String get(Headers headers, String key) {
            Header header = headers.lastHeader(key);
            return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
        }
    }
}
```

---

## 5. Jaeger Deployment

### Docker Compose (Development)

```yaml
# docker-compose.yml
version: '3.8'

services:
  # Jaeger All-in-One (development)
  jaeger:
    image: jaegertracing/all-in-one:1.51
    environment:
      - COLLECTOR_OTLP_ENABLED=true
    ports:
      - "16686:16686"  # Jaeger UI
      - "4317:4317"    # OTLP gRPC receiver
      - "4318:4318"    # OTLP HTTP receiver
    networks:
      - payments-network
  
  # OpenTelemetry Collector (optional)
  otel-collector:
    image: otel/opentelemetry-collector-contrib:0.91.0
    command: ["--config=/etc/otel-collector-config.yaml"]
    volumes:
      - ./otel-collector-config.yaml:/etc/otel-collector-config.yaml
    ports:
      - "4317:4317"    # OTLP gRPC
      - "4318:4318"    # OTLP HTTP
      - "8888:8888"    # Prometheus metrics
    networks:
      - payments-network
```

### OpenTelemetry Collector Configuration

```yaml
# otel-collector-config.yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch:
    timeout: 10s
    send_batch_size: 1024
  
  # Add tenant_id to all spans
  attributes:
    actions:
      - key: tenant.id
        action: insert
        from_attribute: http.request.header.x-tenant-id
  
  # Sample traces (keep 10%)
  probabilistic_sampler:
    sampling_percentage: 10.0
  
  # Filter out health checks
  filter:
    spans:
      exclude:
        match_type: strict
        attributes:
          - key: http.target
            value: /actuator/health

exporters:
  # Export to Jaeger
  jaeger:
    endpoint: jaeger:14250
    tls:
      insecure: true
  
  # Export to Azure Monitor (production)
  azuremonitor:
    instrumentation_key: ${APPLICATIONINSIGHTS_CONNECTION_STRING}
  
  # Export metrics to Prometheus
  prometheus:
    endpoint: "0.0.0.0:8889"
  
  # Logging (debugging)
  logging:
    loglevel: info

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch, attributes, probabilistic_sampler, filter]
      exporters: [jaeger, azuremonitor, logging]
    
    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [prometheus]
```

### Kubernetes Deployment

```yaml
# jaeger-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jaeger
  namespace: payments
spec:
  replicas: 1
  selector:
    matchLabels:
      app: jaeger
  template:
    metadata:
      labels:
        app: jaeger
    spec:
      containers:
        - name: jaeger
          image: jaegertracing/all-in-one:1.51
          env:
            - name: COLLECTOR_OTLP_ENABLED
              value: "true"
            - name: SPAN_STORAGE_TYPE
              value: "cassandra"  # Production: Use Cassandra
            - name: CASSANDRA_SERVERS
              value: "cassandra:9042"
          ports:
            - containerPort: 16686
              name: ui
            - containerPort: 4317
              name: otlp-grpc
          resources:
            requests:
              memory: "1Gi"
              cpu: "500m"
            limits:
              memory: "2Gi"
              cpu: "1000m"

---
apiVersion: v1
kind: Service
metadata:
  name: jaeger
  namespace: payments
spec:
  selector:
    app: jaeger
  ports:
    - name: ui
      port: 16686
      targetPort: 16686
    - name: otlp-grpc
      port: 4317
      targetPort: 4317
  type: LoadBalancer
```

---

## 6. Jaeger UI Usage

### Access Jaeger UI

```
URL: http://localhost:16686

Features:
- Search traces by service, operation, tags
- View trace timeline
- Analyze span details
- Compare traces
- Service dependencies graph
```

### Example Trace View

```
┌─────────────────────────────────────────────────────────────┐
│ Trace: abc-123-xyz (320ms)                                  │
│ Services: 5 | Spans: 12                                     │
├─────────────────────────────────────────────────────────────┤
│ Timeline:                                                    │
│                                                             │
│ api-gateway (10ms)                                          │
│ █                                                           │
│                                                             │
│ payment-service (50ms)                                      │
│   ████                                                      │
│                                                             │
│ validation-service (150ms) ⚠️                               │
│     ██████████████                                          │
│     ├─ limit-check (20ms)                                  │
│     │  ██                                                   │
│     ├─ fraud-check (100ms) ⚠️ SLOW                         │
│     │  ████████                                            │
│     └─ db-update (10ms)                                    │
│        █                                                    │
│                                                             │
│ account-adapter (80ms)                                      │
│                 ██████                                      │
│                 └─ core-banking-api (60ms)                 │
│                    ████                                     │
│                                                             │
│ saga-orchestrator (30ms)                                    │
│                          ██                                 │
└─────────────────────────────────────────────────────────────┘

Span Details: validation-service > fraud-check
├─ Duration: 100ms ⚠️ 
├─ Status: OK
├─ Attributes:
│  ├─ payment.id: PAY-12345
│  ├─ fraud.score: 0.85
│  ├─ fraud.risk_level: HIGH
│  └─ http.status_code: 200
├─ Events:
│  ├─ fraud.api.request (t=0ms)
│  └─ fraud.api.response (t=95ms)
└─ Logs:
   └─ Fraud API call took longer than expected
```

---

## 7. Querying Traces

### Search by Service

```
Service: validation-service
Operation: all
Tags: tenant.id=STD-001
Min Duration: 100ms

Results: 23 traces found
```

### Search by Specific Payment

```
Tags: payment.id=PAY-12345

Result: 1 trace found (320ms, 5 services, 12 spans)
```

### Search by Error

```
Tags: error=true
Min Duration: -

Results: 15 failed traces found
```

---

## 8. Benefits

### Performance Analysis

```
Before tracing:
Q: "Why is this payment slow?"
A: "Let me check logs in 17 services..." (1 hour) ❌

After tracing:
Q: "Why is this payment slow?"
A: "Fraud API call is slow (100ms)" (30 seconds) ✅
```

### Error Debugging

```
Before tracing:
Q: "Payment failed - where?"
A: "Let me search logs..." (1 hour) ❌

After tracing:
Q: "Payment failed - where?"
A: "Account Adapter failed at core banking API call" (30 seconds) ✅
```

### SLA Monitoring

```
Query: Show all traces > 1 second
Result: 45 slow traces found
Common pattern: Core Banking API slow (85% of cases)
Action: Add caching for account lookups
```

---

## 9. Best Practices

### DO ✅

1. **Use meaningful span names**: `payment.initiate`, not `method1`
2. **Add business context**: tenant_id, payment_id, amount
3. **Record exceptions**: `span.recordException(e)`
4. **Use @WithSpan for business logic**: Easy to add/remove
5. **Propagate context**: HTTP, Kafka, async threads
6. **Sample in production**: 10% is usually enough
7. **Monitor span duration**: Set alerts for slow spans

### DON'T ❌

1. **Don't create too many spans**: Adds overhead
2. **Don't add PII to spans**: payment_id yes, customer name no
3. **Don't trace health checks**: Filter them out
4. **Don't use 100% sampling in production**: Too much data
5. **Don't forget to end spans**: Always `span.end()`
6. **Don't block on tracing**: Async export

---

## 10. Production Readiness Checklist

### Pre-Production

- [ ] OpenTelemetry SDK added to all 17 services
- [ ] Automatic instrumentation enabled (HTTP, DB, Kafka)
- [ ] Manual spans added to critical business logic
- [ ] Trace context propagated via HTTP and Kafka
- [ ] Jaeger deployed and tested
- [ ] Sampling configured (10% recommended)
- [ ] Dashboards created in Jaeger/Azure Monitor
- [ ] Alerts set up for slow traces (> 1s)
- [ ] Team trained on Jaeger UI

### Post-Production

- [ ] Monitor trace volume and adjust sampling
- [ ] Create common queries (slow traces, errors)
- [ ] Set up automated reports (daily SLA)
- [ ] Integrate with incident management
- [ ] Regular trace analysis (weekly)

---

## Related Documents

- **[13-MODERN-ARCHITECTURE-PATTERNS.md](13-MODERN-ARCHITECTURE-PATTERNS.md)** - Distributed tracing overview
- **[07-AZURE-INFRASTRUCTURE.md](07-AZURE-INFRASTRUCTURE.md)** - Azure Monitor integration

---

**Last Updated**: 2025-10-11  
**Version**: 1.0
