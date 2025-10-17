-- PayShap Adapter Database Schema
-- Supports instant P2P payments with proxy registry integration

-- PayShap Adapters table
CREATE TABLE payshap_adapters (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    tenant_name VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(36) NOT NULL,
    business_unit_name VARCHAR(255) NOT NULL,
    adapter_name VARCHAR(255) NOT NULL,
    network VARCHAR(50) NOT NULL DEFAULT 'PAYSHAP',
    status VARCHAR(50) NOT NULL DEFAULT 'INACTIVE',
    endpoint VARCHAR(500) NOT NULL,
    api_version VARCHAR(20) DEFAULT '1.0',
    timeout_seconds INTEGER DEFAULT 5,
    retry_attempts INTEGER DEFAULT 3,
    encryption_enabled BOOLEAN DEFAULT true,
    batch_size INTEGER DEFAULT 50,
    amount_limit DECIMAL(15,2) DEFAULT 3000.00,
    currency_code VARCHAR(3) DEFAULT 'ZAR',
    processing_window_start VARCHAR(10) DEFAULT '00:00',
    processing_window_end VARCHAR(10) DEFAULT '23:59',
    proxy_registry_endpoint VARCHAR(500),
    proxy_registry_timeout INTEGER DEFAULT 3,
    proxy_registry_retry_attempts INTEGER DEFAULT 2,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version INTEGER DEFAULT 1
);

-- PayShap Payment Messages table
CREATE TABLE payshap_payment_messages (
    id VARCHAR(36) PRIMARY KEY,
    payshap_adapter_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL DEFAULT 'pacs.008',
    direction VARCHAR(20) NOT NULL DEFAULT 'OUTBOUND',
    message_id VARCHAR(255),
    instruction_id VARCHAR(255),
    end_to_end_id VARCHAR(255),
    transaction_type VARCHAR(50) NOT NULL DEFAULT 'CREDIT',
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    debtor_name VARCHAR(255),
    debtor_account VARCHAR(255),
    debtor_bank_code VARCHAR(50),
    debtor_mobile VARCHAR(20),
    debtor_email VARCHAR(255),
    creditor_name VARCHAR(255),
    creditor_account VARCHAR(255),
    creditor_bank_code VARCHAR(50),
    creditor_mobile VARCHAR(20),
    creditor_email VARCHAR(255),
    payment_purpose VARCHAR(500),
    reference VARCHAR(255),
    proxy_type VARCHAR(50),
    proxy_value VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    response_code VARCHAR(10),
    response_message TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 1,
    FOREIGN KEY (payshap_adapter_id) REFERENCES payshap_adapters(id)
);

-- PayShap Transaction Logs table
CREATE TABLE payshap_transaction_logs (
    id VARCHAR(36) PRIMARY KEY,
    payshap_adapter_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO',
    message TEXT NOT NULL,
    details JSONB,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payshap_adapter_id) REFERENCES payshap_adapters(id)
);

-- PayShap Settlement Records table
CREATE TABLE payshap_settlement_records (
    id VARCHAR(36) PRIMARY KEY,
    payshap_adapter_id VARCHAR(36) NOT NULL,
    settlement_id VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    settlement_type VARCHAR(50) NOT NULL DEFAULT 'INSTANT',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    settlement_date DATE,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payshap_adapter_id) REFERENCES payshap_adapters(id)
);

-- PayShap Proxy Registry Cache table
CREATE TABLE payshap_proxy_cache (
    id VARCHAR(36) PRIMARY KEY,
    proxy_type VARCHAR(50) NOT NULL,
    proxy_value VARCHAR(255) NOT NULL,
    account_number VARCHAR(255),
    bank_code VARCHAR(50),
    bank_name VARCHAR(255),
    account_holder_name VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(proxy_type, proxy_value)
);

-- Indexes for performance
CREATE INDEX idx_payshap_adapters_tenant ON payshap_adapters(tenant_id, business_unit_id);
CREATE INDEX idx_payshap_adapters_status ON payshap_adapters(status);
CREATE INDEX idx_payshap_payment_messages_adapter ON payshap_payment_messages(payshap_adapter_id);
CREATE INDEX idx_payshap_payment_messages_transaction ON payshap_payment_messages(transaction_id);
CREATE INDEX idx_payshap_payment_messages_status ON payshap_payment_messages(status);
CREATE INDEX idx_payshap_payment_messages_created ON payshap_payment_messages(created_at);
CREATE INDEX idx_payshap_transaction_logs_adapter ON payshap_transaction_logs(payshap_adapter_id);
CREATE INDEX idx_payshap_transaction_logs_transaction ON payshap_transaction_logs(transaction_id);
CREATE INDEX idx_payshap_transaction_logs_occurred ON payshap_transaction_logs(occurred_at);
CREATE INDEX idx_payshap_settlement_records_adapter ON payshap_settlement_records(payshap_adapter_id);
CREATE INDEX idx_payshap_settlement_records_transaction ON payshap_settlement_records(transaction_id);
CREATE INDEX idx_payshap_settlement_records_status ON payshap_settlement_records(status);
CREATE INDEX idx_payshap_proxy_cache_proxy ON payshap_proxy_cache(proxy_type, proxy_value);
CREATE INDEX idx_payshap_proxy_cache_active ON payshap_proxy_cache(is_active, expires_at);

-- Comments for documentation
COMMENT ON TABLE payshap_adapters IS 'PayShap adapter configurations for instant P2P payments';
COMMENT ON TABLE payshap_payment_messages IS 'PayShap payment messages with proxy registry integration';
COMMENT ON TABLE payshap_transaction_logs IS 'PayShap transaction audit logs';
COMMENT ON TABLE payshap_settlement_records IS 'PayShap settlement records for instant payments';
COMMENT ON TABLE payshap_proxy_cache IS 'PayShap proxy registry cache for mobile/email lookups';
