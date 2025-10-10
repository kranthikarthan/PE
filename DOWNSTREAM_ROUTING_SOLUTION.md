# Downstream Routing Solution for Same Host/Port Conflicts

## Overview

This document provides a comprehensive solution for resolving downstream routing conflicts in multi-tenant environments where multiple tenants need to call the same external host:port (e.g., bank's NGINX:443) but require different routing based on tenant context and service type.

## Table of Contents

1. [Problem Analysis](#problem-analysis)
2. [Solution Architecture](#solution-architecture)
3. [Implementation Details](#implementation-details)
4. [Deployment Guide](#deployment-guide)
5. [API Usage](#api-usage)
6. [Testing](#testing)
7. [Monitoring and Troubleshooting](#monitoring-and-troubleshooting)
8. [Best Practices](#best-practices)

---

## Problem Analysis

### Scenario Description

```
Payment Engine → Bank's NGINX (443) → Fraud System
Payment Engine → Bank's NGINX (443) → Clearing System
```

**Constraints:**
- Same host and port (bank's NGINX:443) for all tenants
- Cannot change host or port (bank's infrastructure)
- Multiple tenants need different routing
- Routing must be determined by request context

### Original Problem

```
❌ PROBLEMATIC SCENARIO:
┌─────────────────────────────────────────────────────────────┐
│                Payment Engine                              │
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │   Tenant-001    │  │   Tenant-002    │  │   Tenant-003 │ │
│  │                 │  │                 │  │              │ │
│  └─────────────────┘  └─────────────────┘  └──────────────┘ │
│           │                       │                       │ │
│           └───────────────────────┼───────────────────────┘ │
│                                   │                         │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              Bank's NGINX (443)                        │ │
│  │                                                         │ │
│  │  ❌ CONFLICT: Same host:port for all tenants           │ │
│  │  ❌ No tenant isolation                                │ │
│  │  ❌ No service type routing                            │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                   │                         │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │  Fraud System   │    │ Clearing System │                │
│  └─────────────────┘    └─────────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

---

## Solution Architecture

### Multi-Tenant Downstream Routing

```
✅ SOLUTION ARCHITECTURE:
┌─────────────────────────────────────────────────────────────────┐
│                Payment Engine                                  │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐   │
│  │   Tenant-001    │  │   Tenant-002    │  │   Tenant-003 │   │
│  │                 │  │                 │  │              │   │
│  └─────────────────┘  └─────────────────┘  └──────────────┘   │
│           │                       │                       │   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │            Istio Service Mesh                          │   │
│  │                                                         │   │
│  │  ┌─────────────────────────────────────────────────────┐ │   │
│  │  │         VirtualService + EnvoyFilter               │ │   │
│  │  │                                                     │ │   │
│  │  │  X-Tenant-ID: tenant-001                           │ │   │
│  │  │  X-Service-Type: fraud                             │ │   │
│  │  │  X-Route-Context: tenant-001-fraud                 │ │   │
│  │  │  X-Downstream-Route: fraud-system                  │ │   │
│  │  │  X-Bank-Route: /fraud/tenant-001                   │ │   │
│  │  └─────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                   │                         │   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Bank's NGINX (443)                        │   │
│  │                                                         │   │
│  │  ✅ RESOLVED: Same host:port with context routing      │   │
│  │  ✅ Tenant isolation via headers                       │   │
│  │  ✅ Service type routing via headers                   │   │
│  │  ✅ Automatic conflict resolution                      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                   │                         │   │
│  ┌─────────────────┐    ┌─────────────────┐                │   │
│  │  Fraud System   │    │ Clearing System │                │   │
│  │  (via headers)  │    │ (via headers)   │                │   │
│  └─────────────────┘    └─────────────────┘                │   │
└─────────────────────────────────────────────────────────────────┘
```

### Key Components

1. **ServiceEntry**: Defines external bank's NGINX service
2. **VirtualService**: Routes based on tenant and service type headers
3. **DestinationRule**: Manages traffic policies for external services
4. **EnvoyFilter**: Advanced routing logic based on request content
5. **AuthorizationPolicy**: Ensures tenant isolation and security
6. **DownstreamRoutingService**: Application-level routing logic

---

## Implementation Details

### 1. ServiceEntry Configuration

```yaml
# Single ServiceEntry for Bank's NGINX
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: bank-nginx-single-entry
spec:
  hosts:
  - bank-nginx.example.com  # Bank's NGINX host (same for all tenants)
  ports:
  - number: 443
    name: https
    protocol: HTTPS
  location: MESH_EXTERNAL
  resolution: DNS
```

### 2. VirtualService with Header-Based Routing

```yaml
# VirtualService for Same Host/Port Routing
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: same-host-port-routing
spec:
  hosts:
  - bank-nginx.example.com
  http:
  # Tenant-001 Fraud System Routing
  - match:
    - headers:
        x-tenant-id:
          exact: "tenant-001"
        x-service-type:
          exact: "fraud"
    route:
    - destination:
        host: bank-nginx.example.com
        port:
          number: 443
    headers:
      request:
        set:
          X-Tenant-ID: "tenant-001"
          X-Service-Type: "fraud"
          X-Route-Context: "tenant-001-fraud"
          X-Downstream-Route: "fraud-system"
          X-Bank-Route: "/fraud/tenant-001"
```

### 3. EnvoyFilter for Advanced Routing

```yaml
# EnvoyFilter for Request Content-Based Routing
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: request-body-routing
spec:
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_OUTBOUND
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.http.lua
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.lua.v3.Lua
          inline_code: |
            function envoy_on_request(request_handle)
              local body = request_handle:body()
              if body then
                local body_str = body:getBytes(0, body:length())
                local json = require("cjson")
                local success, data = pcall(json.decode, body_str)
                
                if success and data then
                  if data.fraud_check or data.risk_assessment then
                    request_handle:headers():add("x-service-type", "fraud")
                  elseif data.clearing_reference or data.settlement then
                    request_handle:headers():add("x-service-type", "clearing")
                  end
                end
              end
            end
```

### 4. Application-Level Routing Service

```java
@Service
public class DownstreamRoutingService {
    
    public <T> ResponseEntity<T> callFraudSystem(String tenantId, Object requestBody, 
                                               Class<T> responseType, Map<String, String> additionalHeaders) {
        String url = buildUrl("/fraud");
        HttpHeaders headers = createHeaders(tenantId, "fraud", additionalHeaders);
        
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
    }
    
    private HttpHeaders createHeaders(String tenantId, String serviceType, Map<String, String> additionalHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", tenantId);
        headers.set("X-Service-Type", serviceType);
        headers.set("X-Route-Context", tenantId + "-" + serviceType);
        headers.set("X-Downstream-Route", serviceType + "-system");
        headers.set("X-Bank-Route", "/" + serviceType + "/" + tenantId);
        return headers;
    }
}
```

---

## Deployment Guide

### Prerequisites

1. **Kubernetes Cluster**: Version 1.21+
2. **Istio**: Version 1.15+
3. **kubectl**: Configured and accessible
4. **Bank's NGINX**: Accessible from the cluster

### Step 1: Deploy Downstream Routing Solution

```bash
# Deploy with default configuration
./scripts/deploy-downstream-routing.sh

# Deploy with custom bank host/port
./scripts/deploy-downstream-routing.sh --host my-bank.com --port 443

# Dry run to see what would be deployed
./scripts/deploy-downstream-routing.sh --dry-run
```

### Step 2: Configure Bank's NGINX

Update your bank's NGINX configuration to handle the routing headers:

```nginx
# Bank's NGINX Configuration
server {
    listen 443 ssl;
    server_name bank-nginx.example.com;
    
    # Route based on X-Bank-Route header
    location /fraud/ {
        proxy_pass http://fraud-system/;
        proxy_set_header X-Tenant-ID $http_x_tenant_id;
        proxy_set_header X-Service-Type $http_x_service_type;
    }
    
    location /clearing/ {
        proxy_pass http://clearing-system/;
        proxy_set_header X-Tenant-ID $http_x_tenant_id;
        proxy_set_header X-Service-Type $http_x_service_type;
    }
    
    # Default routing
    location / {
        proxy_pass http://default-system/;
        proxy_set_header X-Tenant-ID $http_x_tenant_id;
    }
}
```

### Step 3: Test the Configuration

```bash
# Test downstream routing
./scripts/test-downstream-routing.sh

# Test with specific tenant
./scripts/test-downstream-routing.sh --test-tenant tenant-001

# Test with custom URL
./scripts/test-downstream-routing.sh --url https://payment-engine.local
```

---

## API Usage

### Fraud System Call

```bash
curl -X POST https://payment-engine.local/api/v1/downstream/fraud/tenant-001 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -H "X-Service-Type: fraud" \
  -d '{
    "transaction_id": "TXN-123",
    "amount": 1000,
    "currency": "USD",
    "fraud_check": true
  }'
```

### Clearing System Call

```bash
curl -X POST https://payment-engine.local/api/v1/downstream/clearing/tenant-001 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -H "X-Service-Type: clearing" \
  -d '{
    "transaction_id": "TXN-123",
    "clearing_reference": "CLR-456",
    "settlement_date": "2024-01-01"
  }'
```

### Auto-Routing Call

```bash
curl -X POST https://payment-engine.local/api/v1/downstream/auto/tenant-001 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -d '{
    "transaction_id": "TXN-123",
    "fraud_check": true,
    "risk_score": 85
  }'
```

### Specific Service Call

```bash
curl -X POST https://payment-engine.local/api/v1/downstream/service/tenant-001/fraud \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -d '{
    "transaction_id": "TXN-123",
    "amount": 1000
  }'
```

---

## Testing

### Automated Testing

```bash
# Run comprehensive tests
./scripts/test-downstream-routing.sh

# Test specific scenarios
./scripts/test-downstream-routing.sh --test-tenant tenant-001
```

### Manual Testing

```bash
# Test fraud system routing
curl -X POST http://localhost:8080/api/v1/downstream/fraud/tenant-001 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -d '{"transaction_id": "test-123"}'

# Test clearing system routing
curl -X POST http://localhost:8080/api/v1/downstream/clearing/tenant-001 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -d '{"transaction_id": "test-123"}'

# Test auto-routing
curl -X POST http://localhost:8080/api/v1/downstream/auto/tenant-001 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -d '{"fraud_check": true}'
```

### Test Scenarios

1. **Tenant Isolation**: Verify tenants cannot access each other's resources
2. **Service Type Routing**: Verify correct routing based on service type
3. **Header Injection**: Verify all required headers are injected
4. **Error Handling**: Test error scenarios and fallbacks
5. **Performance**: Test under load with multiple tenants

---

## Monitoring and Troubleshooting

### Monitoring Commands

```bash
# Check ServiceEntries
kubectl get serviceentries -n payment-engine

# Check VirtualServices
kubectl get virtualservices -n payment-engine -l routing-type=same-host-port

# Check DestinationRules
kubectl get destinationrules -n payment-engine -l external-service=bank-nginx

# Check EnvoyFilters
kubectl get envoyfilters -n payment-engine -l app=payment-engine

# Check AuthorizationPolicies
kubectl get authorizationpolicies -n payment-engine -l routing-type=downstream
```

### Troubleshooting Common Issues

#### 1. Routing Not Working

```bash
# Check VirtualService configuration
kubectl describe virtualservice same-host-port-routing -n payment-engine

# Check EnvoyFilter logs
kubectl logs -n payment-engine -l app=payment-processing-service | grep downstream

# Check Istio proxy configuration
istioctl proxy-config route <pod-name> -n payment-engine
```

#### 2. Headers Not Being Set

```bash
# Check EnvoyFilter configuration
kubectl describe envoyfilter request-body-routing -n payment-engine

# Check application logs
kubectl logs -n payment-engine -l app=payment-processing-service | grep headers

# Test header injection manually
curl -v -X POST http://localhost:8080/api/v1/downstream/fraud/tenant-001 \
  -H "Content-Type: application/json" \
  -d '{"test": "data"}'
```

#### 3. External Service Not Reachable

```bash
# Check ServiceEntry
kubectl describe serviceentry bank-nginx-single-entry -n payment-engine

# Test DNS resolution
kubectl exec -n payment-engine <pod-name> -- nslookup bank-nginx.example.com

# Test connectivity
kubectl exec -n payment-engine <pod-name> -- curl -k https://bank-nginx.example.com:443
```

### Debugging Tools

```bash
# Istio debugging
istioctl analyze -n payment-engine

# Check proxy configuration
istioctl proxy-config cluster <pod-name> -n payment-engine

# Check route configuration
istioctl proxy-config route <pod-name> -n payment-engine

# Check listener configuration
istioctl proxy-config listener <pod-name> -n payment-engine
```

---

## Best Practices

### 1. Header Naming Conventions

- **X-Tenant-ID**: Identifies the tenant
- **X-Service-Type**: Identifies the service (fraud/clearing)
- **X-Route-Context**: Combines tenant and service
- **X-Downstream-Route**: Final routing destination
- **X-Bank-Route**: Bank-specific routing path

### 2. Error Handling

```java
// Implement proper error handling
try {
    ResponseEntity<T> response = downstreamRoutingService.callFraudSystem(
        tenantId, requestBody, responseType, headers);
    return response;
} catch (Exception e) {
    logger.error("Downstream call failed for tenant: {}", tenantId, e);
    // Implement fallback logic
    return handleDownstreamError(e, tenantId);
}
```

### 3. Security Considerations

1. **Tenant Isolation**: Ensure complete tenant isolation
2. **Header Validation**: Validate all incoming headers
3. **Authentication**: Implement proper authentication for external calls
4. **Rate Limiting**: Implement rate limiting per tenant
5. **Audit Logging**: Log all downstream calls for audit

### 4. Performance Optimization

1. **Connection Pooling**: Configure appropriate connection pools
2. **Circuit Breakers**: Implement circuit breakers for resilience
3. **Timeout Configuration**: Set appropriate timeouts
4. **Retry Logic**: Implement intelligent retry logic
5. **Caching**: Cache frequently accessed data

### 5. Monitoring and Alerting

```yaml
# Prometheus metrics for downstream calls
- name: downstream_calls_total
  help: Total number of downstream calls
  labels: [tenant_id, service_type, status]

- name: downstream_call_duration_seconds
  help: Duration of downstream calls
  labels: [tenant_id, service_type]
```

---

## Conclusion

The downstream routing solution provides:

✅ **Conflict Resolution**: Resolves same host:port conflicts using header-based routing
✅ **Tenant Isolation**: Complete isolation between tenants
✅ **Service Type Routing**: Automatic routing based on service type
✅ **Flexibility**: Supports multiple routing strategies
✅ **Security**: Comprehensive security policies
✅ **Monitoring**: Full observability and troubleshooting capabilities

This solution eliminates the same host:port conflicts while providing a robust, secure, and scalable downstream routing architecture for multi-tenant environments.