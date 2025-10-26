-- Add PayShap routing rules
-- PayShap is designed for low-value, high-volume payments in South Africa

-- Insert PayShap routing rule for low-value ZAR payments
INSERT INTO routing_rules (rule_name, rule_description, tenant_id, business_unit_id, rule_type, rule_status, priority, is_active, created_at, created_by) VALUES
('PayShap Low Value Rule', 'Route low value ZAR payments to PayShap clearing system', 'tenant-1', 'business-unit-1', 'AMOUNT_CURRENCY', 'ACTIVE', 5, true, CURRENT_TIMESTAMP, 'system'),
('PayShap High Volume Rule', 'Route high volume payments to PayShap for efficiency', 'tenant-1', 'business-unit-1', 'VOLUME_BASED', 'ACTIVE', 6, true, CURRENT_TIMESTAMP, 'system');

-- Insert PayShap routing conditions
INSERT INTO routing_conditions (routing_rule_id, condition_order, field_name, operator, field_value, field_value_type, logical_operator, description) VALUES
-- Low value ZAR payments (PayShap limit is typically R3000)
(5, 1, 'amount', 'LESS_THAN_OR_EQUALS', '3000.00', 'DECIMAL', 'AND', 'Amount less than or equal to R3000'),
(5, 2, 'currency', 'EQUALS', 'ZAR', 'STRING', 'AND', 'Currency is ZAR'),
-- High volume payments (more than 100 transactions per day)
(6, 1, 'payment_count', 'GREATER_THAN', '100', 'INTEGER', 'AND', 'High volume payment processing');

-- Insert PayShap routing actions
INSERT INTO routing_actions (routing_rule_id, action_order, action_type, clearing_system, routing_priority, is_primary, description) VALUES
(5, 1, 'ROUTE_TO_CLEARING_SYSTEM', 'PAYSHAP', 1, true, 'Route to PayShap clearing system'),
(6, 1, 'ROUTE_TO_CLEARING_SYSTEM', 'PAYSHAP', 1, true, 'Route high volume payments to PayShap');

-- Add PayShap-specific indexes for performance
CREATE INDEX idx_routing_actions_payshap ON routing_actions(clearing_system) WHERE clearing_system = 'PAYSHAP';
CREATE INDEX idx_routing_conditions_amount_zar ON routing_conditions(field_name, field_value) WHERE field_name = 'amount' AND field_value_type = 'DECIMAL';
CREATE INDEX idx_routing_conditions_currency_zar ON routing_conditions(field_name, field_value) WHERE field_name = 'currency' AND field_value = 'ZAR';
