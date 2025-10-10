-- V20241201_007__Multi_Level_Auth_Configuration_Tables.sql

-- Clearing System Level Authentication Configuration
CREATE TABLE clearing_system_auth_configuration (
    id UUID PRIMARY KEY,
    environment VARCHAR(50) NOT NULL,
    auth_method VARCHAR(20) NOT NULL,
    jwt_secret VARCHAR(500),
    jwt_issuer VARCHAR(100),
    jwt_audience VARCHAR(100),
    jwt_expiration_seconds INTEGER,
    jws_secret VARCHAR(500),
    jws_algorithm VARCHAR(20),
    jws_issuer VARCHAR(100),
    jws_audience VARCHAR(100),
    jws_expiration_seconds INTEGER,
    oauth2_token_endpoint VARCHAR(500),
    oauth2_client_id VARCHAR(500),
    oauth2_client_secret VARCHAR(500),
    oauth2_scope VARCHAR(100),
    api_key VARCHAR(500),
    api_key_header_name VARCHAR(100),
    basic_auth_username VARCHAR(100),
    basic_auth_password VARCHAR(500),
    include_client_headers BOOLEAN DEFAULT FALSE NOT NULL,
    client_id VARCHAR(100),
    client_secret VARCHAR(500),
    client_id_header_name VARCHAR(100),
    client_secret_header_name VARCHAR(100),
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clearing_system_auth_active ON clearing_system_auth_configuration (is_active);
CREATE INDEX idx_clearing_system_auth_environment ON clearing_system_auth_configuration (environment);

CREATE TABLE clearing_system_auth_configuration_metadata (
    clearing_system_auth_configuration_id UUID NOT NULL,
    metadata_key VARCHAR(255) NOT NULL,
    metadata_value TEXT,
    PRIMARY KEY (clearing_system_auth_configuration_id, metadata_key),
    FOREIGN KEY (clearing_system_auth_configuration_id) REFERENCES clearing_system_auth_configuration(id) ON DELETE CASCADE
);

-- Payment Type Level Authentication Configuration
CREATE TABLE payment_type_auth_configuration (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    auth_method VARCHAR(20) NOT NULL,
    jwt_secret VARCHAR(500),
    jwt_issuer VARCHAR(100),
    jwt_audience VARCHAR(100),
    jwt_expiration_seconds INTEGER,
    jws_secret VARCHAR(500),
    jws_algorithm VARCHAR(20),
    jws_issuer VARCHAR(100),
    jws_audience VARCHAR(100),
    jws_expiration_seconds INTEGER,
    oauth2_token_endpoint VARCHAR(500),
    oauth2_client_id VARCHAR(500),
    oauth2_client_secret VARCHAR(500),
    oauth2_scope VARCHAR(100),
    api_key VARCHAR(500),
    api_key_header_name VARCHAR(100),
    basic_auth_username VARCHAR(100),
    basic_auth_password VARCHAR(500),
    include_client_headers BOOLEAN DEFAULT FALSE NOT NULL,
    client_id VARCHAR(100),
    client_secret VARCHAR(500),
    client_id_header_name VARCHAR(100),
    client_secret_header_name VARCHAR(100),
    clearing_system VARCHAR(100),
    routing_code VARCHAR(100),
    currency VARCHAR(100),
    is_high_value BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_type_auth_payment_type ON payment_type_auth_configuration (payment_type);
CREATE INDEX idx_payment_type_auth_tenant ON payment_type_auth_configuration (tenant_id);
CREATE INDEX idx_payment_type_auth_active ON payment_type_auth_configuration (is_active);

CREATE TABLE payment_type_auth_configuration_metadata (
    payment_type_auth_configuration_id UUID NOT NULL,
    metadata_key VARCHAR(255) NOT NULL,
    metadata_value TEXT,
    PRIMARY KEY (payment_type_auth_configuration_id, metadata_key),
    FOREIGN KEY (payment_type_auth_configuration_id) REFERENCES payment_type_auth_configuration(id) ON DELETE CASCADE
);

-- Downstream Call Level Authentication Configuration
CREATE TABLE downstream_call_auth_configuration (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    endpoint VARCHAR(200) NOT NULL,
    payment_type VARCHAR(50),
    auth_method VARCHAR(20) NOT NULL,
    jwt_secret VARCHAR(500),
    jwt_issuer VARCHAR(100),
    jwt_audience VARCHAR(100),
    jwt_expiration_seconds INTEGER,
    jws_secret VARCHAR(500),
    jws_algorithm VARCHAR(20),
    jws_issuer VARCHAR(100),
    jws_audience VARCHAR(100),
    jws_expiration_seconds INTEGER,
    oauth2_token_endpoint VARCHAR(500),
    oauth2_client_id VARCHAR(500),
    oauth2_client_secret VARCHAR(500),
    oauth2_scope VARCHAR(100),
    api_key VARCHAR(500),
    api_key_header_name VARCHAR(100),
    basic_auth_username VARCHAR(100),
    basic_auth_password VARCHAR(500),
    include_client_headers BOOLEAN DEFAULT FALSE NOT NULL,
    client_id VARCHAR(100),
    client_secret VARCHAR(500),
    client_id_header_name VARCHAR(100),
    client_secret_header_name VARCHAR(100),
    target_host VARCHAR(500),
    target_port INTEGER,
    target_protocol VARCHAR(20),
    target_path VARCHAR(100),
    timeout_seconds INTEGER,
    retry_attempts INTEGER,
    retry_delay_seconds INTEGER,
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_downstream_call_auth_tenant ON downstream_call_auth_configuration (tenant_id);
CREATE INDEX idx_downstream_call_auth_service ON downstream_call_auth_configuration (service_type);
CREATE INDEX idx_downstream_call_auth_endpoint ON downstream_call_auth_configuration (endpoint);
CREATE INDEX idx_downstream_call_auth_active ON downstream_call_auth_configuration (is_active);

CREATE TABLE downstream_call_auth_configuration_metadata (
    downstream_call_auth_configuration_id UUID NOT NULL,
    metadata_key VARCHAR(255) NOT NULL,
    metadata_value TEXT,
    PRIMARY KEY (downstream_call_auth_configuration_id, metadata_key),
    FOREIGN KEY (downstream_call_auth_configuration_id) REFERENCES downstream_call_auth_configuration(id) ON DELETE CASCADE
);

-- Insert default clearing system configurations for different environments
INSERT INTO clearing_system_auth_configuration (
    id, environment, auth_method, jwt_secret, jwt_issuer, jwt_audience, jwt_expiration_seconds,
    include_client_headers, client_id, client_secret, client_id_header_name, client_secret_header_name,
    is_active, description
) VALUES 
(
    gen_random_uuid(), 'dev', 'JWT', 'dev-jwt-secret-key', 'payment-engine-dev', 'payment-engine-api', 3600,
    true, 'dev-client-id', 'dev-client-secret', 'X-Client-ID', 'X-Client-Secret',
    true, 'Default development environment configuration'
),
(
    gen_random_uuid(), 'staging', 'JWS', 'staging-jws-secret-key', 'payment-engine-staging', 'payment-engine-api', 3600,
    true, 'staging-client-id', 'staging-client-secret', 'X-Client-ID', 'X-Client-Secret',
    true, 'Default staging environment configuration'
),
(
    gen_random_uuid(), 'prod', 'JWS', 'prod-jws-secret-key', 'payment-engine-prod', 'payment-engine-api', 1800,
    true, 'prod-client-id', 'prod-client-secret', 'X-Client-ID', 'X-Client-Secret',
    true, 'Default production environment configuration'
);

-- Insert sample payment type configurations
INSERT INTO payment_type_auth_configuration (
    id, tenant_id, payment_type, auth_method, jwt_secret, jwt_issuer, jwt_audience, jwt_expiration_seconds,
    include_client_headers, client_id, client_secret, client_id_header_name, client_secret_header_name,
    clearing_system, currency, is_high_value, is_active, description
) VALUES 
(
    gen_random_uuid(), 'tenant-001', 'SEPA', 'JWT', 'sepa-jwt-secret', 'payment-engine-sepa', 'sepa-api', 3600,
    true, 'sepa-client-id', 'sepa-client-secret', 'X-SEPA-Client-ID', 'X-SEPA-Client-Secret',
    'SEPA_CLEARING', 'EUR', false, true, 'SEPA payment type configuration for tenant-001'
),
(
    gen_random_uuid(), 'tenant-001', 'SWIFT', 'JWS', 'swift-jws-secret', 'payment-engine-swift', 'swift-api', 1800,
    true, 'swift-client-id', 'swift-client-secret', 'X-SWIFT-Client-ID', 'X-SWIFT-Client-Secret',
    'SWIFT_CLEARING', 'USD', true, true, 'SWIFT payment type configuration for tenant-001'
),
(
    gen_random_uuid(), 'tenant-002', 'ACH', 'JWT', 'ach-jwt-secret', 'payment-engine-ach', 'ach-api', 3600,
    true, 'ach-client-id', 'ach-client-secret', 'X-ACH-Client-ID', 'X-ACH-Client-Secret',
    'ACH_CLEARING', 'USD', false, true, 'ACH payment type configuration for tenant-002'
);

-- Insert sample downstream call configurations
INSERT INTO downstream_call_auth_configuration (
    id, tenant_id, service_type, endpoint, payment_type, auth_method, jwt_secret, jwt_issuer, jwt_audience, jwt_expiration_seconds,
    include_client_headers, client_id, client_secret, client_id_header_name, client_secret_header_name,
    target_host, target_port, target_protocol, target_path, timeout_seconds, retry_attempts, retry_delay_seconds,
    is_active, description
) VALUES 
(
    gen_random_uuid(), 'tenant-001', 'fraud', '/fraud', 'SEPA', 'JWT', 'fraud-jwt-secret', 'payment-engine-fraud', 'fraud-api', 1800,
    true, 'fraud-client-id', 'fraud-client-secret', 'X-Fraud-Client-ID', 'X-Fraud-Client-Secret',
    'fraud.bank-nginx.example.com', 443, 'https', '/fraud', 30, 3, 5,
    true, 'Fraud system configuration for tenant-001 SEPA payments'
),
(
    gen_random_uuid(), 'tenant-001', 'clearing', '/clearing', 'SWIFT', 'JWS', 'clearing-jws-secret', 'payment-engine-clearing', 'clearing-api', 1800,
    true, 'clearing-client-id', 'clearing-client-secret', 'X-Clearing-Client-ID', 'X-Clearing-Client-Secret',
    'clearing.bank-nginx.example.com', 443, 'https', '/clearing', 60, 5, 10,
    true, 'Clearing system configuration for tenant-001 SWIFT payments'
),
(
    gen_random_uuid(), 'tenant-002', 'banking', '/banking', 'ACH', 'JWT', 'banking-jwt-secret', 'payment-engine-banking', 'banking-api', 3600,
    true, 'banking-client-id', 'banking-client-secret', 'X-Banking-Client-ID', 'X-Banking-Client-Secret',
    'banking.bank-nginx.example.com', 443, 'https', '/banking', 45, 3, 5,
    true, 'Banking system configuration for tenant-002 ACH payments'
);