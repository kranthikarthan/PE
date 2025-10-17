-- BankservAfrica Adapter Database Schema
-- Supports EFT batch processing, ISO 8583, and ACH integration

-- BankservAfrica Adapters table
CREATE TABLE bankservafrica_adapters (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    adapter_name VARCHAR(255) NOT NULL,
    network VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    api_version VARCHAR(20) NOT NULL,
    timeout_seconds INTEGER NOT NULL,
    retry_attempts INTEGER NOT NULL,
    encryption_enabled BOOLEAN NOT NULL,
    batch_size INTEGER NOT NULL,
    processing_window_start VARCHAR(10),
    processing_window_end VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    
    CONSTRAINT uk_bankservafrica_adapters_tenant_name UNIQUE (tenant_id, adapter_name)
);

-- BankservAfrica EFT Batch Messages table
CREATE TABLE bankservafrica_eft_messages (
    id UUID PRIMARY KEY,
    bankservafrica_adapter_id UUID NOT NULL,
    batch_id VARCHAR(255) NOT NULL,
    message_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    payload TEXT NOT NULL,
    payload_hash VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    status_code INTEGER,
    error_code VARCHAR(50),
    error_message TEXT,
    processing_started_at TIMESTAMP WITH TIME ZONE,
    processing_completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    CONSTRAINT fk_bankservafrica_eft_messages_adapter 
        FOREIGN KEY (bankservafrica_adapter_id) 
        REFERENCES bankservafrica_adapters(id) ON DELETE CASCADE,
    
    CONSTRAINT uk_bankservafrica_eft_messages_batch_message 
        UNIQUE (batch_id, message_id)
);

-- BankservAfrica ISO 8583 Messages table
CREATE TABLE bankservafrica_iso8583_messages (
    id UUID PRIMARY KEY,
    bankservafrica_adapter_id UUID NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(10) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    mti VARCHAR(4) NOT NULL,
    processing_code VARCHAR(6) NOT NULL,
    amount DECIMAL(15,2),
    currency_code VARCHAR(3),
    card_number VARCHAR(19),
    merchant_id VARCHAR(15),
    terminal_id VARCHAR(8),
    response_code VARCHAR(2),
    status VARCHAR(50) NOT NULL,
    raw_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    CONSTRAINT fk_bankservafrica_iso8583_messages_adapter 
        FOREIGN KEY (bankservafrica_adapter_id) 
        REFERENCES bankservafrica_adapters(id) ON DELETE CASCADE,
    
    CONSTRAINT uk_bankservafrica_iso8583_messages_transaction 
        UNIQUE (transaction_id)
);

-- BankservAfrica ACH Transactions table
CREATE TABLE bankservafrica_ach_transactions (
    id UUID PRIMARY KEY,
    bankservafrica_adapter_id UUID NOT NULL,
    ach_batch_id VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    originator_id VARCHAR(15) NOT NULL,
    originator_name VARCHAR(255) NOT NULL,
    receiver_id VARCHAR(15) NOT NULL,
    receiver_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(17) NOT NULL,
    routing_number VARCHAR(9) NOT NULL,
    trace_number VARCHAR(15) NOT NULL,
    status VARCHAR(50) NOT NULL,
    return_code VARCHAR(3),
    return_reason VARCHAR(255),
    settlement_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    CONSTRAINT fk_bankservafrica_ach_transactions_adapter 
        FOREIGN KEY (bankservafrica_adapter_id) 
        REFERENCES bankservafrica_adapters(id) ON DELETE CASCADE,
    
    CONSTRAINT uk_bankservafrica_ach_transactions_trace 
        UNIQUE (trace_number)
);

-- BankservAfrica Transaction Logs table
CREATE TABLE bankservafrica_transaction_logs (
    id UUID PRIMARY KEY,
    bankservafrica_adapter_id UUID NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    status_code INTEGER,
    error_code VARCHAR(50),
    error_message TEXT,
    processing_time_ms INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    CONSTRAINT fk_bankservafrica_transaction_logs_adapter 
        FOREIGN KEY (bankservafrica_adapter_id) 
        REFERENCES bankservafrica_adapters(id) ON DELETE CASCADE
);

-- BankservAfrica Settlement Records table
CREATE TABLE bankservafrica_settlement_records (
    id UUID PRIMARY KEY,
    bankservafrica_adapter_id UUID NOT NULL,
    settlement_date DATE NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    transaction_count INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    settlement_reference VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    
    CONSTRAINT fk_bankservafrica_settlement_records_adapter 
        FOREIGN KEY (bankservafrica_adapter_id) 
        REFERENCES bankservafrica_adapters(id) ON DELETE CASCADE,
    
    CONSTRAINT uk_bankservafrica_settlement_records_date 
        UNIQUE (bankservafrica_adapter_id, settlement_date)
);

-- Create indexes for performance
CREATE INDEX idx_bankservafrica_adapters_tenant ON bankservafrica_adapters(tenant_id);
CREATE INDEX idx_bankservafrica_adapters_status ON bankservafrica_adapters(status);

CREATE INDEX idx_bankservafrica_eft_messages_adapter ON bankservafrica_eft_messages(bankservafrica_adapter_id);
CREATE INDEX idx_bankservafrica_eft_messages_batch ON bankservafrica_eft_messages(batch_id);
CREATE INDEX idx_bankservafrica_eft_messages_status ON bankservafrica_eft_messages(status);
CREATE INDEX idx_bankservafrica_eft_messages_created ON bankservafrica_eft_messages(created_at);

CREATE INDEX idx_bankservafrica_iso8583_messages_adapter ON bankservafrica_iso8583_messages(bankservafrica_adapter_id);
CREATE INDEX idx_bankservafrica_iso8583_messages_transaction ON bankservafrica_iso8583_messages(transaction_id);
CREATE INDEX idx_bankservafrica_iso8583_messages_status ON bankservafrica_iso8583_messages(status);
CREATE INDEX idx_bankservafrica_iso8583_messages_created ON bankservafrica_iso8583_messages(created_at);

CREATE INDEX idx_bankservafrica_ach_transactions_adapter ON bankservafrica_ach_transactions(bankservafrica_adapter_id);
CREATE INDEX idx_bankservafrica_ach_transactions_batch ON bankservafrica_ach_transactions(ach_batch_id);
CREATE INDEX idx_bankservafrica_ach_transactions_status ON bankservafrica_ach_transactions(status);
CREATE INDEX idx_bankservafrica_ach_transactions_settlement ON bankservafrica_ach_transactions(settlement_date);

CREATE INDEX idx_bankservafrica_transaction_logs_adapter ON bankservafrica_transaction_logs(bankservafrica_adapter_id);
CREATE INDEX idx_bankservafrica_transaction_logs_transaction ON bankservafrica_transaction_logs(transaction_id);
CREATE INDEX idx_bankservafrica_transaction_logs_created ON bankservafrica_transaction_logs(created_at);

CREATE INDEX idx_bankservafrica_settlement_records_adapter ON bankservafrica_settlement_records(bankservafrica_adapter_id);
CREATE INDEX idx_bankservafrica_settlement_records_date ON bankservafrica_settlement_records(settlement_date);
CREATE INDEX idx_bankservafrica_settlement_records_status ON bankservafrica_settlement_records(status);

-- Enable Row Level Security (RLS) for multi-tenant support
ALTER TABLE bankservafrica_adapters ENABLE ROW LEVEL SECURITY;
ALTER TABLE bankservafrica_eft_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE bankservafrica_iso8583_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE bankservafrica_ach_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE bankservafrica_transaction_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE bankservafrica_settlement_records ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
CREATE POLICY bankservafrica_adapters_tenant_isolation ON bankservafrica_adapters
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY bankservafrica_eft_messages_tenant_isolation ON bankservafrica_eft_messages
    USING (bankservafrica_adapter_id IN (
        SELECT id FROM bankservafrica_adapters 
        WHERE tenant_id = current_setting('app.current_tenant_id')
    ));

CREATE POLICY bankservafrica_iso8583_messages_tenant_isolation ON bankservafrica_iso8583_messages
    USING (bankservafrica_adapter_id IN (
        SELECT id FROM bankservafrica_adapters 
        WHERE tenant_id = current_setting('app.current_tenant_id')
    ));

CREATE POLICY bankservafrica_ach_transactions_tenant_isolation ON bankservafrica_ach_transactions
    USING (bankservafrica_adapter_id IN (
        SELECT id FROM bankservafrica_adapters 
        WHERE tenant_id = current_setting('app.current_tenant_id')
    ));

CREATE POLICY bankservafrica_transaction_logs_tenant_isolation ON bankservafrica_transaction_logs
    USING (bankservafrica_adapter_id IN (
        SELECT id FROM bankservafrica_adapters 
        WHERE tenant_id = current_setting('app.current_tenant_id')
    ));

CREATE POLICY bankservafrica_settlement_records_tenant_isolation ON bankservafrica_settlement_records
    USING (bankservafrica_adapter_id IN (
        SELECT id FROM bankservafrica_adapters 
        WHERE tenant_id = current_setting('app.current_tenant_id')
    ));

-- Create audit triggers for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_bankservafrica_adapters_updated_at 
    BEFORE UPDATE ON bankservafrica_adapters 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bankservafrica_eft_messages_updated_at 
    BEFORE UPDATE ON bankservafrica_eft_messages 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bankservafrica_iso8583_messages_updated_at 
    BEFORE UPDATE ON bankservafrica_iso8583_messages 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bankservafrica_ach_transactions_updated_at 
    BEFORE UPDATE ON bankservafrica_ach_transactions 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bankservafrica_settlement_records_updated_at 
    BEFORE UPDATE ON bankservafrica_settlement_records 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
