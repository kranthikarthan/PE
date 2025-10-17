-- SAMOS Adapter Service Database Schema
-- High-value RTGS payment clearing adapter for South African Reserve Bank

-- Create SAMOS adapter configuration table
CREATE TABLE samos_adapters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    business_unit_id UUID,
    adapter_name VARCHAR(255) NOT NULL,
    network VARCHAR(50) NOT NULL DEFAULT 'SAMOS',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    endpoint VARCHAR(500) NOT NULL,
    api_version VARCHAR(20) NOT NULL DEFAULT 'v1',
    timeout_seconds INTEGER NOT NULL DEFAULT 30,
    retry_attempts INTEGER NOT NULL DEFAULT 3,
    encryption_enabled BOOLEAN NOT NULL DEFAULT true,
    certificate_path VARCHAR(500),
    certificate_password VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    
    CONSTRAINT uk_samos_adapters_tenant_name UNIQUE (tenant_id, adapter_name)
);

-- Create SAMOS payment messages table
CREATE TABLE samos_payment_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    payment_id VARCHAR(255) NOT NULL,
    message_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL, -- pacs.008, pacs.002, camt.054
    direction VARCHAR(20) NOT NULL, -- INBOUND, OUTBOUND
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, RECEIVED, FAILED
    iso20022_payload TEXT NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    response_code VARCHAR(10),
    response_message TEXT,
    sent_at TIMESTAMP,
    received_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_samos_payment_messages_message_id UNIQUE (message_id)
);

-- Create SAMOS transaction logs table
CREATE TABLE samos_transaction_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    payment_id VARCHAR(255) NOT NULL,
    message_id VARCHAR(255) NOT NULL,
    operation VARCHAR(50) NOT NULL, -- SUBMIT, QUERY, CANCEL
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, TIMEOUT
    request_payload TEXT,
    response_payload TEXT,
    error_code VARCHAR(10),
    error_message TEXT,
    processing_time_ms INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_samos_transaction_logs_payment 
        FOREIGN KEY (payment_id) REFERENCES samos_payment_messages(payment_id)
);

-- Create SAMOS settlement records table
CREATE TABLE samos_settlement_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    settlement_id VARCHAR(255) NOT NULL,
    payment_id VARCHAR(255) NOT NULL,
    nostro_account VARCHAR(50) NOT NULL,
    vostro_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    settlement_date DATE NOT NULL,
    settlement_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, SETTLED, FAILED
    reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_samos_settlement_records_settlement_id UNIQUE (settlement_id)
);

-- Create indexes for performance
CREATE INDEX idx_samos_adapters_tenant_id ON samos_adapters(tenant_id);
CREATE INDEX idx_samos_adapters_status ON samos_adapters(status);

CREATE INDEX idx_samos_payment_messages_tenant_id ON samos_payment_messages(tenant_id);
CREATE INDEX idx_samos_payment_messages_payment_id ON samos_payment_messages(payment_id);
CREATE INDEX idx_samos_payment_messages_message_id ON samos_payment_messages(message_id);
CREATE INDEX idx_samos_payment_messages_status ON samos_payment_messages(status);
CREATE INDEX idx_samos_payment_messages_created_at ON samos_payment_messages(created_at);

CREATE INDEX idx_samos_transaction_logs_tenant_id ON samos_transaction_logs(tenant_id);
CREATE INDEX idx_samos_transaction_logs_transaction_id ON samos_transaction_logs(transaction_id);
CREATE INDEX idx_samos_transaction_logs_payment_id ON samos_transaction_logs(payment_id);
CREATE INDEX idx_samos_transaction_logs_status ON samos_transaction_logs(status);
CREATE INDEX idx_samos_transaction_logs_created_at ON samos_transaction_logs(created_at);

CREATE INDEX idx_samos_settlement_records_tenant_id ON samos_settlement_records(tenant_id);
CREATE INDEX idx_samos_settlement_records_settlement_id ON samos_settlement_records(settlement_id);
CREATE INDEX idx_samos_settlement_records_payment_id ON samos_settlement_records(payment_id);
CREATE INDEX idx_samos_settlement_records_settlement_date ON samos_settlement_records(settlement_date);
CREATE INDEX idx_samos_settlement_records_status ON samos_settlement_records(status);

-- Enable Row Level Security (RLS) for multi-tenancy
ALTER TABLE samos_adapters ENABLE ROW LEVEL SECURITY;
ALTER TABLE samos_payment_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE samos_transaction_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE samos_settlement_records ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
CREATE POLICY samos_adapters_tenant_isolation ON samos_adapters
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);

CREATE POLICY samos_payment_messages_tenant_isolation ON samos_payment_messages
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);

CREATE POLICY samos_transaction_logs_tenant_isolation ON samos_transaction_logs
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);

CREATE POLICY samos_settlement_records_tenant_isolation ON samos_settlement_records
    USING (tenant_id = current_setting('app.current_tenant_id')::uuid);

-- Create audit trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create audit triggers
CREATE TRIGGER update_samos_adapters_updated_at 
    BEFORE UPDATE ON samos_adapters 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_samos_payment_messages_updated_at 
    BEFORE UPDATE ON samos_payment_messages 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_samos_settlement_records_updated_at 
    BEFORE UPDATE ON samos_settlement_records 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
