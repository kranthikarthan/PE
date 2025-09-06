-- Payment Engine Seed Data
-- Initial data for development and testing

SET search_path TO payment_engine, public;

-- ============================================================================
-- SEED DATA
-- ============================================================================

-- Account Types
INSERT INTO account_types (id, code, name, description) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'CHECKING', 'Checking Account', 'Standard checking account for daily transactions'),
    ('550e8400-e29b-41d4-a716-446655440002', 'SAVINGS', 'Savings Account', 'High-yield savings account'),
    ('550e8400-e29b-41d4-a716-446655440003', 'BUSINESS', 'Business Account', 'Business banking account'),
    ('550e8400-e29b-41d4-a716-446655440004', 'JOINT', 'Joint Account', 'Joint account for multiple holders'),
    ('550e8400-e29b-41d4-a716-446655440005', 'ESCROW', 'Escrow Account', 'Escrow account for secure transactions');

-- Payment Types
INSERT INTO payment_types (id, code, name, description, is_synchronous, max_amount, min_amount, processing_fee, configuration) VALUES
    ('660e8400-e29b-41d4-a716-446655440001', 'ACH_CREDIT', 'ACH Credit Transfer', 'Automated Clearing House credit transfer', false, 1000000.00, 0.01, 2.50, '{"processing_time_hours": 24, "cutoff_time": "15:00"}'),
    ('660e8400-e29b-41d4-a716-446655440002', 'ACH_DEBIT', 'ACH Debit Transfer', 'Automated Clearing House debit transfer', false, 1000000.00, 0.01, 1.50, '{"processing_time_hours": 24, "cutoff_time": "15:00"}'),
    ('660e8400-e29b-41d4-a716-446655440003', 'WIRE_DOMESTIC', 'Domestic Wire Transfer', 'Same-day domestic wire transfer', true, 10000000.00, 1.00, 25.00, '{"processing_time_minutes": 30}'),
    ('660e8400-e29b-41d4-a716-446655440004', 'WIRE_INTERNATIONAL', 'International Wire Transfer', 'International wire transfer', false, 5000000.00, 1.00, 45.00, '{"processing_time_hours": 48, "requires_compliance_check": true}'),
    ('660e8400-e29b-41d4-a716-446655440005', 'RTP', 'Real-Time Payment', 'Instant real-time payment', true, 100000.00, 0.01, 0.25, '{"processing_time_seconds": 10}'),
    ('660e8400-e29b-41d4-a716-446655440006', 'ZELLE', 'Zelle Transfer', 'Zelle person-to-person transfer', true, 5000.00, 1.00, 0.00, '{"processing_time_seconds": 30}'),
    ('660e8400-e29b-41d4-a716-446655440007', 'CHECK', 'Check Payment', 'Traditional check payment', false, 50000.00, 0.01, 0.50, '{"processing_time_days": 3}'),
    ('660e8400-e29b-41d4-a716-446655440008', 'CARD_DEBIT', 'Debit Card Payment', 'Debit card transaction', true, 10000.00, 0.01, 0.35, '{"processing_time_seconds": 5}'),
    ('660e8400-e29b-41d4-a716-446655440009', 'MOBILE_WALLET', 'Mobile Wallet Payment', 'Mobile wallet payment (Apple Pay, Google Pay)', true, 2500.00, 0.01, 0.15, '{"processing_time_seconds": 3}');

-- Sample Customers
INSERT INTO customers (id, customer_number, first_name, last_name, email, phone, date_of_birth, status, kyc_status) VALUES
    ('770e8400-e29b-41d4-a716-446655440001', 'CUST001', 'John', 'Doe', 'john.doe@email.com', '+1-555-0101', '1985-03-15', 'ACTIVE', 'VERIFIED'),
    ('770e8400-e29b-41d4-a716-446655440002', 'CUST002', 'Jane', 'Smith', 'jane.smith@email.com', '+1-555-0102', '1990-07-22', 'ACTIVE', 'VERIFIED'),
    ('770e8400-e29b-41d4-a716-446655440003', 'CUST003', 'Robert', 'Johnson', 'robert.johnson@email.com', '+1-555-0103', '1978-11-08', 'ACTIVE', 'VERIFIED'),
    ('770e8400-e29b-41d4-a716-446655440004', 'CUST004', 'Emily', 'Davis', 'emily.davis@email.com', '+1-555-0104', '1992-01-30', 'ACTIVE', 'PENDING'),
    ('770e8400-e29b-41d4-a716-446655440005', 'CUST005', 'Michael', 'Wilson', 'michael.wilson@email.com', '+1-555-0105', '1987-09-12', 'ACTIVE', 'VERIFIED');

-- Sample Accounts
INSERT INTO accounts (id, account_number, customer_id, account_type_id, currency_code, balance, available_balance, status) VALUES
    ('880e8400-e29b-41d4-a716-446655440001', 'ACC001001', '770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'USD', 15000.00, 15000.00, 'ACTIVE'),
    ('880e8400-e29b-41d4-a716-446655440002', 'ACC001002', '770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 'USD', 25000.00, 25000.00, 'ACTIVE'),
    ('880e8400-e29b-41d4-a716-446655440003', 'ACC002001', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'USD', 8500.00, 8500.00, 'ACTIVE'),
    ('880e8400-e29b-41d4-a716-446655440004', 'ACC002002', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'USD', 42000.00, 42000.00, 'ACTIVE'),
    ('880e8400-e29b-41d4-a716-446655440005', 'ACC003001', '770e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'USD', 75000.00, 75000.00, 'ACTIVE'),
    ('880e8400-e29b-41d4-a716-446655440006', 'ACC004001', '770e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', 'USD', 3200.00, 3200.00, 'ACTIVE'),
    ('880e8400-e29b-41d4-a716-446655440007', 'ACC005001', '770e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440001', 'USD', 12800.00, 12800.00, 'ACTIVE');

-- System Users
INSERT INTO users (id, username, email, password_hash, first_name, last_name, is_active) VALUES
    ('990e8400-e29b-41d4-a716-446655440001', 'admin', 'admin@paymentengine.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO4UR.G7Hxm', 'System', 'Administrator', true),
    ('990e8400-e29b-41d4-a716-446655440002', 'operator', 'operator@paymentengine.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO4UR.G7Hxm', 'Payment', 'Operator', true),
    ('990e8400-e29b-41d4-a716-446655440003', 'readonly', 'readonly@paymentengine.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO4UR.G7Hxm', 'Read Only', 'User', true),
    ('990e8400-e29b-41d4-a716-446655440004', 'service', 'service@paymentengine.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO4UR.G7Hxm', 'Service', 'Account', true);

-- Roles
INSERT INTO roles (id, name, description, permissions, is_active) VALUES
    ('aa0e8400-e29b-41d4-a716-446655440001', 'ADMIN', 'System Administrator', '{"permissions": ["*"]}', true),
    ('aa0e8400-e29b-41d4-a716-446655440002', 'OPERATOR', 'Payment Operator', '{"permissions": ["payment:create", "payment:read", "payment:update", "account:read", "transaction:read", "transaction:create"]}', true),
    ('aa0e8400-e29b-41d4-a716-446655440003', 'READONLY', 'Read Only User', '{"permissions": ["payment:read", "account:read", "transaction:read", "customer:read"]}', true),
    ('aa0e8400-e29b-41d4-a716-446655440004', 'SERVICE', 'Service Account', '{"permissions": ["payment:create", "payment:read", "transaction:create", "transaction:read", "account:read"]}', true);

-- User Role Assignments
INSERT INTO user_roles (user_id, role_id, granted_by) VALUES
    ('990e8400-e29b-41d4-a716-446655440001', 'aa0e8400-e29b-41d4-a716-446655440001', '990e8400-e29b-41d4-a716-446655440001'),
    ('990e8400-e29b-41d4-a716-446655440002', 'aa0e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440001'),
    ('990e8400-e29b-41d4-a716-446655440003', 'aa0e8400-e29b-41d4-a716-446655440003', '990e8400-e29b-41d4-a716-446655440001'),
    ('990e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440004', '990e8400-e29b-41d4-a716-446655440001');

-- ============================================================================
-- CONFIGURATION DATA
-- ============================================================================

-- System Configuration
INSERT INTO config.system_config (config_key, config_value, description, environment) VALUES
    ('payment.default_currency', '"USD"', 'Default currency for payments', 'production'),
    ('payment.max_daily_limit', '50000.00', 'Maximum daily transaction limit per account', 'production'),
    ('payment.fraud_detection_enabled', 'true', 'Enable fraud detection for transactions', 'production'),
    ('payment.notification_enabled', 'true', 'Enable transaction notifications', 'production'),
    ('security.jwt_expiry_minutes', '60', 'JWT token expiry time in minutes', 'production'),
    ('security.max_login_attempts', '5', 'Maximum failed login attempts before lockout', 'production'),
    ('security.lockout_duration_minutes', '30', 'Account lockout duration in minutes', 'production'),
    ('kafka.batch_size', '16384', 'Kafka producer batch size', 'production'),
    ('kafka.linger_ms', '5', 'Kafka producer linger time', 'production'),
    ('kafka.retry_attempts', '3', 'Kafka producer retry attempts', 'production');

-- Kafka Topics Configuration
INSERT INTO config.kafka_topics (topic_name, partitions, replication_factor, configuration, is_active) VALUES
    ('payment.transaction.created', 3, 1, '{"retention.ms": 604800000, "cleanup.policy": "delete"}', true),
    ('payment.transaction.updated', 3, 1, '{"retention.ms": 604800000, "cleanup.policy": "delete"}', true),
    ('payment.transaction.completed', 3, 1, '{"retention.ms": 2592000000, "cleanup.policy": "delete"}', true),
    ('payment.transaction.failed', 3, 1, '{"retention.ms": 2592000000, "cleanup.policy": "delete"}', true),
    ('payment.account.balance.updated', 3, 1, '{"retention.ms": 604800000, "cleanup.policy": "delete"}', true),
    ('payment.notification.email', 1, 1, '{"retention.ms": 86400000, "cleanup.policy": "delete"}', true),
    ('payment.notification.sms', 1, 1, '{"retention.ms": 86400000, "cleanup.policy": "delete"}', true),
    ('payment.audit.log', 6, 1, '{"retention.ms": 7776000000, "cleanup.policy": "delete"}', true),
    ('payment.error.log', 3, 1, '{"retention.ms": 2592000000, "cleanup.policy": "delete"}', true),
    ('payment.metrics', 1, 1, '{"retention.ms": 604800000, "cleanup.policy": "delete"}', true);

-- API Endpoints Configuration
INSERT INTO config.api_endpoints (endpoint_path, http_method, service_name, rate_limit_per_minute, requires_auth, configuration) VALUES
    ('/api/v1/payments', 'POST', 'core-banking', 100, true, '{"timeout_seconds": 30, "retry_attempts": 3}'),
    ('/api/v1/payments/{id}', 'GET', 'core-banking', 1000, true, '{"timeout_seconds": 5, "cache_ttl_seconds": 300}'),
    ('/api/v1/payments/{id}/status', 'GET', 'core-banking', 2000, true, '{"timeout_seconds": 5, "cache_ttl_seconds": 60}'),
    ('/api/v1/accounts', 'GET', 'core-banking', 500, true, '{"timeout_seconds": 10, "cache_ttl_seconds": 600}'),
    ('/api/v1/accounts/{id}', 'GET', 'core-banking', 1000, true, '{"timeout_seconds": 5, "cache_ttl_seconds": 300}'),
    ('/api/v1/accounts/{id}/balance', 'GET', 'core-banking', 2000, true, '{"timeout_seconds": 5, "cache_ttl_seconds": 30}'),
    ('/api/v1/accounts/{id}/transactions', 'GET', 'core-banking', 500, true, '{"timeout_seconds": 10, "cache_ttl_seconds": 120}'),
    ('/api/v1/payment-types', 'GET', 'core-banking', 1000, true, '{"timeout_seconds": 5, "cache_ttl_seconds": 3600}'),
    ('/api/v1/customers', 'GET', 'core-banking', 200, true, '{"timeout_seconds": 10, "cache_ttl_seconds": 600}'),
    ('/api/v1/customers/{id}', 'GET', 'core-banking', 1000, true, '{"timeout_seconds": 5, "cache_ttl_seconds": 300}'),
    ('/api/v1/auth/login', 'POST', 'middleware', 10, false, '{"timeout_seconds": 10, "rate_limit_per_ip": 5}'),
    ('/api/v1/auth/refresh', 'POST', 'middleware', 50, true, '{"timeout_seconds": 5}'),
    ('/api/v1/health', 'GET', 'api-gateway', 10000, false, '{"timeout_seconds": 2}'),
    ('/api/v1/metrics', 'GET', 'api-gateway', 100, true, '{"timeout_seconds": 10}');

-- Sample Transactions for Testing
INSERT INTO transactions (id, transaction_reference, external_reference, from_account_id, to_account_id, payment_type_id, amount, fee_amount, status, transaction_type, description, metadata) VALUES
    ('bb0e8400-e29b-41d4-a716-446655440001', 'TXN-2024-001', 'EXT-REF-001', '880e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440005', 500.00, 0.25, 'COMPLETED', 'TRANSFER', 'RTP transfer to Jane Smith', '{"channel": "mobile", "device_id": "device123"}'),
    ('bb0e8400-e29b-41d4-a716-446655440002', 'TXN-2024-002', 'EXT-REF-002', '880e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440005', '660e8400-e29b-41d4-a716-446655440001', 2500.00, 2.50, 'PROCESSING', 'TRANSFER', 'ACH credit to business account', '{"channel": "web", "ip_address": "192.168.1.100"}'),
    ('bb0e8400-e29b-41d4-a716-446655440003', 'TXN-2024-003', 'EXT-REF-003', '880e8400-e29b-41d4-a716-446655440007', NULL, '660e8400-e29b-41d4-a716-446655440003', 10000.00, 25.00, 'PENDING', 'PAYMENT', 'Wire transfer payment', '{"channel": "branch", "teller_id": "T001"}');

-- Sample Transaction Events
INSERT INTO transaction_events (transaction_id, event_type, event_data, created_by) VALUES
    ('bb0e8400-e29b-41d4-a716-446655440001', 'TRANSACTION_CREATED', '{"status": "PENDING", "amount": 500.00, "created_at": "2024-01-15T10:30:00Z"}', 'system'),
    ('bb0e8400-e29b-41d4-a716-446655440001', 'TRANSACTION_VALIDATED', '{"validation_result": "PASSED", "checks": ["balance", "limits", "fraud"]}', 'system'),
    ('bb0e8400-e29b-41d4-a716-446655440001', 'TRANSACTION_PROCESSING', '{"status": "PROCESSING", "processor": "RTP_PROCESSOR", "started_at": "2024-01-15T10:30:05Z"}', 'system'),
    ('bb0e8400-e29b-41d4-a716-446655440001', 'TRANSACTION_COMPLETED', '{"status": "COMPLETED", "completed_at": "2024-01-15T10:30:15Z", "confirmation_code": "RTP123456"}', 'system');

-- API Keys for Testing
INSERT INTO api_keys (key_name, key_hash, user_id, scopes, expires_at) VALUES
    ('test-api-key', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO4UR.G7Hxm', '990e8400-e29b-41d4-a716-446655440004', '["payment:create", "payment:read", "account:read"]', '2025-12-31 23:59:59+00'),
    ('mobile-app-key', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO4UR.G7Hxm', '990e8400-e29b-41d4-a716-446655440002', '["payment:create", "payment:read", "account:read", "customer:read"]', '2025-12-31 23:59:59+00');

-- ============================================================================
-- VIEWS FOR COMMON QUERIES
-- ============================================================================

-- Account Summary View
CREATE VIEW account_summary AS
SELECT 
    a.id,
    a.account_number,
    c.customer_number,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    at.name AS account_type,
    a.currency_code,
    a.balance,
    a.available_balance,
    a.status,
    a.opened_date,
    COUNT(t.id) AS transaction_count,
    COALESCE(SUM(CASE WHEN t.status = 'COMPLETED' THEN t.amount ELSE 0 END), 0) AS total_transaction_amount
FROM accounts a
JOIN customers c ON a.customer_id = c.id
JOIN account_types at ON a.account_type_id = at.id
LEFT JOIN transactions t ON (a.id = t.from_account_id OR a.id = t.to_account_id)
GROUP BY a.id, a.account_number, c.customer_number, c.first_name, c.last_name, 
         at.name, a.currency_code, a.balance, a.available_balance, a.status, a.opened_date;

-- Transaction Summary View
CREATE VIEW transaction_summary AS
SELECT 
    t.id,
    t.transaction_reference,
    t.external_reference,
    fa.account_number AS from_account,
    ta.account_number AS to_account,
    CONCAT(fc.first_name, ' ', fc.last_name) AS from_customer,
    CONCAT(tc.first_name, ' ', tc.last_name) AS to_customer,
    pt.name AS payment_type,
    t.amount,
    t.fee_amount,
    t.currency_code,
    t.status,
    t.transaction_type,
    t.description,
    t.initiated_at,
    t.completed_at,
    EXTRACT(EPOCH FROM (COALESCE(t.completed_at, CURRENT_TIMESTAMP) - t.initiated_at)) AS processing_time_seconds
FROM transactions t
LEFT JOIN accounts fa ON t.from_account_id = fa.id
LEFT JOIN accounts ta ON t.to_account_id = ta.id
LEFT JOIN customers fc ON fa.customer_id = fc.id
LEFT JOIN customers tc ON ta.customer_id = tc.id
JOIN payment_types pt ON t.payment_type_id = pt.id;