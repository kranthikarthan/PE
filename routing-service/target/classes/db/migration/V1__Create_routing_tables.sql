-- Create routing rules table
CREATE TABLE routing_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    rule_description VARCHAR(500),
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    rule_type VARCHAR(50) NOT NULL,
    rule_status VARCHAR(50) NOT NULL,
    priority INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0
);

-- Create routing conditions table
CREATE TABLE routing_conditions (
    id BIGSERIAL PRIMARY KEY,
    routing_rule_id BIGINT NOT NULL,
    condition_order INTEGER NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    operator VARCHAR(50) NOT NULL,
    field_value VARCHAR(500),
    field_value_type VARCHAR(50),
    logical_operator VARCHAR(10),
    is_negated BOOLEAN DEFAULT false,
    description VARCHAR(200),
    FOREIGN KEY (routing_rule_id) REFERENCES routing_rules(id) ON DELETE CASCADE
);

-- Create routing actions table
CREATE TABLE routing_actions (
    id BIGSERIAL PRIMARY KEY,
    routing_rule_id BIGINT NOT NULL,
    action_order INTEGER NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    clearing_system VARCHAR(50),
    routing_priority INTEGER,
    action_parameters VARCHAR(1000),
    is_primary BOOLEAN DEFAULT false,
    description VARCHAR(200),
    FOREIGN KEY (routing_rule_id) REFERENCES routing_rules(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_routing_rules_tenant_id ON routing_rules(tenant_id);
CREATE INDEX idx_routing_rules_tenant_business_unit ON routing_rules(tenant_id, business_unit_id);
CREATE INDEX idx_routing_rules_status ON routing_rules(rule_status);
CREATE INDEX idx_routing_rules_type ON routing_rules(rule_type);
CREATE INDEX idx_routing_rules_active ON routing_rules(is_active);
CREATE INDEX idx_routing_rules_effective ON routing_rules(effective_from, effective_to);
CREATE INDEX idx_routing_rules_priority ON routing_rules(priority);

CREATE INDEX idx_routing_conditions_rule_id ON routing_conditions(routing_rule_id);
CREATE INDEX idx_routing_conditions_field_name ON routing_conditions(field_name);
CREATE INDEX idx_routing_conditions_order ON routing_conditions(routing_rule_id, condition_order);

CREATE INDEX idx_routing_actions_rule_id ON routing_actions(routing_rule_id);
CREATE INDEX idx_routing_actions_type ON routing_actions(action_type);
CREATE INDEX idx_routing_actions_clearing_system ON routing_actions(clearing_system);
CREATE INDEX idx_routing_actions_primary ON routing_actions(is_primary);
CREATE INDEX idx_routing_actions_order ON routing_actions(routing_rule_id, action_order);
CREATE INDEX idx_routing_actions_priority ON routing_actions(routing_rule_id, routing_priority);

-- Insert sample routing rules
INSERT INTO routing_rules (rule_name, rule_description, tenant_id, business_unit_id, rule_type, rule_status, priority, is_active, created_at, created_by) VALUES
('High Value Payment Rule', 'Route high value payments to primary clearing system', 'tenant-1', 'business-unit-1', 'AMOUNT_RANGE', 'ACTIVE', 1, true, CURRENT_TIMESTAMP, 'system'),
('Low Value Payment Rule', 'Route low value payments to secondary clearing system', 'tenant-1', 'business-unit-1', 'AMOUNT_RANGE', 'ACTIVE', 2, true, CURRENT_TIMESTAMP, 'system'),
('ZAR Currency Rule', 'Route ZAR payments to local clearing system', 'tenant-1', 'business-unit-1', 'CURRENCY', 'ACTIVE', 3, true, CURRENT_TIMESTAMP, 'system'),
('USD Currency Rule', 'Route USD payments to international clearing system', 'tenant-1', 'business-unit-1', 'CURRENCY', 'ACTIVE', 4, true, CURRENT_TIMESTAMP, 'system');

-- Insert sample routing conditions
INSERT INTO routing_conditions (routing_rule_id, condition_order, field_name, operator, field_value, field_value_type, logical_operator, description) VALUES
(1, 1, 'amount', 'GREATER_THAN_OR_EQUALS', '10000.00', 'DECIMAL', 'AND', 'Amount greater than or equal to 10000'),
(2, 1, 'amount', 'LESS_THAN', '10000.00', 'DECIMAL', 'AND', 'Amount less than 10000'),
(3, 1, 'currency', 'EQUALS', 'ZAR', 'STRING', 'AND', 'Currency is ZAR'),
(4, 1, 'currency', 'EQUALS', 'USD', 'STRING', 'AND', 'Currency is USD');

-- Insert sample routing actions
INSERT INTO routing_actions (routing_rule_id, action_order, action_type, clearing_system, routing_priority, is_primary, description) VALUES
(1, 1, 'ROUTE_TO_CLEARING_SYSTEM', 'PRIMARY_CLEARING', 1, true, 'Route to primary clearing system'),
(2, 1, 'ROUTE_TO_CLEARING_SYSTEM', 'SECONDARY_CLEARING', 2, true, 'Route to secondary clearing system'),
(3, 1, 'ROUTE_TO_CLEARING_SYSTEM', 'LOCAL_CLEARING', 1, true, 'Route to local clearing system'),
(4, 1, 'ROUTE_TO_CLEARING_SYSTEM', 'INTERNATIONAL_CLEARING', 1, true, 'Route to international clearing system');
