-- Migration: Add Core Banking Configuration Support
-- Description: Creates tables and indexes for core banking configuration management

-- Create core_banking_configurations table
CREATE TABLE IF NOT EXISTS payment_engine.core_banking_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    adapter_type VARCHAR(20) NOT NULL CHECK (adapter_type IN ('REST', 'GRPC', 'INTERNAL')),
    base_url VARCHAR(500),
    grpc_host VARCHAR(255),
    grpc_port INTEGER,
    authentication_method VARCHAR(50),
    api_key VARCHAR(500),
    username VARCHAR(100),
    password VARCHAR(500),
    certificate_path VARCHAR(500),
    processing_mode VARCHAR(20) DEFAULT 'SYNC',
    message_format VARCHAR(20) DEFAULT 'JSON',
    timeout_ms INTEGER DEFAULT 30000,
    retry_attempts INTEGER DEFAULT 3,
    priority INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    bank_code VARCHAR(20),
    bank_name VARCHAR(255),
    additional_config JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create indexes for core_banking_configurations
CREATE INDEX IF NOT EXISTS idx_core_banking_config_tenant_id ON payment_engine.core_banking_configurations(tenant_id);
CREATE INDEX IF NOT EXISTS idx_core_banking_config_adapter_type ON payment_engine.core_banking_configurations(adapter_type);
CREATE INDEX IF NOT EXISTS idx_core_banking_config_active ON payment_engine.core_banking_configurations(is_active);
CREATE INDEX IF NOT EXISTS idx_core_banking_config_bank_code ON payment_engine.core_banking_configurations(bank_code);
CREATE INDEX IF NOT EXISTS idx_core_banking_config_priority ON payment_engine.core_banking_configurations(priority);
CREATE UNIQUE INDEX IF NOT EXISTS idx_core_banking_config_tenant_unique ON payment_engine.core_banking_configurations(tenant_id) WHERE is_active = true;

-- Create clearing_system_configurations table
CREATE TABLE IF NOT EXISTS payment_engine.clearing_system_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clearing_system_code VARCHAR(50) NOT NULL,
    clearing_system_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    endpoint_url VARCHAR(500),
    authentication_method VARCHAR(50),
    api_key VARCHAR(500),
    username VARCHAR(100),
    password VARCHAR(500),
    certificate_path VARCHAR(500),
    processing_mode VARCHAR(20) DEFAULT 'ASYNC',
    message_format VARCHAR(20) DEFAULT 'XML',
    timeout_ms INTEGER DEFAULT 30000,
    retry_attempts INTEGER DEFAULT 3,
    priority INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    supported_payment_types TEXT[],
    supported_currencies TEXT[],
    local_instrumentation_codes TEXT[],
    additional_config JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create indexes for clearing_system_configurations
CREATE INDEX IF NOT EXISTS idx_clearing_system_config_code ON payment_engine.clearing_system_configurations(clearing_system_code);
CREATE INDEX IF NOT EXISTS idx_clearing_system_config_tenant_id ON payment_engine.clearing_system_configurations(tenant_id);
CREATE INDEX IF NOT EXISTS idx_clearing_system_config_active ON payment_engine.clearing_system_configurations(is_active);
CREATE INDEX IF NOT EXISTS idx_clearing_system_config_priority ON payment_engine.clearing_system_configurations(priority);
CREATE UNIQUE INDEX IF NOT EXISTS idx_clearing_system_config_tenant_code_unique ON payment_engine.clearing_system_configurations(clearing_system_code, tenant_id) WHERE is_active = true;

-- Create payment_routing_rules table
CREATE TABLE IF NOT EXISTS payment_engine.payment_routing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    local_instrumentation_code VARCHAR(50) NOT NULL,
    from_bank_code VARCHAR(20),
    to_bank_code VARCHAR(20),
    routing_type VARCHAR(20) NOT NULL CHECK (routing_type IN ('SAME_BANK', 'OTHER_BANK', 'INCOMING_CLEARING', 'EXTERNAL_SYSTEM')),
    clearing_system_code VARCHAR(50),
    processing_mode VARCHAR(20) DEFAULT 'SYNC',
    message_format VARCHAR(20) DEFAULT 'JSON',
    priority INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    conditions JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create indexes for payment_routing_rules
CREATE INDEX IF NOT EXISTS idx_payment_routing_tenant_id ON payment_engine.payment_routing_rules(tenant_id);
CREATE INDEX IF NOT EXISTS idx_payment_routing_payment_type ON payment_engine.payment_routing_rules(payment_type);
CREATE INDEX IF NOT EXISTS idx_payment_routing_local_instrument ON payment_engine.payment_routing_rules(local_instrumentation_code);
CREATE INDEX IF NOT EXISTS idx_payment_routing_type ON payment_engine.payment_routing_rules(routing_type);
CREATE INDEX IF NOT EXISTS idx_payment_routing_active ON payment_engine.payment_routing_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_payment_routing_priority ON payment_engine.payment_routing_rules(priority);

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_core_banking_configurations_updated_at 
    BEFORE UPDATE ON payment_engine.core_banking_configurations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_clearing_system_configurations_updated_at 
    BEFORE UPDATE ON payment_engine.clearing_system_configurations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_routing_rules_updated_at 
    BEFORE UPDATE ON payment_engine.payment_routing_rules 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample core banking configurations
INSERT INTO payment_engine.core_banking_configurations (
    tenant_id, adapter_type, base_url, authentication_method, 
    processing_mode, message_format, bank_code, bank_name, 
    created_by, updated_by
) VALUES 
(
    'tenant1', 'REST', 'http://localhost:8081', 'API_KEY',
    'SYNC', 'JSON', 'BANK001', 'Sample Bank 1',
    'system', 'system'
),
(
    'tenant2', 'GRPC', NULL, 'CERTIFICATE',
    'ASYNC', 'JSON', 'BANK002', 'Sample Bank 2',
    'system', 'system'
);

-- Insert sample clearing system configurations
INSERT INTO payment_engine.clearing_system_configurations (
    clearing_system_code, clearing_system_name, tenant_id, endpoint_url,
    authentication_method, processing_mode, message_format,
    supported_payment_types, supported_currencies, local_instrumentation_codes,
    created_by, updated_by
) VALUES 
(
    'CLEARING_001', 'Sample Clearing System 1', 'tenant1', 'https://clearing1.example.com/api',
    'API_KEY', 'ASYNC', 'XML',
    ARRAY['TRANSFER', 'PAYMENT'], ARRAY['USD', 'EUR'], ARRAY['LOCAL_INSTR_001', 'LOCAL_INSTR_002'],
    'system', 'system'
),
(
    'CLEARING_002', 'Sample Clearing System 2', 'tenant2', 'https://clearing2.example.com/api',
    'CERTIFICATE', 'SYNC', 'JSON',
    ARRAY['TRANSFER', 'PAYMENT', 'BULK'], ARRAY['USD', 'EUR', 'GBP'], ARRAY['LOCAL_INSTR_003'],
    'system', 'system'
);

-- Insert sample payment routing rules
INSERT INTO payment_engine.payment_routing_rules (
    tenant_id, payment_type, local_instrumentation_code, routing_type,
    processing_mode, message_format, created_by, updated_by
) VALUES 
(
    'tenant1', 'TRANSFER', 'LOCAL_INSTR_001', 'SAME_BANK',
    'SYNC', 'JSON', 'system', 'system'
),
(
    'tenant1', 'PAYMENT', 'LOCAL_INSTR_002', 'OTHER_BANK',
    'ASYNC', 'XML', 'system', 'system'
),
(
    'tenant2', 'TRANSFER', 'LOCAL_INSTR_003', 'OTHER_BANK',
    'ASYNC', 'JSON', 'system', 'system'
);

-- Add comments
COMMENT ON TABLE payment_engine.core_banking_configurations IS 'Configuration for core banking system integration per tenant';
COMMENT ON TABLE payment_engine.clearing_system_configurations IS 'Configuration for clearing system integration per tenant';
COMMENT ON TABLE payment_engine.payment_routing_rules IS 'Rules for payment routing based on tenant, payment type, and local instrumentation code';

COMMENT ON COLUMN payment_engine.core_banking_configurations.adapter_type IS 'Type of core banking adapter: REST, GRPC, or INTERNAL';
COMMENT ON COLUMN payment_engine.core_banking_configurations.processing_mode IS 'Processing mode: SYNC, ASYNC, or BATCH';
COMMENT ON COLUMN payment_engine.core_banking_configurations.message_format IS 'Message format: JSON or XML';
COMMENT ON COLUMN payment_engine.core_banking_configurations.additional_config IS 'Additional configuration parameters in JSON format';

COMMENT ON COLUMN payment_engine.clearing_system_configurations.supported_payment_types IS 'Array of supported payment types';
COMMENT ON COLUMN payment_engine.clearing_system_configurations.supported_currencies IS 'Array of supported currencies';
COMMENT ON COLUMN payment_engine.clearing_system_configurations.local_instrumentation_codes IS 'Array of supported local instrumentation codes';

COMMENT ON COLUMN payment_engine.payment_routing_rules.routing_type IS 'Type of routing: SAME_BANK, OTHER_BANK, INCOMING_CLEARING, or EXTERNAL_SYSTEM';
COMMENT ON COLUMN payment_engine.payment_routing_rules.conditions IS 'Additional routing conditions in JSON format';