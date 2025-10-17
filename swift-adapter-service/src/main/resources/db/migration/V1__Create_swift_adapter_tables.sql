-- SWIFT Adapter Database Schema
-- Supports international payments with sanctions screening and FX conversion

-- SWIFT Adapters table
CREATE TABLE swift_adapters (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    tenant_name VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(36) NOT NULL,
    business_unit_name VARCHAR(255) NOT NULL,
    adapter_name VARCHAR(255) NOT NULL,
    network VARCHAR(50) NOT NULL DEFAULT 'SWIFT',
    status VARCHAR(50) NOT NULL DEFAULT 'INACTIVE',
    endpoint VARCHAR(500) NOT NULL,
    api_version VARCHAR(20) DEFAULT '1.0',
    timeout_seconds INTEGER DEFAULT 10,
    retry_attempts INTEGER DEFAULT 3,
    encryption_enabled BOOLEAN DEFAULT true,
    batch_size INTEGER DEFAULT 100,
    sanctions_screening_enabled BOOLEAN DEFAULT true,
    sanctions_endpoint VARCHAR(500),
    sanctions_timeout INTEGER DEFAULT 5,
    sanctions_retry_attempts INTEGER DEFAULT 2,
    fx_conversion_enabled BOOLEAN DEFAULT true,
    fx_endpoint VARCHAR(500),
    fx_timeout INTEGER DEFAULT 3,
    fx_retry_attempts INTEGER DEFAULT 2,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version INTEGER DEFAULT 1
);

-- SWIFT Payment Messages table
CREATE TABLE swift_payment_messages (
    id VARCHAR(36) PRIMARY KEY,
    swift_adapter_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL DEFAULT 'MT103',
    direction VARCHAR(20) NOT NULL DEFAULT 'OUTBOUND',
    message_id VARCHAR(255),
    instruction_id VARCHAR(255),
    end_to_end_id VARCHAR(255),
    transaction_type VARCHAR(50) NOT NULL DEFAULT 'CREDIT',
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    debtor_name VARCHAR(255),
    debtor_account VARCHAR(255),
    debtor_bank_code VARCHAR(50),
    debtor_bank_name VARCHAR(255),
    debtor_bank_country VARCHAR(3),
    debtor_bank_swift_code VARCHAR(11),
    creditor_name VARCHAR(255),
    creditor_account VARCHAR(255),
    creditor_bank_code VARCHAR(50),
    creditor_bank_name VARCHAR(255),
    creditor_bank_country VARCHAR(3),
    creditor_bank_swift_code VARCHAR(11),
    payment_purpose VARCHAR(500),
    reference VARCHAR(255),
    correspondent_bank_code VARCHAR(50),
    correspondent_bank_name VARCHAR(255),
    correspondent_bank_swift_code VARCHAR(11),
    intermediary_bank_code VARCHAR(50),
    intermediary_bank_name VARCHAR(255),
    intermediary_bank_swift_code VARCHAR(11),
    charges_bearer VARCHAR(20) DEFAULT 'OUR',
    exchange_rate DECIMAL(10,6),
    original_amount DECIMAL(15,2),
    original_currency VARCHAR(3),
    sanctions_screening_status VARCHAR(50) DEFAULT 'PENDING',
    sanctions_screening_result VARCHAR(50),
    sanctions_screening_details TEXT,
    fx_conversion_status VARCHAR(50) DEFAULT 'PENDING',
    fx_conversion_result VARCHAR(50),
    fx_conversion_rate DECIMAL(10,6),
    fx_conversion_amount DECIMAL(15,2),
    fx_conversion_currency VARCHAR(3),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    response_code VARCHAR(10),
    response_message TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 1,
    FOREIGN KEY (swift_adapter_id) REFERENCES swift_adapters(id)
);

-- SWIFT Transaction Logs table
CREATE TABLE swift_transaction_logs (
    id VARCHAR(36) PRIMARY KEY,
    swift_adapter_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO',
    message TEXT NOT NULL,
    details JSONB,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (swift_adapter_id) REFERENCES swift_adapters(id)
);

-- SWIFT Settlement Records table
CREATE TABLE swift_settlement_records (
    id VARCHAR(36) PRIMARY KEY,
    swift_adapter_id VARCHAR(36) NOT NULL,
    settlement_id VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    settlement_type VARCHAR(50) NOT NULL DEFAULT 'T+2',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    settlement_date DATE,
    correspondent_bank VARCHAR(255),
    nostro_account VARCHAR(255),
    vostro_account VARCHAR(255),
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (swift_adapter_id) REFERENCES swift_adapters(id)
);

-- SWIFT Sanctions Screening Results table
CREATE TABLE swift_sanctions_screening_results (
    id VARCHAR(36) PRIMARY KEY,
    swift_adapter_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    screening_type VARCHAR(50) NOT NULL DEFAULT 'SANCTIONS',
    entity_name VARCHAR(255),
    entity_type VARCHAR(50),
    country_code VARCHAR(3),
    screening_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    screening_result VARCHAR(50),
    risk_score INTEGER,
    risk_level VARCHAR(20),
    screening_details TEXT,
    screened_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (swift_adapter_id) REFERENCES swift_adapters(id)
);

-- SWIFT FX Conversion Records table
CREATE TABLE swift_fx_conversion_records (
    id VARCHAR(36) PRIMARY KEY,
    swift_adapter_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    from_currency VARCHAR(3) NOT NULL,
    to_currency VARCHAR(3) NOT NULL,
    from_amount DECIMAL(15,2) NOT NULL,
    to_amount DECIMAL(15,2) NOT NULL,
    exchange_rate DECIMAL(10,6) NOT NULL,
    conversion_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    conversion_result VARCHAR(50),
    conversion_details TEXT,
    converted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (swift_adapter_id) REFERENCES swift_adapters(id)
);

-- Indexes for performance
CREATE INDEX idx_swift_adapters_tenant ON swift_adapters(tenant_id, business_unit_id);
CREATE INDEX idx_swift_adapters_status ON swift_adapters(status);
CREATE INDEX idx_swift_payment_messages_adapter ON swift_payment_messages(swift_adapter_id);
CREATE INDEX idx_swift_payment_messages_transaction ON swift_payment_messages(transaction_id);
CREATE INDEX idx_swift_payment_messages_status ON swift_payment_messages(status);
CREATE INDEX idx_swift_payment_messages_created ON swift_payment_messages(created_at);
CREATE INDEX idx_swift_payment_messages_sanctions ON swift_payment_messages(sanctions_screening_status);
CREATE INDEX idx_swift_payment_messages_fx ON swift_payment_messages(fx_conversion_status);
CREATE INDEX idx_swift_transaction_logs_adapter ON swift_transaction_logs(swift_adapter_id);
CREATE INDEX idx_swift_transaction_logs_transaction ON swift_transaction_logs(transaction_id);
CREATE INDEX idx_swift_transaction_logs_occurred ON swift_transaction_logs(occurred_at);
CREATE INDEX idx_swift_settlement_records_adapter ON swift_settlement_records(swift_adapter_id);
CREATE INDEX idx_swift_settlement_records_transaction ON swift_settlement_records(transaction_id);
CREATE INDEX idx_swift_settlement_records_status ON swift_settlement_records(status);
CREATE INDEX idx_swift_sanctions_screening_adapter ON swift_sanctions_screening_results(swift_adapter_id);
CREATE INDEX idx_swift_sanctions_screening_transaction ON swift_sanctions_screening_results(transaction_id);
CREATE INDEX idx_swift_sanctions_screening_status ON swift_sanctions_screening_results(screening_status);
CREATE INDEX idx_swift_fx_conversion_adapter ON swift_fx_conversion_records(swift_adapter_id);
CREATE INDEX idx_swift_fx_conversion_transaction ON swift_fx_conversion_records(transaction_id);
CREATE INDEX idx_swift_fx_conversion_status ON swift_fx_conversion_records(conversion_status);

-- Comments for documentation
COMMENT ON TABLE swift_adapters IS 'SWIFT adapter configurations for international payments';
COMMENT ON TABLE swift_payment_messages IS 'SWIFT payment messages with sanctions screening and FX conversion';
COMMENT ON TABLE swift_transaction_logs IS 'SWIFT transaction audit logs';
COMMENT ON TABLE swift_settlement_records IS 'SWIFT settlement records for international payments';
COMMENT ON TABLE swift_sanctions_screening_results IS 'SWIFT sanctions screening results for compliance';
COMMENT ON TABLE swift_fx_conversion_records IS 'SWIFT foreign exchange conversion records';
