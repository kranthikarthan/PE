-- Migration execution script
-- This script runs all migration files in order

-- Enable uuid extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create config schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS config;

-- Grant permissions
GRANT USAGE ON SCHEMA config TO payment_engine_role;
GRANT CREATE ON SCHEMA config TO payment_engine_role;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA config TO payment_engine_role;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA config TO payment_engine_role;

-- Run ISO 20022 migration
\i /docker-entrypoint-initdb.d/migrations/002-add-iso20022-support.sql

-- Run tenancy and configurability migration
\i /docker-entrypoint-initdb.d/migrations/003-add-tenancy-and-configurability.sql

-- Update existing data to have default tenant
UPDATE payment_engine.customers SET tenant_id = 'default' WHERE tenant_id IS NULL;
UPDATE payment_engine.accounts SET tenant_id = 'default' WHERE tenant_id IS NULL;
UPDATE payment_engine.transactions SET tenant_id = 'default' WHERE tenant_id IS NULL;
UPDATE payment_engine.users SET tenant_id = 'default' WHERE tenant_id IS NULL;

-- Refresh materialized views if any exist
-- REFRESH MATERIALIZED VIEW IF EXISTS some_view;

-- Create indexes for performance
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_tenant_created 
    ON payment_engine.customers(tenant_id, created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_accounts_tenant_status 
    ON payment_engine.accounts(tenant_id, status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_tenant_date 
    ON payment_engine.transactions(tenant_id, created_at);

-- Update table statistics
ANALYZE payment_engine.customers;
ANALYZE payment_engine.accounts;
ANALYZE payment_engine.transactions;
ANALYZE config.tenants;
ANALYZE config.tenant_configurations;

-- Log migration completion
INSERT INTO config.configuration_history 
(config_type, config_key, new_value, changed_by, change_source, rollback_possible)
VALUES 
('MIGRATION', 'tenancy_and_configurability', 
 '{"version": "003", "timestamp": "' || CURRENT_TIMESTAMP || '", "status": "completed"}',
 'system', 'MIGRATION', false);

-- Success message
DO $$
BEGIN
    RAISE NOTICE 'Multi-tenancy and configurability migration completed successfully!';
    RAISE NOTICE 'Added % tenant configuration tables', (
        SELECT count(*) 
        FROM information_schema.tables 
        WHERE table_schema = 'config' 
        AND table_name LIKE '%tenant%' OR table_name LIKE '%config%'
    );
END $$;