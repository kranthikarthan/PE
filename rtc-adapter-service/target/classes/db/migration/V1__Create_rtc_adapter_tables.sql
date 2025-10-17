-- RTC Adapter Database Schema
-- Real-Time Clearing for instant low-value payments

-- RTC Adapter Configuration
CREATE TABLE rtc_adapters (
    id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50) NOT NULL,
    adapter_name VARCHAR(100) NOT NULL,
    network VARCHAR(20) NOT NULL DEFAULT 'RTC',
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    endpoint VARCHAR(255) NOT NULL,
    api_version VARCHAR(20) DEFAULT '1.0',
    timeout_seconds INTEGER DEFAULT 10,
    retry_attempts INTEGER DEFAULT 3,
    encryption_enabled BOOLEAN DEFAULT true,
    batch_size INTEGER DEFAULT 100,
    processing_window_start VARCHAR(10),
    processing_window_end VARCHAR(10),
    amount_limit DECIMAL(18,2) DEFAULT 5000.00,
    currency_code VARCHAR(3) DEFAULT 'ZAR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    
    CONSTRAINT chk_rtc_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')),
    CONSTRAINT chk_rtc_network CHECK (network = 'RTC'),
    CONSTRAINT chk_rtc_amount_limit CHECK (amount_limit > 0),
    CONSTRAINT chk_rtc_currency CHECK (currency_code = 'ZAR')
);

-- RTC Payment Messages
CREATE TABLE rtc_payment_messages (
    id VARCHAR(50) PRIMARY KEY,
    rtc_adapter_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    message_id VARCHAR(100) NOT NULL,
    instruction_id VARCHAR(100) NOT NULL,
    end_to_end_id VARCHAR(100) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    debtor_name VARCHAR(255),
    debtor_account VARCHAR(50),
    debtor_bank_code VARCHAR(20),
    creditor_name VARCHAR(255),
    creditor_account VARCHAR(50),
    creditor_bank_code VARCHAR(20),
    payment_purpose VARCHAR(255),
    reference VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    response_code VARCHAR(10),
    response_message TEXT,
    submitted_at TIMESTAMP,
    processed_at TIMESTAMP,
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rtc_payment_adapter FOREIGN KEY (rtc_adapter_id) REFERENCES rtc_adapters(id),
    CONSTRAINT chk_rtc_payment_direction CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    CONSTRAINT chk_rtc_payment_status CHECK (status IN ('PENDING', 'SUBMITTED', 'PROCESSING', 'COMPLETED', 'FAILED', 'REJECTED')),
    CONSTRAINT chk_rtc_payment_amount CHECK (amount > 0),
    CONSTRAINT chk_rtc_payment_currency CHECK (currency = 'ZAR')
);

-- RTC Transaction Logs
CREATE TABLE rtc_transaction_logs (
    id VARCHAR(50) PRIMARY KEY,
    rtc_adapter_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processing_time_ms INTEGER,
    error_code VARCHAR(20),
    error_message TEXT,
    request_payload TEXT,
    response_payload TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rtc_log_adapter FOREIGN KEY (rtc_adapter_id) REFERENCES rtc_adapters(id),
    CONSTRAINT chk_rtc_log_status CHECK (status IN ('SUCCESS', 'FAILED', 'TIMEOUT', 'ERROR'))
);

-- RTC Settlement Records
CREATE TABLE rtc_settlement_records (
    id VARCHAR(50) PRIMARY KEY,
    rtc_adapter_id VARCHAR(50) NOT NULL,
    settlement_date DATE NOT NULL,
    settlement_reference VARCHAR(100) NOT NULL,
    total_transactions INTEGER DEFAULT 0,
    total_amount DECIMAL(18,2) DEFAULT 0.00,
    successful_transactions INTEGER DEFAULT 0,
    failed_transactions INTEGER DEFAULT 0,
    settlement_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    settlement_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rtc_settlement_adapter FOREIGN KEY (rtc_adapter_id) REFERENCES rtc_adapters(id),
    CONSTRAINT chk_rtc_settlement_status CHECK (settlement_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_rtc_settlement_amount CHECK (total_amount >= 0)
);

-- Indexes for Performance
CREATE INDEX idx_rtc_adapters_tenant ON rtc_adapters(tenant_id, business_unit_id);
CREATE INDEX idx_rtc_adapters_status ON rtc_adapters(status);
CREATE INDEX idx_rtc_payment_adapter ON rtc_payment_messages(rtc_adapter_id);
CREATE INDEX idx_rtc_payment_transaction ON rtc_payment_messages(transaction_id);
CREATE INDEX idx_rtc_payment_status ON rtc_payment_messages(status);
CREATE INDEX idx_rtc_payment_created ON rtc_payment_messages(created_at);
CREATE INDEX idx_rtc_log_adapter ON rtc_transaction_logs(rtc_adapter_id);
CREATE INDEX idx_rtc_log_transaction ON rtc_transaction_logs(transaction_id);
CREATE INDEX idx_rtc_log_occurred ON rtc_transaction_logs(occurred_at);
CREATE INDEX idx_rtc_settlement_adapter ON rtc_settlement_records(rtc_adapter_id);
CREATE INDEX idx_rtc_settlement_date ON rtc_settlement_records(settlement_date);

-- Comments
COMMENT ON TABLE rtc_adapters IS 'RTC Adapter configurations for real-time clearing';
COMMENT ON TABLE rtc_payment_messages IS 'RTC payment messages for instant low-value payments';
COMMENT ON TABLE rtc_transaction_logs IS 'RTC transaction operation logs';
COMMENT ON TABLE rtc_settlement_records IS 'RTC daily settlement records';
