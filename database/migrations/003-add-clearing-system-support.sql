-- Migration: Add Clearing System Support
-- Description: Creates tables for clearing system configurations, tenant mappings, and endpoints

-- Create clearing_systems table
CREATE TABLE IF NOT EXISTS clearing_systems (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    country_code VARCHAR(3),
    currency VARCHAR(3),
    is_active BOOLEAN NOT NULL DEFAULT true,
    processing_mode VARCHAR(20), -- SYNCHRONOUS, ASYNCHRONOUS, BATCH
    timeout_seconds INTEGER,
    endpoint_url VARCHAR(500),
    authentication_type VARCHAR(20), -- NONE, API_KEY, JWT, OAUTH2, MTLS
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create clearing_system_authentication_config table
CREATE TABLE IF NOT EXISTS clearing_system_authentication_config (
    clearing_system_id UUID NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    PRIMARY KEY (clearing_system_id, config_key),
    FOREIGN KEY (clearing_system_id) REFERENCES clearing_systems(id) ON DELETE CASCADE
);

-- Create clearing_system_supported_messages table
CREATE TABLE IF NOT EXISTS clearing_system_supported_messages (
    clearing_system_id UUID NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    message_description VARCHAR(200),
    PRIMARY KEY (clearing_system_id, message_type),
    FOREIGN KEY (clearing_system_id) REFERENCES clearing_systems(id) ON DELETE CASCADE
);

-- Create clearing_system_supported_payment_types table
CREATE TABLE IF NOT EXISTS clearing_system_supported_payment_types (
    clearing_system_id UUID NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    payment_description VARCHAR(200),
    PRIMARY KEY (clearing_system_id, payment_type),
    FOREIGN KEY (clearing_system_id) REFERENCES clearing_systems(id) ON DELETE CASCADE
);

-- Create clearing_system_supported_instruments table
CREATE TABLE IF NOT EXISTS clearing_system_supported_instruments (
    clearing_system_id UUID NOT NULL,
    instrument_code VARCHAR(50) NOT NULL,
    instrument_description VARCHAR(200),
    PRIMARY KEY (clearing_system_id, instrument_code),
    FOREIGN KEY (clearing_system_id) REFERENCES clearing_systems(id) ON DELETE CASCADE
);

-- Create tenant_clearing_system_mappings table
CREATE TABLE IF NOT EXISTS tenant_clearing_system_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    local_instrument_code VARCHAR(50),
    clearing_system_code VARCHAR(20) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, payment_type, local_instrument_code),
    FOREIGN KEY (clearing_system_code) REFERENCES clearing_systems(code)
);

-- Create clearing_system_endpoints table
CREATE TABLE IF NOT EXISTS clearing_system_endpoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clearing_system_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    endpoint_type VARCHAR(50) NOT NULL, -- SYNC, ASYNC, POLLING, WEBHOOK
    message_type VARCHAR(50) NOT NULL, -- pacs008, pacs002, pain001, pain002
    url VARCHAR(500) NOT NULL,
    http_method VARCHAR(10), -- GET, POST, PUT, DELETE
    timeout_ms INTEGER,
    retry_attempts INTEGER,
    authentication_type VARCHAR(20), -- NONE, API_KEY, JWT, OAUTH2, MTLS
    is_active BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 1,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (clearing_system_id) REFERENCES clearing_systems(id) ON DELETE CASCADE
);

-- Create clearing_system_endpoint_auth_config table
CREATE TABLE IF NOT EXISTS clearing_system_endpoint_auth_config (
    endpoint_id UUID NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    PRIMARY KEY (endpoint_id, config_key),
    FOREIGN KEY (endpoint_id) REFERENCES clearing_system_endpoints(id) ON DELETE CASCADE
);

-- Create clearing_system_endpoint_headers table
CREATE TABLE IF NOT EXISTS clearing_system_endpoint_headers (
    endpoint_id UUID NOT NULL,
    header_name VARCHAR(100) NOT NULL,
    header_value TEXT,
    PRIMARY KEY (endpoint_id, header_name),
    FOREIGN KEY (endpoint_id) REFERENCES clearing_system_endpoints(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_clearing_systems_code ON clearing_systems(code);
CREATE INDEX IF NOT EXISTS idx_clearing_systems_active ON clearing_systems(is_active);
CREATE INDEX IF NOT EXISTS idx_clearing_systems_country ON clearing_systems(country_code);
CREATE INDEX IF NOT EXISTS idx_clearing_systems_currency ON clearing_systems(currency);

CREATE INDEX IF NOT EXISTS idx_tenant_mappings_tenant ON tenant_clearing_system_mappings(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_mappings_payment_type ON tenant_clearing_system_mappings(payment_type);
CREATE INDEX IF NOT EXISTS idx_tenant_mappings_instrument ON tenant_clearing_system_mappings(local_instrument_code);
CREATE INDEX IF NOT EXISTS idx_tenant_mappings_clearing_system ON tenant_clearing_system_mappings(clearing_system_code);
CREATE INDEX IF NOT EXISTS idx_tenant_mappings_active ON tenant_clearing_system_mappings(is_active);

CREATE INDEX IF NOT EXISTS idx_endpoints_clearing_system ON clearing_system_endpoints(clearing_system_id);
CREATE INDEX IF NOT EXISTS idx_endpoints_type ON clearing_system_endpoints(endpoint_type);
CREATE INDEX IF NOT EXISTS idx_endpoints_message_type ON clearing_system_endpoints(message_type);
CREATE INDEX IF NOT EXISTS idx_endpoints_active ON clearing_system_endpoints(is_active);

-- Insert default clearing systems
INSERT INTO clearing_systems (code, name, description, country_code, currency, processing_mode, timeout_seconds, endpoint_url, authentication_type) VALUES
('FEDWIRE', 'Federal Reserve Wire Network', 'US domestic wire transfer system', 'US', 'USD', 'SYNCHRONOUS', 30, 'https://api.fedwire.com/v1/payments', 'API_KEY'),
('CHAPS', 'Clearing House Automated Payment System', 'UK same-day high-value payment system', 'GB', 'GBP', 'SYNCHRONOUS', 30, 'https://api.chaps.co.uk/v1/payments', 'API_KEY'),
('SEPA', 'Single Euro Payments Area', 'European payment system for euro transactions', 'EU', 'EUR', 'ASYNCHRONOUS', 60, 'https://api.sepa.eu/v1/payments', 'API_KEY'),
('ACH', 'Automated Clearing House', 'US batch payment system', 'US', 'USD', 'BATCH', 3600, 'https://api.ach.com/v1/payments', 'API_KEY'),
('RTP', 'Real-Time Payments', 'US real-time payment system', 'US', 'USD', 'SYNCHRONOUS', 10, 'https://api.rtp.com/v1/payments', 'API_KEY')
ON CONFLICT (code) DO NOTHING;

-- Insert supported message types for each clearing system
INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'pacs008', 'FI to FI Customer Credit Transfer'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'pacs002', 'FI to FI Payment Status Report'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'pacs004', 'Payment Return'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'pacs007', 'Payment Cancellation Request'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'pacs028', 'Payment Status Request'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'camt054', 'Bank to Customer Debit Credit Notification'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'camt029', 'Resolution of Investigation'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'pain001', 'Customer Credit Transfer Initiation'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'pain002', 'Customer Payment Status Report'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'camt055', 'Financial Institution to Financial Institution Payment Cancellation Request'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_messages (clearing_system_id, message_type, message_description)
SELECT cs.id, 'camt056', 'Financial Institution to Financial Institution Payment Status Request'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

-- Insert supported payment types
INSERT INTO clearing_system_supported_payment_types (clearing_system_id, payment_type, payment_description)
SELECT cs.id, 'WIRE_DOMESTIC', 'Domestic Wire Transfer'
FROM clearing_systems cs WHERE cs.code IN ('FEDWIRE', 'CHAPS')
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_payment_types (clearing_system_id, payment_type, payment_description)
SELECT cs.id, 'WIRE_INTERNATIONAL', 'International Wire Transfer'
FROM clearing_systems cs WHERE cs.code IN ('FEDWIRE', 'CHAPS')
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_payment_types (clearing_system_id, payment_type, payment_description)
SELECT cs.id, 'ACH_CREDIT', 'ACH Credit Transfer'
FROM clearing_systems cs WHERE cs.code = 'ACH'
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_payment_types (clearing_system_id, payment_type, payment_description)
SELECT cs.id, 'ACH_DEBIT', 'ACH Debit Transfer'
FROM clearing_systems cs WHERE cs.code = 'ACH'
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_payment_types (clearing_system_id, payment_type, payment_description)
SELECT cs.id, 'RTP', 'Real-Time Payment'
FROM clearing_systems cs WHERE cs.code = 'RTP'
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_payment_types (clearing_system_id, payment_type, payment_description)
SELECT cs.id, 'SEPA_CREDIT', 'SEPA Credit Transfer'
FROM clearing_systems cs WHERE cs.code = 'SEPA'
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_payment_types (clearing_system_id, payment_type, payment_description)
SELECT cs.id, 'SEPA_INSTANT', 'SEPA Instant Credit Transfer'
FROM clearing_systems cs WHERE cs.code = 'SEPA'
ON CONFLICT DO NOTHING;

-- Insert supported local instruments
INSERT INTO clearing_system_supported_instruments (clearing_system_id, instrument_code, instrument_description)
SELECT cs.id, 'WIRE', 'Wire Transfer'
FROM clearing_systems cs WHERE cs.code IN ('FEDWIRE', 'CHAPS')
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_instruments (clearing_system_id, instrument_code, instrument_description)
SELECT cs.id, 'FEDWIRE', 'Fedwire Transfer'
FROM clearing_systems cs WHERE cs.code = 'FEDWIRE'
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_instruments (clearing_system_id, instrument_code, instrument_description)
SELECT cs.id, 'CHAPS', 'CHAPS Transfer'
FROM clearing_systems cs WHERE cs.code = 'CHAPS'
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_instruments (clearing_system_id, instrument_code, instrument_description)
SELECT cs.id, 'ACH', 'ACH Transfer'
FROM clearing_systems cs WHERE cs.code = 'ACH'
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_instruments (clearing_system_id, instrument_code, instrument_description)
SELECT cs.id, 'CCD', 'Corporate Credit or Debit'
FROM clearing_systems cs WHERE cs.code = 'ACH'
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_instruments (clearing_system_id, instrument_code, instrument_description)
SELECT cs.id, 'RTP', 'Real-Time Payment'
FROM clearing_systems cs WHERE cs.code = 'RTP'
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_instruments (clearing_system_id, instrument_code, instrument_description)
SELECT cs.id, 'INST', 'Instant Payment'
FROM clearing_systems cs WHERE cs.code IN ('RTP', 'SEPA')
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_supported_instruments (clearing_system_id, instrument_code, instrument_description)
SELECT cs.id, 'SEPA', 'SEPA Transfer'
FROM clearing_systems cs WHERE cs.code = 'SEPA'
ON CONFLICT DO NOTHING;

-- Insert default tenant mappings
INSERT INTO tenant_clearing_system_mappings (tenant_id, payment_type, local_instrument_code, clearing_system_code, priority, description) VALUES
('default', 'WIRE_DOMESTIC', NULL, 'FEDWIRE', 1, 'Default domestic wire routing'),
('default', 'ACH_CREDIT', NULL, 'ACH', 1, 'Default ACH credit routing'),
('default', 'RTP', NULL, 'RTP', 1, 'Default RTP routing'),
('demo-bank', 'WIRE_DOMESTIC', NULL, 'FEDWIRE', 1, 'Demo bank domestic wire routing'),
('demo-bank', 'WIRE_INTERNATIONAL', NULL, 'FEDWIRE', 1, 'Demo bank international wire routing'),
('demo-bank', 'ACH_CREDIT', NULL, 'ACH', 1, 'Demo bank ACH credit routing'),
('demo-bank', 'ACH_DEBIT', NULL, 'ACH', 1, 'Demo bank ACH debit routing'),
('demo-bank', 'RTP', NULL, 'RTP', 1, 'Demo bank RTP routing'),
('demo-bank', 'SEPA_CREDIT', NULL, 'SEPA', 1, 'Demo bank SEPA credit routing'),
('fintech-corp', 'RTP', NULL, 'RTP', 1, 'Fintech corp RTP routing'),
('fintech-corp', 'ACH_CREDIT', NULL, 'ACH', 1, 'Fintech corp ACH credit routing'),
('fintech-corp', 'WIRE_DOMESTIC', NULL, 'FEDWIRE', 1, 'Fintech corp domestic wire routing')
ON CONFLICT (tenant_id, payment_type, local_instrument_code) DO NOTHING;

-- Insert default endpoints for each clearing system
INSERT INTO clearing_system_endpoints (clearing_system_id, name, endpoint_type, message_type, url, http_method, timeout_ms, retry_attempts, authentication_type, priority, description)
SELECT cs.id, 'PACS008 Sync Endpoint', 'SYNC', 'pacs008', cs.endpoint_url || '/sync/pacs008', 'POST', cs.timeout_seconds * 1000, 3, cs.authentication_type, 1, 'Synchronous PACS008 endpoint'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_endpoints (clearing_system_id, name, endpoint_type, message_type, url, http_method, timeout_ms, retry_attempts, authentication_type, priority, description)
SELECT cs.id, 'PACS008 Async Endpoint', 'ASYNC', 'pacs008', cs.endpoint_url || '/async/pacs008', 'POST', cs.timeout_seconds * 1000, 3, cs.authentication_type, 1, 'Asynchronous PACS008 endpoint'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_endpoints (clearing_system_id, name, endpoint_type, message_type, url, http_method, timeout_ms, retry_attempts, authentication_type, priority, description)
SELECT cs.id, 'PACS002 Response Endpoint', 'WEBHOOK', 'pacs002', cs.endpoint_url || '/webhook/pacs002', 'POST', 30000, 3, cs.authentication_type, 1, 'PACS002 response webhook endpoint'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_endpoints (clearing_system_id, name, endpoint_type, message_type, url, http_method, timeout_ms, retry_attempts, authentication_type, priority, description)
SELECT cs.id, 'PACS004 Return Endpoint', 'WEBHOOK', 'pacs004', cs.endpoint_url || '/webhook/pacs004', 'POST', 30000, 3, cs.authentication_type, 1, 'PACS004 return webhook endpoint'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_endpoints (clearing_system_id, name, endpoint_type, message_type, url, http_method, timeout_ms, retry_attempts, authentication_type, priority, description)
SELECT cs.id, 'PACS007 Cancellation Endpoint', 'SYNC', 'pacs007', cs.endpoint_url || '/sync/pacs007', 'POST', cs.timeout_seconds * 1000, 3, cs.authentication_type, 1, 'PACS007 cancellation endpoint'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_endpoints (clearing_system_id, name, endpoint_type, message_type, url, http_method, timeout_ms, retry_attempts, authentication_type, priority, description)
SELECT cs.id, 'PACS028 Status Request Endpoint', 'SYNC', 'pacs028', cs.endpoint_url || '/sync/pacs028', 'POST', cs.timeout_seconds * 1000, 3, cs.authentication_type, 1, 'PACS028 status request endpoint'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_endpoints (clearing_system_id, name, endpoint_type, message_type, url, http_method, timeout_ms, retry_attempts, authentication_type, priority, description)
SELECT cs.id, 'CAMT054 Notification Endpoint', 'WEBHOOK', 'camt054', cs.endpoint_url || '/webhook/camt054', 'POST', 30000, 3, cs.authentication_type, 1, 'CAMT054 notification webhook endpoint'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_endpoints (clearing_system_id, name, endpoint_type, message_type, url, http_method, timeout_ms, retry_attempts, authentication_type, priority, description)
SELECT cs.id, 'CAMT029 Resolution Endpoint', 'WEBHOOK', 'camt029', cs.endpoint_url || '/webhook/camt029', 'POST', 30000, 3, cs.authentication_type, 1, 'CAMT029 resolution webhook endpoint'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_endpoints (clearing_system_id, name, endpoint_type, message_type, url, http_method, timeout_ms, retry_attempts, authentication_type, priority, description)
SELECT cs.id, 'Status Polling Endpoint', 'POLLING', 'status', cs.endpoint_url || '/poll/status', 'GET', 10000, 3, cs.authentication_type, 1, 'Status polling endpoint'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

-- Insert default authentication config
INSERT INTO clearing_system_authentication_config (clearing_system_id, config_key, config_value)
SELECT cs.id, 'apiKey', 'default-api-key-' || cs.code
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_authentication_config (clearing_system_id, config_key, config_value)
SELECT cs.id, 'certificate', 'certificates/' || cs.code || '.pem'
FROM clearing_systems cs
ON CONFLICT DO NOTHING;

-- Insert default headers for endpoints
INSERT INTO clearing_system_endpoint_headers (endpoint_id, header_name, header_value)
SELECT e.id, 'Content-Type', 'application/json'
FROM clearing_system_endpoints e
ON CONFLICT DO NOTHING;

INSERT INTO clearing_system_endpoint_headers (endpoint_id, header_name, header_value)
SELECT e.id, 'Accept', 'application/json'
FROM clearing_system_endpoints e
ON CONFLICT DO NOTHING;

-- Create view for clearing system summary
CREATE OR REPLACE VIEW clearing_system_summary AS
SELECT 
    cs.id,
    cs.code,
    cs.name,
    cs.description,
    cs.country_code,
    cs.currency,
    cs.is_active,
    cs.processing_mode,
    cs.timeout_seconds,
    cs.endpoint_url,
    cs.authentication_type,
    COUNT(DISTINCT t.id) as tenant_mapping_count,
    COUNT(DISTINCT e.id) as endpoint_count,
    cs.created_at,
    cs.updated_at
FROM clearing_systems cs
LEFT JOIN tenant_clearing_system_mappings t ON cs.code = t.clearing_system_code AND t.is_active = true
LEFT JOIN clearing_system_endpoints e ON cs.id = e.clearing_system_id AND e.is_active = true
GROUP BY cs.id, cs.code, cs.name, cs.description, cs.country_code, cs.currency, 
         cs.is_active, cs.processing_mode, cs.timeout_seconds, cs.endpoint_url, 
         cs.authentication_type, cs.created_at, cs.updated_at;

-- Create view for tenant clearing system routing
CREATE OR REPLACE VIEW tenant_clearing_system_routing AS
SELECT 
    t.id,
    t.tenant_id,
    t.payment_type,
    t.local_instrument_code,
    t.clearing_system_code,
    cs.name as clearing_system_name,
    cs.processing_mode,
    cs.endpoint_url,
    cs.authentication_type,
    t.priority,
    t.is_active,
    t.description,
    t.created_at,
    t.updated_at
FROM tenant_clearing_system_mappings t
JOIN clearing_systems cs ON t.clearing_system_code = cs.code
WHERE t.is_active = true AND cs.is_active = true;

COMMENT ON TABLE clearing_systems IS 'Configuration for clearing systems (schemes)';
COMMENT ON TABLE tenant_clearing_system_mappings IS 'Tenant-specific clearing system routing mappings';
COMMENT ON TABLE clearing_system_endpoints IS 'API endpoints for each clearing system';
COMMENT ON VIEW clearing_system_summary IS 'Summary view of clearing systems with counts';
COMMENT ON VIEW tenant_clearing_system_routing IS 'Tenant clearing system routing view with clearing system details';