-- Database Performance Optimization Migration
-- Target: Support 2000 TPS for payment processing

-- ============================================================================
-- INDEX OPTIMIZATION
-- ============================================================================

-- Drop existing inefficient indexes
DROP INDEX IF EXISTS idx_transactions_created_at;
DROP INDEX IF EXISTS idx_transactions_status;
DROP INDEX IF EXISTS idx_transactions_tenant_id;

-- Create optimized indexes for high TPS
CREATE INDEX CONCURRENTLY idx_transactions_created_at_btree 
ON transactions(created_at) 
WHERE status IN ('PENDING', 'PROCESSING', 'COMPLETED');

-- Partial index for active transactions only
CREATE INDEX CONCURRENTLY idx_transactions_active_status 
ON transactions(status, created_at) 
WHERE status IN ('PENDING', 'PROCESSING');

-- Composite index for tenant-based queries
CREATE INDEX CONCURRENTLY idx_transactions_tenant_status_created 
ON transactions(tenant_id, status, created_at DESC);

-- Covering index for common query patterns
CREATE INDEX CONCURRENTLY idx_transactions_covering 
ON transactions(tenant_id, status, created_at) 
INCLUDE (amount, currency_code, transaction_reference);

-- Index for account-based queries
CREATE INDEX CONCURRENTLY idx_transactions_from_account 
ON transactions(from_account_id, created_at DESC) 
WHERE from_account_id IS NOT NULL;

CREATE INDEX CONCURRENTLY idx_transactions_to_account 
ON transactions(to_account_id, created_at DESC) 
WHERE to_account_id IS NOT NULL;

-- Index for payment type queries
CREATE INDEX CONCURRENTLY idx_transactions_payment_type 
ON transactions(payment_type_id, created_at DESC);

-- Index for external reference lookups
CREATE INDEX CONCURRENTLY idx_transactions_external_ref 
ON transactions(external_reference) 
WHERE external_reference IS NOT NULL;

-- ============================================================================
-- ACCOUNT TABLE OPTIMIZATION
-- ============================================================================

-- Index for account balance queries
CREATE INDEX CONCURRENTLY idx_accounts_balance_active 
ON accounts(balance) 
WHERE status = 'ACTIVE';

-- Composite index for tenant and status
CREATE INDEX CONCURRENTLY idx_accounts_tenant_status 
ON accounts(tenant_id, status);

-- Index for account number lookups
CREATE INDEX CONCURRENTLY idx_accounts_number_tenant 
ON accounts(account_number, tenant_id);

-- ============================================================================
-- PAYMENT TYPE TABLE OPTIMIZATION
-- ============================================================================

-- Index for active payment types
CREATE INDEX CONCURRENTLY idx_payment_types_active 
ON payment_types(id) 
WHERE is_active = true;

-- Index for tenant-specific payment types
CREATE INDEX CONCURRENTLY idx_payment_types_tenant_active 
ON payment_types(tenant_id, is_active);

-- ============================================================================
-- TABLE PARTITIONING (for high TPS)
-- ============================================================================

-- Create partitioned transactions table for high volume
CREATE TABLE transactions_partitioned (
    LIKE transactions INCLUDING ALL
) PARTITION BY RANGE (created_at);

-- Create monthly partitions for the next 12 months
CREATE TABLE transactions_2024_12 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2024-12-01') TO ('2025-01-01');

CREATE TABLE transactions_2025_01 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE transactions_2025_02 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

CREATE TABLE transactions_2025_03 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');

CREATE TABLE transactions_2025_04 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-04-01') TO ('2025-05-01');

CREATE TABLE transactions_2025_05 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-05-01') TO ('2025-06-01');

CREATE TABLE transactions_2025_06 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-06-01') TO ('2025-07-01');

CREATE TABLE transactions_2025_07 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-07-01') TO ('2025-08-01');

CREATE TABLE transactions_2025_08 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-08-01') TO ('2025-09-01');

CREATE TABLE transactions_2025_09 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-09-01') TO ('2025-10-01');

CREATE TABLE transactions_2025_10 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-10-01') TO ('2025-11-01');

CREATE TABLE transactions_2025_11 PARTITION OF transactions_partitioned
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');

-- Create indexes on partitioned table
CREATE INDEX CONCURRENTLY idx_transactions_partitioned_tenant_status 
ON transactions_partitioned(tenant_id, status, created_at);

CREATE INDEX CONCURRENTLY idx_transactions_partitioned_created_at 
ON transactions_partitioned(created_at);

-- ============================================================================
-- MATERIALIZED VIEWS FOR ANALYTICS
-- ============================================================================

-- Daily transaction summary
CREATE MATERIALIZED VIEW mv_daily_transaction_summary AS
SELECT 
    DATE(created_at) as transaction_date,
    tenant_id,
    status,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as average_amount,
    MIN(amount) as min_amount,
    MAX(amount) as max_amount
FROM transactions
WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(created_at), tenant_id, status;

-- Create index on materialized view
CREATE INDEX CONCURRENTLY idx_mv_daily_summary_date_tenant 
ON mv_daily_transaction_summary(transaction_date, tenant_id);

-- Refresh function for materialized view
CREATE OR REPLACE FUNCTION refresh_daily_transaction_summary()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_transaction_summary;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- PERFORMANCE OPTIMIZATION FUNCTIONS
-- ============================================================================

-- Function to get account balance with row-level locking
CREATE OR REPLACE FUNCTION get_account_balance_locked(
    p_account_id UUID
) RETURNS DECIMAL(15,2) AS $$
DECLARE
    v_balance DECIMAL(15,2);
BEGIN
    SELECT balance INTO v_balance
    FROM accounts
    WHERE id = p_account_id
    FOR UPDATE;
    
    RETURN COALESCE(v_balance, 0);
END;
$$ LANGUAGE plpgsql;

-- Function to update account balance atomically
CREATE OR REPLACE FUNCTION update_account_balance(
    p_account_id UUID,
    p_amount DECIMAL(15,2),
    p_operation VARCHAR(10) -- 'DEBIT' or 'CREDIT'
) RETURNS BOOLEAN AS $$
DECLARE
    v_current_balance DECIMAL(15,2);
    v_new_balance DECIMAL(15,2);
BEGIN
    -- Lock the account row
    SELECT balance INTO v_current_balance
    FROM accounts
    WHERE id = p_account_id
    FOR UPDATE;
    
    IF v_current_balance IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Calculate new balance
    IF p_operation = 'DEBIT' THEN
        v_new_balance := v_current_balance - p_amount;
        IF v_new_balance < 0 THEN
            RETURN FALSE; -- Insufficient funds
        END IF;
    ELSIF p_operation = 'CREDIT' THEN
        v_new_balance := v_current_balance + p_amount;
    ELSE
        RETURN FALSE; -- Invalid operation
    END IF;
    
    -- Update the balance
    UPDATE accounts 
    SET balance = v_new_balance, updated_at = CURRENT_TIMESTAMP
    WHERE id = p_account_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- STATISTICS AND MONITORING
-- ============================================================================

-- Enable query statistics
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Create view for transaction performance monitoring
CREATE OR REPLACE VIEW v_transaction_performance AS
SELECT 
    DATE_TRUNC('hour', created_at) as hour,
    tenant_id,
    status,
    COUNT(*) as transaction_count,
    AVG(EXTRACT(EPOCH FROM (completed_at - created_at))) as avg_processing_time_seconds,
    MIN(EXTRACT(EPOCH FROM (completed_at - created_at))) as min_processing_time_seconds,
    MAX(EXTRACT(EPOCH FROM (completed_at - created_at))) as max_processing_time_seconds,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY EXTRACT(EPOCH FROM (completed_at - created_at))) as p95_processing_time_seconds
FROM transactions
WHERE created_at >= CURRENT_DATE - INTERVAL '7 days'
    AND completed_at IS NOT NULL
GROUP BY DATE_TRUNC('hour', created_at), tenant_id, status
ORDER BY hour DESC, tenant_id, status;

-- ============================================================================
-- CONFIGURATION OPTIMIZATION
-- ============================================================================

-- Update table statistics for better query planning
ANALYZE transactions;
ANALYZE accounts;
ANALYZE payment_types;

-- Set table-specific configuration
ALTER TABLE transactions SET (fillfactor = 90);
ALTER TABLE accounts SET (fillfactor = 95);

-- ============================================================================
-- CLEANUP AND MAINTENANCE
-- ============================================================================

-- Create function for partition maintenance
CREATE OR REPLACE FUNCTION create_monthly_partition(
    p_table_name TEXT,
    p_start_date DATE
) RETURNS void AS $$
DECLARE
    v_partition_name TEXT;
    v_end_date DATE;
BEGIN
    v_partition_name := p_table_name || '_' || TO_CHAR(p_start_date, 'YYYY_MM');
    v_end_date := p_start_date + INTERVAL '1 month';
    
    EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
                   v_partition_name, p_table_name, p_start_date, v_end_date);
END;
$$ LANGUAGE plpgsql;

-- Create function for old partition cleanup
CREATE OR REPLACE FUNCTION cleanup_old_partitions(
    p_table_name TEXT,
    p_retention_months INTEGER DEFAULT 12
) RETURNS void AS $$
DECLARE
    v_partition_name TEXT;
    v_cutoff_date DATE;
BEGIN
    v_cutoff_date := CURRENT_DATE - (p_retention_months || ' months')::INTERVAL;
    
    FOR v_partition_name IN
        SELECT schemaname||'.'||tablename
        FROM pg_tables
        WHERE tablename LIKE p_table_name || '_%'
        AND tablename ~ '^\d{4}_\d{2}$'
        AND TO_DATE(REPLACE(SPLIT_PART(tablename, '_', 2), '_', '-') || '-01', 'YYYY-MM-DD') < v_cutoff_date
    LOOP
        EXECUTE 'DROP TABLE IF EXISTS ' || v_partition_name;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- PERFORMANCE MONITORING QUERIES
-- ============================================================================

-- Query to monitor index usage
CREATE OR REPLACE VIEW v_index_usage AS
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_tup_read,
    idx_tup_fetch,
    idx_scan,
    CASE 
        WHEN idx_scan = 0 THEN 'UNUSED'
        WHEN idx_scan < 100 THEN 'LOW_USAGE'
        ELSE 'HIGH_USAGE'
    END as usage_level
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Query to monitor table bloat
CREATE OR REPLACE VIEW v_table_bloat AS
SELECT 
    schemaname,
    tablename,
    n_tup_ins as inserts,
    n_tup_upd as updates,
    n_tup_del as deletes,
    n_live_tup as live_tuples,
    n_dead_tup as dead_tuples,
    ROUND((n_dead_tup::DECIMAL / NULLIF(n_live_tup + n_dead_tup, 0)) * 100, 2) as bloat_percentage
FROM pg_stat_user_tables
WHERE n_live_tup > 0
ORDER BY bloat_percentage DESC;

-- ============================================================================
-- FINAL OPTIMIZATIONS
-- ============================================================================

-- Update PostgreSQL configuration for high TPS
-- Note: These require postgresql.conf changes and restart
-- shared_buffers = 8GB
-- effective_cache_size = 24GB
-- work_mem = 256MB
-- maintenance_work_mem = 2GB
-- checkpoint_completion_target = 0.9
-- wal_buffers = 64MB
-- max_connections = 500
-- shared_preload_libraries = 'pg_stat_statements'

-- Create connection pool monitoring
CREATE OR REPLACE VIEW v_connection_monitoring AS
SELECT 
    state,
    COUNT(*) as connection_count,
    ROUND(AVG(EXTRACT(EPOCH FROM (now() - state_change))), 2) as avg_state_duration_seconds
FROM pg_stat_activity
WHERE datname = current_database()
GROUP BY state
ORDER BY connection_count DESC;

COMMENT ON VIEW v_connection_monitoring IS 'Monitor database connections and their states';

-- Create performance summary view
CREATE OR REPLACE VIEW v_performance_summary AS
SELECT 
    'Transactions' as metric,
    COUNT(*) as total_count,
    COUNT(*) FILTER (WHERE created_at >= CURRENT_DATE) as today_count,
    COUNT(*) FILTER (WHERE created_at >= CURRENT_DATE - INTERVAL '1 hour') as last_hour_count
FROM transactions
UNION ALL
SELECT 
    'Active Accounts' as metric,
    COUNT(*) as total_count,
    COUNT(*) FILTER (WHERE updated_at >= CURRENT_DATE) as today_count,
    COUNT(*) FILTER (WHERE updated_at >= CURRENT_DATE - INTERVAL '1 hour') as last_hour_count
FROM accounts
WHERE status = 'ACTIVE';

COMMENT ON VIEW v_performance_summary IS 'High-level performance metrics for monitoring';