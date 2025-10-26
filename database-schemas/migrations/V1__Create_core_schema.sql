-- Migration V1: Create core schema for Payments Engine v2
-- This migration creates the foundational database schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Core payments table with UETR correlation
CREATE TABLE payments (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  uetr UUID UNIQUE NOT NULL,
  tenant_id VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  amount DECIMAL(15,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  source_account VARCHAR(50),
  destination_account VARCHAR(50),
  payment_type VARCHAR(20),
  priority VARCHAR(20) DEFAULT 'NORMAL',
  initiated_by VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP,
  failed_at TIMESTAMP,
  failure_reason TEXT
);

-- UETR correlation table
CREATE TABLE uetr_correlation (
  payment_id UUID PRIMARY KEY REFERENCES payments(id),
  uetr UUID UNIQUE NOT NULL,
  tenant_id VARCHAR(50) NOT NULL,
  pain001_message_id VARCHAR(50),
  pacs008_message_id VARCHAR(50),
  pacs002_message_id VARCHAR(50),
  pacs004_message_id VARCHAR(50),
  camt054_message_id VARCHAR(50),
  correlation_status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ISO 20022 message details
CREATE TABLE iso20022_message_details (
  message_id VARCHAR(50) PRIMARY KEY,
  uetr UUID NOT NULL REFERENCES uetr_correlation(uetr),
  message_type VARCHAR(20) NOT NULL,
  original_message_id VARCHAR(50),
  original_transaction_id VARCHAR(50),
  instruction_id VARCHAR(50),
  transaction_id VARCHAR(50),
  status_code VARCHAR(20),
  status_reason VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment audit log
CREATE TABLE payment_audit_log (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  payment_id UUID REFERENCES payments(id),
  uetr UUID NOT NULL,
  action VARCHAR(50) NOT NULL,
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  user_id VARCHAR(50),
  iso20022_message_id VARCHAR(50),
  raw_message TEXT,
  hash VARCHAR(64)
);

-- Payment status history
CREATE TABLE payment_status_history (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  payment_id UUID REFERENCES payments(id),
  uetr UUID NOT NULL,
  status VARCHAR(20) NOT NULL,
  status_reason VARCHAR(100),
  changed_by VARCHAR(50),
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  metadata JSONB
);

-- Tenant configuration
CREATE TABLE tenant_configuration (
  tenant_id VARCHAR(50) PRIMARY KEY,
  configuration JSONB NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance indexes
CREATE INDEX idx_payments_uetr ON payments(uetr);
CREATE INDEX idx_payments_tenant_status ON payments(tenant_id, status);
CREATE INDEX idx_payments_created_at ON payments(created_at);
CREATE INDEX idx_payments_status ON payments(status) WHERE status IN ('PROCESSING', 'PENDING');

CREATE INDEX idx_uetr_correlation_uetr ON uetr_correlation(uetr);
CREATE INDEX idx_uetr_correlation_tenant ON uetr_correlation(tenant_id);
CREATE INDEX idx_uetr_correlation_status ON uetr_correlation(correlation_status);

CREATE INDEX idx_iso20022_message_details_uetr ON iso20022_message_details(uetr);
CREATE INDEX idx_iso20022_message_details_type ON iso20022_message_details(message_type);

CREATE INDEX idx_payment_audit_log_payment_id ON payment_audit_log(payment_id);
CREATE INDEX idx_payment_audit_log_uetr ON payment_audit_log(uetr);
CREATE INDEX idx_payment_audit_log_timestamp ON payment_audit_log(timestamp);

CREATE INDEX idx_payment_status_history_payment_id ON payment_status_history(payment_id);
CREATE INDEX idx_payment_status_history_uetr ON payment_status_history(uetr);
CREATE INDEX idx_payment_status_history_changed_at ON payment_status_history(changed_at);

-- Constraints
ALTER TABLE payments ADD CONSTRAINT chk_amount_positive CHECK (amount > 0);
ALTER TABLE payments ADD CONSTRAINT chk_currency_length CHECK (LENGTH(currency) = 3);
ALTER TABLE payments ADD CONSTRAINT chk_status_valid CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED'));

ALTER TABLE uetr_correlation ADD CONSTRAINT chk_correlation_status_valid 
CHECK (correlation_status IN ('ACTIVE', 'INACTIVE', 'EXPIRED'));

-- Triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_uetr_correlation_updated_at BEFORE UPDATE ON uetr_correlation
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tenant_configuration_updated_at BEFORE UPDATE ON tenant_configuration
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
