-- Cassandra Schema for ISO 20022 Message Storage
-- Optimized for high-volume message processing with UETR correlation

-- Create keyspace for ISO 20022 messages
CREATE KEYSPACE IF NOT EXISTS iso20022_messages 
WITH REPLICATION = {
  'class': 'NetworkTopologyStrategy',
  'datacenter1': 3,
  'datacenter2': 3
};

-- Use the keyspace
USE iso20022_messages;

-- pain.001 messages table
CREATE TABLE IF NOT EXISTS pain001_messages (
  uetr UUID,
  message_id TEXT,
  tenant_id TEXT,
  payment_id UUID,
  raw_xml TEXT,
  parsed_json TEXT,
  validation_status TEXT,
  processing_status TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC)
  AND COMPACTION = {
    'class': 'TimeWindowCompactionStrategy',
    'compaction_window_size': '1',
    'compaction_window_unit': 'DAYS'
  };

-- pacs.008 messages table
CREATE TABLE IF NOT EXISTS pacs008_messages (
  uetr UUID,
  message_id TEXT,
  tenant_id TEXT,
  payment_id UUID,
  original_pain001_id TEXT,
  raw_xml TEXT,
  parsed_json TEXT,
  status TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- pacs.002 messages table
CREATE TABLE IF NOT EXISTS pacs002_messages (
  uetr UUID,
  message_id TEXT,
  tenant_id TEXT,
  payment_id UUID,
  original_message_id TEXT,
  original_transaction_id TEXT,
  status_code TEXT,
  status_reason TEXT,
  raw_xml TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- pacs.004 messages table (Payment Returns)
CREATE TABLE IF NOT EXISTS pacs004_messages (
  uetr UUID,
  message_id TEXT,
  tenant_id TEXT,
  payment_id UUID,
  original_message_id TEXT,
  return_reason TEXT,
  raw_xml TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- camt.054 messages table (Bank Notifications)
CREATE TABLE IF NOT EXISTS camt054_messages (
  uetr UUID,
  message_id TEXT,
  tenant_id TEXT,
  payment_id UUID,
  notification_type TEXT,
  raw_xml TEXT,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

-- UETR correlation table
CREATE TABLE IF NOT EXISTS uetr_correlation (
  uetr UUID PRIMARY KEY,
  payment_id UUID,
  tenant_id TEXT,
  pain001_id TEXT,
  pacs008_id TEXT,
  pacs002_id TEXT,
  pacs004_id TEXT,
  camt054_id TEXT,
  correlation_status TEXT,
  created_at TIMESTAMP
) WITH DEFAULT TTL = 2592000; -- 30 days

-- Message processing status
CREATE TABLE IF NOT EXISTS message_processing_status (
  uetr UUID,
  message_id TEXT,
  processing_status TEXT,
  error_message TEXT,
  retry_count INT,
  last_processed_at TIMESTAMP,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH DEFAULT TTL = 604800; -- 7 days

-- Message validation results
CREATE TABLE IF NOT EXISTS message_validation_results (
  uetr UUID,
  message_id TEXT,
  validation_status TEXT,
  validation_errors TEXT,
  validation_warnings TEXT,
  validated_at TIMESTAMP,
  created_at TIMESTAMP,
  PRIMARY KEY (uetr, message_id)
) WITH DEFAULT TTL = 2592000; -- 30 days

-- Materialized views for common queries

-- Messages by tenant
CREATE MATERIALIZED VIEW IF NOT EXISTS messages_by_tenant AS
SELECT uetr, message_id, message_type, tenant_id, payment_id, created_at
FROM pain001_messages
WHERE tenant_id IS NOT NULL
PRIMARY KEY (tenant_id, created_at, uetr, message_id)
WITH CLUSTERING ORDER BY (created_at DESC);

-- Messages by status
CREATE MATERIALIZED VIEW IF NOT EXISTS messages_by_status AS
SELECT uetr, message_id, processing_status, created_at
FROM pain001_messages
WHERE processing_status IS NOT NULL
PRIMARY KEY (processing_status, created_at, uetr, message_id)
WITH CLUSTERING ORDER BY (created_at DESC);

-- UETR correlation by tenant
CREATE MATERIALIZED VIEW IF NOT EXISTS uetr_correlation_by_tenant AS
SELECT uetr, tenant_id, payment_id, correlation_status, created_at
FROM uetr_correlation
WHERE tenant_id IS NOT NULL
PRIMARY KEY (tenant_id, created_at, uetr)
WITH CLUSTERING ORDER BY (created_at DESC);

-- Message processing status by tenant
CREATE MATERIALIZED VIEW IF NOT EXISTS processing_status_by_tenant AS
SELECT uetr, message_id, processing_status, tenant_id, created_at
FROM message_processing_status
WHERE tenant_id IS NOT NULL
PRIMARY KEY (tenant_id, created_at, uetr, message_id)
WITH CLUSTERING ORDER BY (created_at DESC);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_pain001_tenant ON pain001_messages(tenant_id);
CREATE INDEX IF NOT EXISTS idx_pain001_status ON pain001_messages(processing_status);
CREATE INDEX IF NOT EXISTS idx_pacs008_tenant ON pacs008_messages(tenant_id);
CREATE INDEX IF NOT EXISTS idx_pacs008_status ON pacs008_messages(status);
CREATE INDEX IF NOT EXISTS idx_pacs002_tenant ON pacs002_messages(tenant_id);
CREATE INDEX IF NOT EXISTS idx_pacs002_status ON pacs002_messages(status_code);

-- Comments for documentation
COMMENT ON KEYSPACE iso20022_messages IS 'ISO 20022 message storage with UETR correlation for high-volume processing';
COMMENT ON TABLE pain001_messages IS 'pain.001 Payment Initiation messages with UETR correlation';
COMMENT ON TABLE pacs008_messages IS 'pacs.008 FI to FI Credit Transfer messages with UETR correlation';
COMMENT ON TABLE pacs002_messages IS 'pacs.002 Payment Status Report messages with UETR correlation';
COMMENT ON TABLE pacs004_messages IS 'pacs.004 Payment Return messages with UETR correlation';
COMMENT ON TABLE camt054_messages IS 'camt.054 Bank to Customer Notification messages with UETR correlation';
COMMENT ON TABLE uetr_correlation IS 'UETR-based message correlation for ISO 20022 messages';
COMMENT ON TABLE message_processing_status IS 'Message processing status and error tracking';
COMMENT ON TABLE message_validation_results IS 'Message validation results and error tracking';
