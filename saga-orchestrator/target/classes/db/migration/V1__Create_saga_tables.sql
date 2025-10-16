-- Create saga tables for orchestration
CREATE TABLE IF NOT EXISTS sagas (
    id VARCHAR(255) PRIMARY KEY,
    saga_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    payment_id VARCHAR(255),
    saga_data JSONB,
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    compensated_at TIMESTAMP WITH TIME ZONE,
    current_step_index INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS saga_steps (
    id VARCHAR(255) PRIMARY KEY,
    saga_id VARCHAR(255) NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    step_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    sequence INTEGER NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    endpoint VARCHAR(500),
    compensation_endpoint VARCHAR(500),
    input_data JSONB,
    output_data JSONB,
    error_data JSONB,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    compensated_at TIMESTAMP WITH TIME ZONE,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (saga_id) REFERENCES sagas(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS saga_events (
    id VARCHAR(255) PRIMARY KEY,
    saga_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (saga_id) REFERENCES sagas(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_sagas_tenant_id ON sagas(tenant_id);
CREATE INDEX IF NOT EXISTS idx_sagas_correlation_id ON sagas(correlation_id);
CREATE INDEX IF NOT EXISTS idx_sagas_payment_id ON sagas(payment_id);
CREATE INDEX IF NOT EXISTS idx_sagas_status ON sagas(status);
CREATE INDEX IF NOT EXISTS idx_sagas_started_at ON sagas(started_at);

CREATE INDEX IF NOT EXISTS idx_saga_steps_saga_id ON saga_steps(saga_id);
CREATE INDEX IF NOT EXISTS idx_saga_steps_tenant_id ON saga_steps(tenant_id);
CREATE INDEX IF NOT EXISTS idx_saga_steps_correlation_id ON saga_steps(correlation_id);
CREATE INDEX IF NOT EXISTS idx_saga_steps_status ON saga_steps(status);
CREATE INDEX IF NOT EXISTS idx_saga_steps_sequence ON saga_steps(saga_id, sequence);

CREATE INDEX IF NOT EXISTS idx_saga_events_saga_id ON saga_events(saga_id);
CREATE INDEX IF NOT EXISTS idx_saga_events_tenant_id ON saga_events(tenant_id);
CREATE INDEX IF NOT EXISTS idx_saga_events_correlation_id ON saga_events(correlation_id);
CREATE INDEX IF NOT EXISTS idx_saga_events_event_type ON saga_events(event_type);
CREATE INDEX IF NOT EXISTS idx_saga_events_occurred_at ON saga_events(occurred_at);

-- Create saga templates table
CREATE TABLE IF NOT EXISTS saga_templates (
    id VARCHAR(255) PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    step_definitions JSONB NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample saga templates
INSERT INTO saga_templates (id, template_name, description, step_definitions, version, created_at) VALUES
('template-1', 'PaymentProcessingSaga', 'Standard payment processing workflow', 
 '[
   {"stepName": "Validate Payment", "stepType": "VALIDATION", "serviceName": "validation-service", "endpoint": "/api/v1/validate", "compensationEndpoint": "/api/v1/validate/compensate", "maxRetries": 3, "timeoutSeconds": 30, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Route Payment", "stepType": "ROUTING", "serviceName": "routing-service", "endpoint": "/api/v1/route", "compensationEndpoint": "/api/v1/route/compensate", "maxRetries": 3, "timeoutSeconds": 30, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Account Operations", "stepType": "ACCOUNT_ADAPTER", "serviceName": "account-adapter-service", "endpoint": "/api/v1/account/operations", "compensationEndpoint": "/api/v1/account/operations/compensate", "maxRetries": 3, "timeoutSeconds": 30, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Process Transaction", "stepType": "TRANSACTION_PROCESSING", "serviceName": "transaction-processing-service", "endpoint": "/api/v1/transactions", "compensationEndpoint": "/api/v1/transactions/compensate", "maxRetries": 3, "timeoutSeconds": 30, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Send Notification", "stepType": "NOTIFICATION", "serviceName": "notification-service", "endpoint": "/api/v1/notify", "compensationEndpoint": "/api/v1/notify/compensate", "maxRetries": 3, "timeoutSeconds": 30, "isOptional": false, "isCompensationStep": false}
 ]'::jsonb, 1, CURRENT_TIMESTAMP),
('template-2', 'FastPaymentSaga', 'Fast payment processing workflow',
 '[
   {"stepName": "Quick Validation", "stepType": "VALIDATION", "serviceName": "validation-service", "endpoint": "/api/v1/validate/quick", "compensationEndpoint": "/api/v1/validate/quick/compensate", "maxRetries": 3, "timeoutSeconds": 15, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Direct Route", "stepType": "ROUTING", "serviceName": "routing-service", "endpoint": "/api/v1/route/direct", "compensationEndpoint": "/api/v1/route/direct/compensate", "maxRetries": 3, "timeoutSeconds": 15, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Fast Transaction", "stepType": "TRANSACTION_PROCESSING", "serviceName": "transaction-processing-service", "endpoint": "/api/v1/transactions/fast", "compensationEndpoint": "/api/v1/transactions/fast/compensate", "maxRetries": 3, "timeoutSeconds": 15, "isOptional": false, "isCompensationStep": false}
 ]'::jsonb, 1, CURRENT_TIMESTAMP),
('template-3', 'HighValuePaymentSaga', 'High-value payment processing workflow with enhanced validation',
 '[
   {"stepName": "Enhanced Validation", "stepType": "VALIDATION", "serviceName": "validation-service", "endpoint": "/api/v1/validate/enhanced", "compensationEndpoint": "/api/v1/validate/enhanced/compensate", "maxRetries": 3, "timeoutSeconds": 60, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Manual Review", "stepType": "VALIDATION", "serviceName": "manual-review-service", "endpoint": "/api/v1/review", "compensationEndpoint": "/api/v1/review/compensate", "maxRetries": 1, "timeoutSeconds": 300, "isOptional": true, "isCompensationStep": false},
   {"stepName": "Route with Approval", "stepType": "ROUTING", "serviceName": "routing-service", "endpoint": "/api/v1/route/approved", "compensationEndpoint": "/api/v1/route/approved/compensate", "maxRetries": 3, "timeoutSeconds": 30, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Account Operations", "stepType": "ACCOUNT_ADAPTER", "serviceName": "account-adapter-service", "endpoint": "/api/v1/account/operations", "compensationEndpoint": "/api/v1/account/operations/compensate", "maxRetries": 3, "timeoutSeconds": 30, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Process Transaction", "stepType": "TRANSACTION_PROCESSING", "serviceName": "transaction-processing-service", "endpoint": "/api/v1/transactions", "compensationEndpoint": "/api/v1/transactions/compensate", "maxRetries": 3, "timeoutSeconds": 30, "isOptional": false, "isCompensationStep": false},
   {"stepName": "Compliance Notification", "stepType": "NOTIFICATION", "serviceName": "notification-service", "endpoint": "/api/v1/notify/compliance", "compensationEndpoint": "/api/v1/notify/compliance/compensate", "maxRetries": 3, "timeoutSeconds": 30, "isOptional": false, "isCompensationStep": false}
 ]'::jsonb, 1, CURRENT_TIMESTAMP);
