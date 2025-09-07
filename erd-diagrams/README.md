# ISO 20022 Payment Engine - Entity Relationship Diagrams (ERD)

This directory contains comprehensive PlantUML Entity Relationship Diagrams (ERD) for the ISO 20022 payment processing system database, covering all database entities, relationships, constraints, and indexes.

## 📋 **ERD Overview**

### **1. Core Entities** (`01-core-entities.puml`)
**Purpose**: Core business entities and their relationships
**Coverage**:
- 🏢 **Tenants**: Multi-tenant architecture with tenant isolation
- ⚙️ **Scheme Configs**: ISO 20022 scheme configuration management
- 🏦 **Clearing Systems**: External clearing system definitions
- 🔗 **Clearing System Endpoints**: API endpoints for each clearing system
- 🗺️ **Tenant-Clearing System Mappings**: Routing rules based on tenant, payment type, and local instrument
- 📨 **Message Flows**: Message flow definitions and configurations
- 🛣️ **Routing Rules**: Dynamic routing rule configuration

**Key Features**:
- Complete tenant isolation
- Configurable clearing system endpoints
- Dynamic routing based on business rules
- Multi-format message support (JSON/XML)
- Configurable response modes (IMMEDIATE, ASYNC, KAFKA, WEBHOOK)
- Flow direction support (CLIENT_TO_CLEARING, CLEARING_TO_CLIENT, BIDIRECTIONAL)

### **2. Message Processing** (`02-message-processing.puml`)
**Purpose**: Message processing and transaction management entities
**Coverage**:
- 📨 **Message Transactions**: Core transaction lifecycle management
- 📄 **Message Payloads**: Message content storage with encryption and signing
- 🔄 **Message Transformations**: ISO 20022 message transformation tracking
- ✅ **Message Validations**: Validation results and error tracking
- 🔌 **Clearing System Interactions**: External system interaction logs
- 🪝 **Webhook Deliveries**: Asynchronous webhook delivery tracking
- 📨 **Kafka Messages**: Message queue integration and tracking
- 📋 **Message Audit Logs**: Comprehensive audit trail for all message operations

**Key Features**:
- Complete transaction lifecycle tracking
- Message encryption and digital signature support
- Transformation and validation tracking
- External system interaction logging
- Asynchronous delivery mechanisms
- Comprehensive audit trails

### **3. Security & Audit** (`03-security-audit.puml`)
**Purpose**: Security, authentication, and audit entities
**Coverage**:
- 👤 **Users**: User management with security features
- 🔐 **User Roles**: Role-based access control (RBAC)
- 🔑 **User Permissions**: Granular permission management
- 🎫 **OAuth Clients**: OAuth2 client management
- 🎟️ **OAuth Tokens**: Token lifecycle management
- 🔒 **Encryption Keys**: Key management for message encryption
- 📜 **Digital Certificates**: Certificate management for digital signatures
- 📋 **Audit Events**: Comprehensive audit logging
- 🚨 **Security Events**: Security incident tracking
- ⚡ **Rate Limit Violations**: Rate limiting and throttling
- 🔒 **Failed Login Attempts**: Authentication security tracking

**Key Features**:
- OAuth2/JWT authentication
- Role-based access control
- Message encryption and digital signatures
- Comprehensive audit logging
- Security event monitoring
- Rate limiting and account protection

### **4. Monitoring & Metrics** (`04-monitoring-metrics.puml`)
**Purpose**: Monitoring, metrics, and alerting entities
**Coverage**:
- 📊 **System Metrics**: Application and system performance metrics
- 💼 **Business Metrics**: Business-specific metrics and KPIs
- ⚡ **Performance Metrics**: API and service performance tracking
- ❌ **Error Metrics**: Error tracking and analysis
- 💚 **Health Checks**: Service health monitoring
- 🔄 **Circuit Breaker Metrics**: Resilience pattern monitoring
- ⚡ **Rate Limiter Metrics**: Rate limiting performance
- 📨 **Kafka Metrics**: Message queue performance
- 💾 **Database Metrics**: Database performance monitoring
- 🚀 **Cache Metrics**: Caching performance tracking
- 🚨 **Alert Rules**: Alert configuration and rules
- 🔔 **Alert Instances**: Active alert tracking
- 📢 **Notification Channels**: Notification delivery configuration
- 📤 **Notification Deliveries**: Notification delivery tracking

**Key Features**:
- Comprehensive metrics collection
- Real-time performance monitoring
- Health check management
- Circuit breaker and resilience monitoring
- Alert management and notification
- Multi-channel notification support

### **5. Configuration Management** (`05-configuration-management.puml`)
**Purpose**: Configuration management and feature flag entities
**Coverage**:
- 📋 **Configuration Templates**: Reusable configuration templates
- 🏢 **Tenant Configurations**: Tenant-specific configuration
- 🔧 **Service Configurations**: Service-level configuration
- 🚩 **Feature Flags**: Feature toggle management
- 🌍 **Environment Configurations**: Environment-specific settings
- 📚 **Configuration History**: Configuration change tracking
- ✅ **Configuration Validations**: Configuration validation rules
- 🔗 **Configuration Dependencies**: Configuration dependency management
- 📖 **Configuration Schemas**: Configuration schema definitions
- 🚀 **Configuration Deployments**: Configuration deployment tracking
- ✅ **Configuration Approvals**: Configuration approval workflow
- 📋 **Configuration Audit Logs**: Configuration change audit

**Key Features**:
- Dynamic configuration management
- Feature flag support
- Configuration validation
- Change tracking and audit
- Approval workflows
- Environment-specific configurations

### **6. Complete Database Schema** (`06-complete-database-schema.puml`)
**Purpose**: Complete database schema overview with all entities
**Coverage**:
- 🏗️ **Complete Entity Model**: All database entities in one view
- 🔗 **Relationship Overview**: All entity relationships
- 📊 **Schema Summary**: High-level schema understanding
- 🎯 **Key Entities**: Core entities and their purposes
- 📋 **Constraint Summary**: Key constraints and validations

**Key Features**:
- Complete database schema visualization
- Entity relationship overview
- Constraint and index summary
- Multi-domain coverage
- Scalable architecture design

## 🎯 **Database Design Principles**

### **Multi-Tenancy**
- **Tenant Isolation**: Complete data isolation between tenants
- **Tenant-Specific Configuration**: Per-tenant configuration management
- **Tenant-Scoped Access**: Role-based access control with tenant scoping
- **Tenant Metrics**: Business metrics per tenant
- **Tenant Audit**: Audit trails with tenant context

### **Scalability**
- **UUID Primary Keys**: Globally unique identifiers
- **Indexed Foreign Keys**: Optimized relationship queries
- **Partitioning Ready**: Design supports horizontal partitioning
- **Archive Strategy**: Historical data management
- **Performance Optimization**: Strategic indexing for performance

### **Security**
- **Data Encryption**: Support for encrypted data storage
- **Digital Signatures**: Message integrity and authenticity
- **Audit Trails**: Comprehensive audit logging
- **Access Control**: Role-based access control
- **Security Monitoring**: Security event tracking

### **Observability**
- **Metrics Collection**: Comprehensive metrics storage
- **Performance Monitoring**: API and service performance tracking
- **Health Monitoring**: Service health status tracking
- **Alert Management**: Proactive alerting and notification
- **Audit Logging**: Complete audit trail

### **Configuration Management**
- **Dynamic Configuration**: Runtime configuration changes
- **Feature Flags**: Feature toggle management
- **Environment Management**: Environment-specific configurations
- **Change Tracking**: Configuration change audit
- **Validation**: Configuration validation rules

## 🔧 **Technical Implementation Details**

### **Data Types**
- **UUID**: Primary keys for global uniqueness
- **VARCHAR**: Variable-length strings with appropriate limits
- **TEXT**: Large text fields for JSON/XML data
- **TIMESTAMP**: Time-based fields with timezone support
- **DECIMAL**: Precise numeric values for metrics
- **INTEGER/BIGINT**: Numeric fields for counts and IDs
- **BOOLEAN**: Boolean flags and status fields

### **Constraints**
- **Primary Keys**: UUID-based primary keys
- **Foreign Keys**: Referential integrity constraints
- **Unique Constraints**: Business key uniqueness
- **Check Constraints**: Data validation rules
- **Not Null Constraints**: Required field validation

### **Indexes**
- **Primary Indexes**: Primary key indexes
- **Foreign Key Indexes**: Relationship query optimization
- **Business Key Indexes**: Unique constraint indexes
- **Query Optimization Indexes**: Performance optimization
- **Composite Indexes**: Multi-column query optimization

### **Relationships**
- **One-to-Many**: Standard parent-child relationships
- **Many-to-Many**: Junction table relationships
- **Self-Referencing**: Hierarchical relationships
- **Optional Relationships**: Nullable foreign keys
- **Cascade Operations**: Referential integrity maintenance

## 📊 **Entity Categories**

### **Core Business Entities**
- **Tenants**: Multi-tenant architecture foundation
- **Clearing Systems**: External system integration
- **Scheme Configs**: ISO 20022 configuration
- **Routing Rules**: Business logic configuration

### **Message Processing Entities**
- **Message Transactions**: Transaction lifecycle
- **Message Payloads**: Message content storage
- **Transformations**: Message transformation tracking
- **Validations**: Validation result tracking

### **Security Entities**
- **Users**: User management
- **Roles & Permissions**: Access control
- **OAuth**: Authentication and authorization
- **Encryption**: Security key management

### **Monitoring Entities**
- **Metrics**: Performance and business metrics
- **Health Checks**: Service health monitoring
- **Alerts**: Alert management
- **Notifications**: Notification delivery

### **Configuration Entities**
- **Configurations**: Dynamic configuration
- **Feature Flags**: Feature management
- **Templates**: Reusable configurations
- **History**: Change tracking

## 🚀 **Usage Instructions**

### **Viewing the ERDs**
1. **Online**: Copy PlantUML code to http://www.plantuml.com/plantuml/
2. **VS Code**: Install PlantUML extension for live preview
3. **IntelliJ**: Install PlantUML plugin for integrated viewing
4. **Local**: Install PlantUML command-line tool

### **Database Implementation**
1. **Schema Creation**: Use ERD as reference for DDL scripts
2. **Migration Scripts**: Create Flyway/Liquibase migrations
3. **Index Creation**: Implement performance indexes
4. **Constraint Validation**: Add data validation constraints
5. **Testing**: Validate schema with test data

### **Development Integration**
- **Entity Mapping**: Use ERD for JPA entity creation
- **Repository Design**: Design repositories based on relationships
- **Query Optimization**: Use indexes for query performance
- **Data Validation**: Implement constraint validation
- **Audit Implementation**: Add audit logging based on ERD

## 📝 **Maintenance & Updates**

### **Schema Evolution**
- **Version Control**: Track schema changes in version control
- **Migration Scripts**: Create migration scripts for changes
- **Backward Compatibility**: Maintain backward compatibility
- **Testing**: Test schema changes thoroughly
- **Documentation**: Update documentation with changes

### **Performance Optimization**
- **Index Analysis**: Regular index performance analysis
- **Query Optimization**: Optimize queries based on usage patterns
- **Partitioning**: Implement partitioning for large tables
- **Archiving**: Archive historical data
- **Monitoring**: Monitor database performance

### **Security Maintenance**
- **Access Review**: Regular access control review
- **Audit Analysis**: Analyze audit logs for security issues
- **Key Rotation**: Regular encryption key rotation
- **Vulnerability Assessment**: Regular security assessments
- **Compliance**: Ensure compliance with regulations

This comprehensive set of ERDs provides complete visibility into the ISO 20022 payment processing system database design, enabling database administrators, developers, and architects to understand, implement, and maintain the database effectively! 🚀