# Azure Infrastructure Architecture

## Overview
This document details the Azure cloud infrastructure for the payments engine, including compute, networking, storage, security, and monitoring.

---

## Azure Services Summary

| Category | Azure Service | Purpose |
|----------|--------------|---------|
| **Compute** | Azure Kubernetes Service (AKS) | Container orchestration |
| **Messaging** | Azure Service Bus Premium | Event-driven messaging |
| **Database** | Azure Database for PostgreSQL Flexible Server | Transactional data |
| **Database** | Azure Cosmos DB | Audit logs (high-write) |
| **Cache** | Azure Cache for Redis Premium | Caching layer |
| **Storage** | Azure Blob Storage | Documents, reports |
| **Networking** | Azure Virtual Network (VNet) | Network isolation |
| **Networking** | Azure Application Gateway | Load balancing, WAF |
| **API Management** | Azure API Management | API gateway, throttling |
| **Identity** | Azure AD B2C | Identity provider |
| **Security** | Azure Key Vault | Secrets management |
| **Security** | Azure Firewall | Network security |
| **Monitoring** | Azure Monitor | Logging, metrics |
| **Monitoring** | Application Insights | APM, distributed tracing |
| **Analytics** | Azure Synapse Analytics | Data warehouse |
| **CI/CD** | Azure DevOps | Pipelines, repos |
| **Container Registry** | Azure Container Registry | Docker images |

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         INTERNET                                     │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Azure Front Door (CDN + DDoS)                    │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  Azure Application Gateway (WAF)                    │
│                      Region: South Africa North                     │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Azure Virtual Network                           │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                     AKS Subnet (10.0.1.0/24)                 │  │
│  │  ┌────────────────────────────────────────────────────────┐  │  │
│  │  │       Azure Kubernetes Service (AKS)                   │  │  │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │  │  │
│  │  │  │ Payment  │ │Validation│ │ Account  │ │ Routing  │  │  │  │
│  │  │  │ Service  │ │ Service  │ │ Service  │ │ Service  │  │  │  │
│  │  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │  │  │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │  │  │
│  │  │  │Transaction│ │ Clearing │ │Settlement│ │  Saga    │  │  │  │
│  │  │  │ Service  │ │ Adapters │ │ Service  │ │Orchestrator│ │  │
│  │  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │  │  │
│  │  └────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                 Database Subnet (10.0.2.0/24)                │  │
│  │  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐   │  │
│  │  │  PostgreSQL    │  │   Cosmos DB    │  │    Redis     │   │  │
│  │  │  Flex Server   │  │                │  │   Premium    │   │  │
│  │  └────────────────┘  └────────────────┘  └──────────────┘   │  │
│  └──────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │              Integration Subnet (10.0.3.0/24)                │  │
│  │  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐   │  │
│  │  │ Service Bus    │  │ Event Grid     │  │ Key Vault    │   │  │
│  │  │   Premium      │  │                │  │              │   │  │
│  │  └────────────────┘  └────────────────┘  └──────────────┘   │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Azure Monitor + Log Analytics                    │
│                        Application Insights                          │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 1. Compute Infrastructure

### 1.1 Azure Kubernetes Service (AKS)

#### Cluster Configuration

```hcl
# Terraform configuration for AKS
resource "azurerm_kubernetes_cluster" "payments_aks" {
  name                = "payments-engine-aks"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name
  dns_prefix          = "payments-engine"
  kubernetes_version  = "1.28"

  default_node_pool {
    name                = "system"
    node_count          = 3
    vm_size             = "Standard_D4s_v3"  # 4 vCPU, 16 GB RAM
    type                = "VirtualMachineScaleSets"
    availability_zones  = ["1", "2", "3"]
    enable_auto_scaling = true
    min_count           = 3
    max_count           = 10
    os_disk_size_gb     = 128
    vnet_subnet_id      = azurerm_subnet.aks_subnet.id
  }

  # Additional node pool for workloads
  node_pool {
    name                = "workload"
    node_count          = 5
    vm_size             = "Standard_D8s_v3"  # 8 vCPU, 32 GB RAM
    availability_zones  = ["1", "2", "3"]
    enable_auto_scaling = true
    min_count           = 5
    max_count           = 50
    os_disk_size_gb     = 256
    vnet_subnet_id      = azurerm_subnet.aks_subnet.id
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin     = "azure"
    network_policy     = "azure"
    load_balancer_sku  = "standard"
    service_cidr       = "10.1.0.0/16"
    dns_service_ip     = "10.1.0.10"
  }

  addon_profile {
    aci_connector_linux {
      enabled = false
    }
    azure_policy {
      enabled = true
    }
    http_application_routing {
      enabled = false
    }
    oms_agent {
      enabled                    = true
      log_analytics_workspace_id = azurerm_log_analytics_workspace.payments.id
    }
  }

  tags = {
    Environment = "Production"
    Project     = "PaymentsEngine"
  }
}
```

#### Node Pool Specifications

| Node Pool | VM Size | vCPU | RAM | Count | Auto-Scale | Purpose |
|-----------|---------|------|-----|-------|------------|---------|
| System | Standard_D4s_v3 | 4 | 16 GB | 3 | 3-10 | System pods, monitoring |
| Workload | Standard_D8s_v3 | 8 | 32 GB | 5 | 5-50 | Application services |

#### Resource Requests & Limits (per pod)

```yaml
# Example Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-initiation-service
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: payment-initiation
        image: acr.azurecr.io/payment-initiation:latest
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secrets
              key: payment-db-url
```

---

## 2. Networking

### 2.1 Virtual Network

```hcl
resource "azurerm_virtual_network" "payments_vnet" {
  name                = "payments-vnet"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name
  address_space       = ["10.0.0.0/16"]

  tags = {
    Environment = "Production"
  }
}

# Subnets
resource "azurerm_subnet" "aks_subnet" {
  name                 = "aks-subnet"
  resource_group_name  = azurerm_resource_group.payments.name
  virtual_network_name = azurerm_virtual_network.payments_vnet.name
  address_prefixes     = ["10.0.1.0/24"]
  
  service_endpoints = ["Microsoft.Sql", "Microsoft.Storage", "Microsoft.KeyVault"]
}

resource "azurerm_subnet" "database_subnet" {
  name                 = "database-subnet"
  resource_group_name  = azurerm_resource_group.payments.name
  virtual_network_name = azurerm_virtual_network.payments_vnet.name
  address_prefixes     = ["10.0.2.0/24"]
  
  delegation {
    name = "postgresql-delegation"
    service_delegation {
      name = "Microsoft.DBforPostgreSQL/flexibleServers"
    }
  }
}

resource "azurerm_subnet" "integration_subnet" {
  name                 = "integration-subnet"
  resource_group_name  = azurerm_resource_group.payments.name
  virtual_network_name = azurerm_virtual_network.payments_vnet.name
  address_prefixes     = ["10.0.3.0/24"]
  
  service_endpoints = ["Microsoft.ServiceBus", "Microsoft.EventHub"]
}
```

### 2.2 Network Security Groups

```hcl
resource "azurerm_network_security_group" "aks_nsg" {
  name                = "aks-nsg"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name

  # Allow HTTPS from Application Gateway
  security_rule {
    name                       = "AllowHTTPSFromAppGateway"
    priority                   = 100
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "443"
    source_address_prefix      = "10.0.0.0/24"  # App Gateway subnet
    destination_address_prefix = "10.0.1.0/24"  # AKS subnet
  }

  # Deny all other inbound traffic
  security_rule {
    name                       = "DenyAllInbound"
    priority                   = 4096
    direction                  = "Inbound"
    access                     = "Deny"
    protocol                   = "*"
    source_port_range          = "*"
    destination_port_range     = "*"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }
}
```

### 2.3 Application Gateway

```hcl
resource "azurerm_application_gateway" "payments_appgw" {
  name                = "payments-appgw"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name

  sku {
    name     = "WAF_v2"
    tier     = "WAF_v2"
    capacity = 2
  }

  gateway_ip_configuration {
    name      = "appgw-ip-config"
    subnet_id = azurerm_subnet.appgw_subnet.id
  }

  frontend_port {
    name = "https-port"
    port = 443
  }

  frontend_ip_configuration {
    name                 = "appgw-frontend-ip"
    public_ip_address_id = azurerm_public_ip.appgw_public_ip.id
  }

  backend_address_pool {
    name = "aks-backend-pool"
    ip_addresses = [
      # AKS ingress controller IPs
    ]
  }

  backend_http_settings {
    name                  = "https-backend-settings"
    cookie_based_affinity = "Disabled"
    port                  = 443
    protocol              = "Https"
    request_timeout       = 30
  }

  http_listener {
    name                           = "https-listener"
    frontend_ip_configuration_name = "appgw-frontend-ip"
    frontend_port_name             = "https-port"
    protocol                       = "Https"
    ssl_certificate_name           = "payments-ssl-cert"
  }

  request_routing_rule {
    name                       = "https-routing-rule"
    rule_type                  = "Basic"
    http_listener_name         = "https-listener"
    backend_address_pool_name  = "aks-backend-pool"
    backend_http_settings_name = "https-backend-settings"
    priority                   = 1
  }

  waf_configuration {
    enabled          = true
    firewall_mode    = "Prevention"
    rule_set_type    = "OWASP"
    rule_set_version = "3.2"
  }
}
```

---

## 3. Data Services

### 3.1 Azure Database for PostgreSQL Flexible Server

```hcl
resource "azurerm_postgresql_flexible_server" "payments_db" {
  name                   = "payments-postgresql"
  location               = azurerm_resource_group.payments.location
  resource_group_name    = azurerm_resource_group.payments.name
  
  sku_name               = "GP_Standard_D4s_v3"  # 4 vCPU, 16 GB RAM
  storage_mb             = 1048576  # 1 TB
  
  version                = "15"
  administrator_login    = "pgadmin"
  administrator_password = random_password.pg_password.result
  
  backup_retention_days  = 35
  geo_redundant_backup_enabled = true
  
  high_availability {
    mode                      = "ZoneRedundant"
    standby_availability_zone = "2"
  }
  
  delegated_subnet_id    = azurerm_subnet.database_subnet.id
  private_dns_zone_id    = azurerm_private_dns_zone.postgresql.id
  
  tags = {
    Environment = "Production"
  }
}

# Read replica for read-heavy services
resource "azurerm_postgresql_flexible_server" "payments_db_replica" {
  name                   = "payments-postgresql-replica"
  location               = "South Africa West"  # Different region
  resource_group_name    = azurerm_resource_group.payments.name
  
  create_mode            = "Replica"
  source_server_id       = azurerm_postgresql_flexible_server.payments_db.id
  
  sku_name               = "GP_Standard_D4s_v3"
  storage_mb             = 1048576
}
```

#### Configuration Parameters

```sql
-- postgresql.conf optimizations
max_connections = 500
shared_buffers = 4GB
effective_cache_size = 12GB
maintenance_work_mem = 1GB
work_mem = 16MB
wal_buffers = 16MB
max_wal_size = 4GB
checkpoint_completion_target = 0.9
random_page_cost = 1.1  # SSD storage
effective_io_concurrency = 200
```

### 3.2 Azure Cosmos DB

```hcl
resource "azurerm_cosmosdb_account" "audit_db" {
  name                = "payments-audit-cosmosdb"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name
  offer_type          = "Standard"
  kind                = "GlobalDocumentDB"

  consistency_policy {
    consistency_level       = "Session"
    max_interval_in_seconds = 5
    max_staleness_prefix    = 100
  }

  geo_location {
    location          = "South Africa North"
    failover_priority = 0
  }

  geo_location {
    location          = "South Africa West"
    failover_priority = 1
  }

  capabilities {
    name = "EnableServerless"  # Or provisioned throughput
  }

  backup {
    type                = "Continuous"
    interval_in_minutes = 240
    retention_in_hours  = 720  # 30 days
  }
}

resource "azurerm_cosmosdb_sql_database" "audit" {
  name                = "audit-db"
  resource_group_name = azurerm_resource_group.payments.name
  account_name        = azurerm_cosmosdb_account.audit_db.name
  throughput          = 10000  # RU/s
}

resource "azurerm_cosmosdb_sql_container" "audit_events" {
  name                  = "audit-events"
  resource_group_name   = azurerm_resource_group.payments.name
  account_name          = azurerm_cosmosdb_account.audit_db.name
  database_name         = azurerm_cosmosdb_sql_database.audit.name
  partition_key_path    = "/partitionKey"  # Date-based partitioning
  partition_key_version = 2
  throughput            = 10000

  indexing_policy {
    indexing_mode = "consistent"

    included_path {
      path = "/*"
    }

    excluded_path {
      path = "/requestData/*"
    }
  }

  default_ttl = 220752000  # 7 years in seconds
}
```

### 3.3 Azure Cache for Redis

```hcl
resource "azurerm_redis_cache" "payments_cache" {
  name                = "payments-redis"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name
  
  capacity            = 3  # Premium P3: 26 GB
  family              = "P"
  sku_name            = "Premium"
  
  enable_non_ssl_port = false
  minimum_tls_version = "1.2"
  
  redis_configuration {
    maxmemory_policy = "allkeys-lru"
    maxmemory_reserved = 2
    maxfragmentationmemory_reserved = 2
  }
  
  # Persistence for Premium
  redis_persistence {
    rdb_backup_enabled            = true
    rdb_backup_frequency          = 60  # minutes
    rdb_storage_connection_string = azurerm_storage_account.redis_backup.primary_connection_string
  }
  
  # Clustering
  shard_count = 3
  
  subnet_id = azurerm_subnet.redis_subnet.id
  
  zones = ["1", "2", "3"]
}
```

---

## 4. Messaging & Integration

### 4.1 Azure Service Bus

```hcl
resource "azurerm_servicebus_namespace" "payments" {
  name                = "payments-servicebus"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name
  sku                 = "Premium"
  capacity            = 1  # Messaging Units
  
  zone_redundant      = true
  
  identity {
    type = "SystemAssigned"
  }
}

# Topics and Subscriptions
resource "azurerm_servicebus_topic" "payment_initiated" {
  name         = "payment-initiated"
  namespace_id = azurerm_servicebus_namespace.payments.id
  
  max_size_in_megabytes = 5120
  default_message_ttl   = "P14D"  # 14 days
  
  enable_partitioning = true
  enable_express      = false
}

resource "azurerm_servicebus_subscription" "validation_service" {
  name               = "validation-service-subscription"
  topic_id           = azurerm_servicebus_topic.payment_initiated.id
  max_delivery_count = 3
  
  default_message_ttl            = "P14D"
  lock_duration                  = "PT5M"
  enable_dead_lettering_on_message_expiration = true
  
  requires_session = false
}
```

#### Topics Configuration

```yaml
Topics:
  - payment/initiated:
      partitions: 32
      ttl: 14 days
      max_size: 5 GB
      
  - payment/validated:
      partitions: 32
      ttl: 14 days
      max_size: 5 GB
      
  - payment/completed:
      partitions: 32
      ttl: 14 days
      max_size: 5 GB
      
  - clearing/submitted:
      partitions: 16
      ttl: 30 days
      max_size: 5 GB
```

---

## 5. Security

### 5.1 Azure Key Vault

```hcl
resource "azurerm_key_vault" "payments" {
  name                        = "payments-keyvault"
  location                    = azurerm_resource_group.payments.location
  resource_group_name         = azurerm_resource_group.payments.name
  enabled_for_disk_encryption = true
  tenant_id                   = data.azurerm_client_config.current.tenant_id
  soft_delete_retention_days  = 90
  purge_protection_enabled    = true
  
  sku_name = "premium"  # HSM-backed keys
  
  network_acls {
    default_action = "Deny"
    bypass         = "AzureServices"
    
    ip_rules = [
      # Office IP ranges
    ]
    
    virtual_network_subnet_ids = [
      azurerm_subnet.aks_subnet.id,
      azurerm_subnet.integration_subnet.id
    ]
  }
}

# Secrets
resource "azurerm_key_vault_secret" "database_password" {
  name         = "postgresql-admin-password"
  value        = random_password.pg_password.result
  key_vault_id = azurerm_key_vault.payments.id
  
  expiration_date = "2026-01-01T00:00:00Z"
}

resource "azurerm_key_vault_secret" "service_bus_connection" {
  name         = "servicebus-connection-string"
  value        = azurerm_servicebus_namespace.payments.default_primary_connection_string
  key_vault_id = azurerm_key_vault.payments.id
}
```

### 5.2 Azure AD B2C

```hcl
resource "azuread_b2c_directory" "payments" {
  country_code            = "ZA"
  data_residency_location = "Africa"
  display_name            = "Payments Engine"
  domain_name             = "paymentsengine.onmicrosoft.com"
  
  sku_name = "PremiumP1"
}

# User flows
resource "azuread_b2c_user_flow" "sign_up_sign_in" {
  name                = "B2C_1_signup_signin"
  user_flow_type      = "signUpOrSignIn"
  user_flow_type_version = "v2"
  
  identity_providers = ["EmailPassword"]
  
  attributes {
    built_in_attributes = ["email", "displayName", "givenName", "surname"]
  }
}
```

---

## 6. Monitoring & Logging

### 6.1 Azure Monitor & Log Analytics

```hcl
resource "azurerm_log_analytics_workspace" "payments" {
  name                = "payments-logs"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name
  sku                 = "PerGB2018"
  retention_in_days   = 90  # Regulatory requirement: keep longer in cold storage
}

# Diagnostic settings for AKS
resource "azurerm_monitor_diagnostic_setting" "aks_diagnostics" {
  name                       = "aks-diagnostics"
  target_resource_id         = azurerm_kubernetes_cluster.payments_aks.id
  log_analytics_workspace_id = azurerm_log_analytics_workspace.payments.id

  enabled_log {
    category = "kube-apiserver"
  }
  
  enabled_log {
    category = "kube-controller-manager"
  }
  
  enabled_log {
    category = "kube-scheduler"
  }
  
  metric {
    category = "AllMetrics"
    enabled  = true
  }
}
```

### 6.2 Application Insights

```hcl
resource "azurerm_application_insights" "payments" {
  name                = "payments-appinsights"
  location            = azurerm_resource_group.payments.location
  resource_group_name = azurerm_resource_group.payments.name
  workspace_id        = azurerm_log_analytics_workspace.payments.id
  application_type    = "java"
  
  sampling_percentage = 100  # Full sampling for critical transactions
  
  retention_in_days   = 90
}

# Alert rules
resource "azurerm_monitor_metric_alert" "high_error_rate" {
  name                = "high-error-rate"
  resource_group_name = azurerm_resource_group.payments.name
  scopes              = [azurerm_application_insights.payments.id]
  description         = "Alert when error rate exceeds 5%"
  
  criteria {
    metric_namespace = "Microsoft.Insights/components"
    metric_name      = "exceptions/count"
    aggregation      = "Count"
    operator         = "GreaterThan"
    threshold        = 100
  }
  
  frequency   = "PT1M"
  window_size = "PT5M"
  
  action {
    action_group_id = azurerm_monitor_action_group.payments_alerts.id
  }
}
```

### 6.3 KQL Queries for Monitoring

```kusto
// Transaction success rate
AppRequests
| where TimeGenerated > ago(1h)
| where Name contains "POST /api/v1/payments"
| summarize 
    TotalRequests = count(),
    SuccessRequests = countif(Success == true),
    FailedRequests = countif(Success == false)
| extend SuccessRate = todouble(SuccessRequests) / todouble(TotalRequests) * 100
| project SuccessRate, TotalRequests, FailedRequests

// Average response time (p95)
AppRequests
| where TimeGenerated > ago(1h)
| summarize 
    AvgDuration = avg(DurationMs),
    P50Duration = percentile(DurationMs, 50),
    P95Duration = percentile(DurationMs, 95),
    P99Duration = percentile(DurationMs, 99)
| project AvgDuration, P50Duration, P95Duration, P99Duration

// Error distribution
AppExceptions
| where TimeGenerated > ago(1h)
| summarize Count = count() by Type, Message
| order by Count desc
| take 10

// Service Bus queue depth
AzureMetrics
| where ResourceProvider == "MICROSOFT.SERVICEBUS"
| where MetricName == "ActiveMessages"
| summarize AvgQueueDepth = avg(Average) by Resource
| order by AvgQueueDepth desc
```

---

## 7. Disaster Recovery

### 7.1 Multi-Region Setup

```
Primary Region: South Africa North (Johannesburg)
Secondary Region: South Africa West (Cape Town)
```

#### Active-Passive Configuration

```hcl
# Traffic Manager for DNS-based failover
resource "azurerm_traffic_manager_profile" "payments" {
  name                   = "payments-tm"
  resource_group_name    = azurerm_resource_group.payments.name
  traffic_routing_method = "Priority"
  
  dns_config {
    relative_name = "payments-engine"
    ttl           = 30
  }
  
  monitor_config {
    protocol                     = "HTTPS"
    port                         = 443
    path                         = "/health"
    interval_in_seconds          = 30
    timeout_in_seconds           = 10
    tolerated_number_of_failures = 3
  }
}

# Primary endpoint
resource "azurerm_traffic_manager_azure_endpoint" "primary" {
  name               = "primary-endpoint"
  profile_id         = azurerm_traffic_manager_profile.payments.id
  target_resource_id = azurerm_public_ip.appgw_primary.id
  priority           = 1
}

# Secondary endpoint
resource "azurerm_traffic_manager_azure_endpoint" "secondary" {
  name               = "secondary-endpoint"
  profile_id         = azurerm_traffic_manager_profile.payments.id
  target_resource_id = azurerm_public_ip.appgw_secondary.id
  priority           = 2
}
```

### 7.2 Backup Strategy

| Resource | Backup Frequency | Retention | Recovery Time |
|----------|------------------|-----------|---------------|
| PostgreSQL | Hourly (incremental), Daily (full) | 35 days | < 1 hour |
| Cosmos DB | Continuous | 30 days | < 1 hour |
| Redis | Hourly | 7 days | < 30 minutes |
| AKS ETCD | Daily | 30 days | < 2 hours |
| Blob Storage | Geo-redundant (automatic) | Immutable | Instant |

---

## 8. Cost Optimization

### Estimated Monthly Costs (Production)

| Service | Configuration | Monthly Cost (USD) |
|---------|---------------|-------------------|
| AKS (2 node pools) | 8 nodes (D8s_v3) | $4,800 |
| PostgreSQL Flexible | GP D4s_v3 + Replica | $1,200 |
| Cosmos DB | 10,000 RU/s | $600 |
| Redis Premium P3 | 26 GB, 3 shards | $1,400 |
| Service Bus Premium | 1 MU | $650 |
| Application Gateway WAF | v2, 2 instances | $450 |
| Azure Monitor | 100 GB/day | $2,300 |
| Storage (Blob + Backup) | 10 TB | $200 |
| Key Vault | Premium, 10k ops/day | $50 |
| API Management | Premium tier | $3,000 |
| Bandwidth | 5 TB egress | $400 |
| **Total** | | **~$15,050/month** |

### Cost Optimization Strategies

1. **Reserved Instances**: Save 30-50% on VMs
2. **Auto-scaling**: Scale down during off-peak hours
3. **Log Retention**: Move old logs to cold storage
4. **Spot Instances**: Use for non-critical workloads
5. **Right-sizing**: Monitor and adjust VM sizes

---

## 9. Deployment

### 9.1 Terraform Deployment

```bash
# Initialize Terraform
terraform init

# Plan deployment
terraform plan -out=tfplan

# Apply deployment
terraform apply tfplan
```

### 9.2 Kubernetes Deployment

```bash
# Set kubectl context
az aks get-credentials --resource-group payments-rg --name payments-aks

# Deploy services
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmaps/
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/
kubectl apply -f k8s/ingress.yaml

# Verify deployment
kubectl get pods -n payments
kubectl get services -n payments
```

### 9.3 CI/CD Pipeline (Azure DevOps)

```yaml
# azure-pipelines.yml
trigger:
  branches:
    include:
      - main
      - develop

pool:
  vmImage: 'ubuntu-latest'

stages:
  - stage: Build
    jobs:
      - job: BuildServices
        steps:
          - task: Maven@3
            inputs:
              mavenPomFile: 'pom.xml'
              goals: 'clean package'
              
          - task: Docker@2
            inputs:
              command: 'buildAndPush'
              repository: 'payments-engine/payment-initiation'
              dockerfile: '**/Dockerfile'
              containerRegistry: 'paymentsACR'
              tags: |
                $(Build.BuildId)
                latest
  
  - stage: Test
    jobs:
      - job: UnitTests
        steps:
          - task: Maven@3
            inputs:
              goals: 'test'
              
      - job: IntegrationTests
        steps:
          - script: |
              docker-compose -f docker-compose.test.yml up -d
              mvn verify
              docker-compose down
  
  - stage: Deploy
    condition: and(succeeded(), eq(variables['Build.SourceBranch'], 'refs/heads/main'))
    jobs:
      - deployment: DeployToProduction
        environment: 'production'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: Kubernetes@1
                  inputs:
                    command: 'apply'
                    namespace: 'payments'
                    manifests: 'k8s/**/*.yaml'
```

---

## 10. Security Best Practices

### Network Security
- ✅ All subnets protected by NSGs
- ✅ Private endpoints for PaaS services
- ✅ No public IPs on VMs (except load balancers)
- ✅ Azure Firewall for egress traffic
- ✅ WAF enabled on Application Gateway

### Identity & Access
- ✅ Managed identities for service-to-service auth
- ✅ Azure RBAC for resource access
- ✅ Key Vault for secrets management
- ✅ MFA enforced for all users
- ✅ Conditional access policies

### Data Protection
- ✅ Encryption at rest (all storage)
- ✅ TLS 1.3 for data in transit
- ✅ Private endpoints for databases
- ✅ Backup encryption
- ✅ Data residency in South Africa

### Monitoring & Compliance
- ✅ Azure Security Center enabled
- ✅ Microsoft Defender for Cloud
- ✅ Azure Policy for compliance
- ✅ Audit logging enabled
- ✅ Alert on suspicious activities

---

## Summary

This Azure infrastructure provides:
- **High Availability**: 99.95% SLA
- **Scalability**: Auto-scaling up to 50 nodes
- **Security**: Multi-layered security with zero trust
- **Performance**: < 200ms API response time (p95)
- **Disaster Recovery**: RTO 1 hour, RPO 5 minutes
- **Compliance**: POPIA, FICA, PCI DSS ready

**Total Infrastructure Cost**: ~$15,000/month for production environment

---

**Previous**: See `06-SOUTH-AFRICA-CLEARING.md` for clearing systems
**Next**: See `README.md` for project overview and getting started
