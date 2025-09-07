-- Migration for Tenant Configuration Tables
-- This migration creates tables for tenant configuration versioning and cloning

-- Create tenant_configuration_versions table
CREATE TABLE tenant_configuration_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    version VARCHAR(20) NOT NULL,
    name VARCHAR(100),
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    environment VARCHAR(50) NOT NULL,
    source_tenant_id VARCHAR(50),
    source_version VARCHAR(20),
    cloned_by VARCHAR(100),
    cloned_at TIMESTAMP,
    change_log TEXT,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT uk_tenant_config_tenant_version UNIQUE (tenant_id, version),
    CONSTRAINT chk_tenant_config_environment CHECK (environment IN ('DEVELOPMENT', 'INTEGRATION', 'USER_ACCEPTANCE', 'PRODUCTION'))
);

-- Create indexes for tenant_configuration_versions
CREATE INDEX idx_tenant_config_tenant_id ON tenant_configuration_versions(tenant_id);
CREATE INDEX idx_tenant_config_version ON tenant_configuration_versions(version);
CREATE INDEX idx_tenant_config_active ON tenant_configuration_versions(is_active);
CREATE INDEX idx_tenant_config_created_at ON tenant_configuration_versions(created_at);
CREATE INDEX idx_tenant_config_environment ON tenant_configuration_versions(environment);
CREATE INDEX idx_tenant_config_source_tenant ON tenant_configuration_versions(source_tenant_id);

-- Create tenant_configuration_data table for storing configuration key-value pairs
CREATE TABLE tenant_configuration_data (
    tenant_configuration_id UUID NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    
    PRIMARY KEY (tenant_configuration_id, config_key),
    FOREIGN KEY (tenant_configuration_id) REFERENCES tenant_configuration_versions(id) ON DELETE CASCADE
);

-- Create index for tenant_configuration_data
CREATE INDEX idx_tenant_config_data_key ON tenant_configuration_data(config_key);

-- Create tenant_configuration_metadata table for storing metadata key-value pairs
CREATE TABLE tenant_configuration_metadata (
    tenant_configuration_id UUID NOT NULL,
    metadata_key VARCHAR(255) NOT NULL,
    metadata_value TEXT,
    
    PRIMARY KEY (tenant_configuration_id, metadata_key),
    FOREIGN KEY (tenant_configuration_id) REFERENCES tenant_configuration_versions(id) ON DELETE CASCADE
);

-- Create index for tenant_configuration_metadata
CREATE INDEX idx_tenant_config_metadata_key ON tenant_configuration_metadata(metadata_key);

-- Create tenant_cloning_history table for tracking cloning operations
CREATE TABLE tenant_cloning_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_tenant_id VARCHAR(50) NOT NULL,
    source_version VARCHAR(20),
    target_tenant_id VARCHAR(50) NOT NULL,
    target_version VARCHAR(20) NOT NULL,
    target_environment VARCHAR(50) NOT NULL,
    cloned_by VARCHAR(100) NOT NULL,
    cloned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operation_type VARCHAR(50) NOT NULL, -- CLONE, ROLLBACK, TEMPLATE_CREATE, TEMPLATE_APPLY
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    configuration_data_count INTEGER DEFAULT 0,
    metadata_count INTEGER DEFAULT 0,
    overrides_applied INTEGER DEFAULT 0,
    
    CONSTRAINT chk_cloning_history_operation_type CHECK (operation_type IN ('CLONE', 'ROLLBACK', 'TEMPLATE_CREATE', 'TEMPLATE_APPLY'))
);

-- Create indexes for tenant_cloning_history
CREATE INDEX idx_cloning_history_source_tenant ON tenant_cloning_history(source_tenant_id);
CREATE INDEX idx_cloning_history_target_tenant ON tenant_cloning_history(target_tenant_id);
CREATE INDEX idx_cloning_history_cloned_at ON tenant_cloning_history(cloned_at);
CREATE INDEX idx_cloning_history_operation_type ON tenant_cloning_history(operation_type);

-- Create tenant_export_history table for tracking export operations
CREATE TABLE tenant_export_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    export_id VARCHAR(100) NOT NULL UNIQUE,
    tenant_id VARCHAR(50) NOT NULL,
    version VARCHAR(20),
    export_format VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    exported_by VARCHAR(100) NOT NULL,
    exported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    export_reason VARCHAR(500),
    configurations_count INTEGER DEFAULT 0,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    
    CONSTRAINT chk_export_history_format CHECK (export_format IN ('JSON', 'YAML', 'XML'))
);

-- Create indexes for tenant_export_history
CREATE INDEX idx_export_history_tenant_id ON tenant_export_history(tenant_id);
CREATE INDEX idx_export_history_exported_at ON tenant_export_history(exported_at);
CREATE INDEX idx_export_history_export_id ON tenant_export_history(export_id);

-- Create tenant_import_history table for tracking import operations
CREATE TABLE tenant_import_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    import_id VARCHAR(100) NOT NULL UNIQUE,
    target_tenant_id VARCHAR(50) NOT NULL,
    target_version VARCHAR(20),
    target_environment VARCHAR(50),
    import_format VARCHAR(20) NOT NULL,
    imported_by VARCHAR(100) NOT NULL,
    imported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    configurations_imported INTEGER DEFAULT 0,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    validation_results JSONB,
    
    CONSTRAINT chk_import_history_format CHECK (import_format IN ('JSON', 'YAML', 'XML'))
);

-- Create indexes for tenant_import_history
CREATE INDEX idx_import_history_target_tenant ON tenant_import_history(target_tenant_id);
CREATE INDEX idx_import_history_imported_at ON tenant_import_history(imported_at);
CREATE INDEX idx_import_history_import_id ON tenant_import_history(import_id);

-- Create tenant_templates table for storing reusable templates
CREATE TABLE tenant_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_name VARCHAR(100) NOT NULL UNIQUE,
    template_description VARCHAR(500),
    source_tenant_id VARCHAR(50) NOT NULL,
    source_version VARCHAR(20) NOT NULL,
    template_configuration_data JSONB,
    template_metadata JSONB,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    usage_count INTEGER DEFAULT 0
);

-- Create indexes for tenant_templates
CREATE INDEX idx_tenant_templates_name ON tenant_templates(template_name);
CREATE INDEX idx_tenant_templates_source_tenant ON tenant_templates(source_tenant_id);
CREATE INDEX idx_tenant_templates_active ON tenant_templates(is_active);

-- Create tenant_template_usage table for tracking template usage
CREATE TABLE tenant_template_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL,
    applied_to_tenant_id VARCHAR(50) NOT NULL,
    applied_to_version VARCHAR(20) NOT NULL,
    applied_by VARCHAR(100) NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    overrides_applied JSONB,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    
    FOREIGN KEY (template_id) REFERENCES tenant_templates(id) ON DELETE CASCADE
);

-- Create indexes for tenant_template_usage
CREATE INDEX idx_template_usage_template_id ON tenant_template_usage(template_id);
CREATE INDEX idx_template_usage_applied_to_tenant ON tenant_template_usage(applied_to_tenant_id);
CREATE INDEX idx_template_usage_applied_at ON tenant_template_usage(applied_at);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_tenant_configurations_updated_at 
    BEFORE UPDATE ON tenant_configurations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tenant_templates_updated_at 
    BEFORE UPDATE ON tenant_templates 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for testing
INSERT INTO tenant_configuration_versions (
    tenant_id, version, name, description, is_active, environment, 
    created_by, updated_by, change_log
) VALUES 
(
    'tenant-001', '1.0.0', 'Production Tenant', 'Main production tenant configuration', 
    TRUE, 'PRODUCTION', 'admin', 'admin', 'Initial production configuration'
),
(
    'tenant-001', '1.1.0', 'Production Tenant', 'Updated production tenant configuration', 
    FALSE, 'PRODUCTION', 'admin', 'admin', 'Updated configuration with new features'
),
(
    'tenant-002', '1.0.0', 'Development Tenant', 'Development environment tenant', 
    TRUE, 'DEVELOPMENT', 'admin', 'admin', 'Initial development configuration'
),
(
    'tenant-003', '1.0.0', 'UAT Tenant', 'User Acceptance Testing tenant', 
    TRUE, 'USER_ACCEPTANCE', 'admin', 'admin', 'Initial UAT configuration'
);

-- Insert sample configuration data
INSERT INTO tenant_configuration_data (tenant_configuration_id, config_key, config_value)
SELECT 
    tc.id,
    config_data.key,
    config_data.value
FROM tenant_configuration_versions tc
CROSS JOIN (
    VALUES 
        ('tenant.id', 'tenant-001'),
        ('environment', 'PRODUCTION'),
        ('database.url', 'jdbc:postgresql://localhost:5432/payment_engine'),
        ('redis.host', 'localhost'),
        ('redis.port', '6379'),
        ('logging.level', 'INFO'),
        ('cache.enabled', 'true'),
        ('monitoring.enabled', 'true')
) AS config_data(key, value)
WHERE tc.tenant_id = 'tenant-001' AND tc.version = '1.0.0';

-- Insert sample metadata
INSERT INTO tenant_configuration_metadata (tenant_configuration_id, metadata_key, metadata_value)
SELECT 
    tc.id,
    metadata_data.key,
    metadata_data.value
FROM tenant_configuration_versions tc
CROSS JOIN (
    VALUES 
        ('created.from', 'manual'),
        ('last.updated.by', 'admin'),
        ('configuration.type', 'production'),
        ('backup.enabled', 'true'),
        ('replication.enabled', 'true')
) AS metadata_data(key, value)
WHERE tc.tenant_id = 'tenant-001' AND tc.version = '1.0.0';

-- Insert sample template
INSERT INTO tenant_templates (
    template_name, template_description, source_tenant_id, source_version,
    template_configuration_data, template_metadata, created_by
) VALUES (
    'production-template',
    'Standard production tenant template',
    'tenant-001',
    '1.0.0',
    '{"tenant.id": "{{TENANT_ID}}", "environment": "PRODUCTION", "database.url": "jdbc:postgresql://prod-db:5432/payment_engine", "redis.host": "prod-redis", "logging.level": "INFO", "cache.enabled": "true", "monitoring.enabled": "true"}',
    '{"template.type": "production", "created.from": "tenant-001", "version": "1.0.0"}',
    'admin'
);

-- Create view for tenant configuration summary
CREATE VIEW tenant_configuration_summary AS
SELECT 
    tc.tenant_id,
    tc.version,
    tc.name,
    tc.description,
    tc.is_active,
    tc.environment,
    tc.source_tenant_id,
    tc.source_version,
    tc.cloned_by,
    tc.cloned_at,
    tc.created_by,
    tc.created_at,
    tc.updated_at,
    COUNT(tcd.config_key) as configuration_data_count,
    COUNT(tcm.metadata_key) as metadata_count
FROM tenant_configuration_versions tc
LEFT JOIN tenant_configuration_data tcd ON tc.id = tcd.tenant_configuration_id
LEFT JOIN tenant_configuration_metadata tcm ON tc.id = tcm.tenant_configuration_id
GROUP BY tc.id, tc.tenant_id, tc.version, tc.name, tc.description, tc.is_active, 
         tc.environment, tc.source_tenant_id, tc.source_version, tc.cloned_by, 
         tc.cloned_at, tc.created_by, tc.created_at, tc.updated_at;

-- Create view for cloning statistics
CREATE VIEW tenant_cloning_statistics AS
SELECT 
    DATE_TRUNC('day', cloned_at) as date,
    operation_type,
    COUNT(*) as operation_count,
    COUNT(CASE WHEN success = TRUE THEN 1 END) as successful_operations,
    COUNT(CASE WHEN success = FALSE THEN 1 END) as failed_operations,
    AVG(configuration_data_count) as avg_configuration_data_count,
    AVG(metadata_count) as avg_metadata_count
FROM tenant_cloning_history
GROUP BY DATE_TRUNC('day', cloned_at), operation_type
ORDER BY date DESC, operation_type;

-- Add comments for documentation
COMMENT ON TABLE tenant_configuration_versions IS 'Stores tenant configuration versions with full versioning support';
COMMENT ON TABLE tenant_configuration_data IS 'Stores configuration key-value pairs for each tenant configuration';
COMMENT ON TABLE tenant_configuration_metadata IS 'Stores metadata key-value pairs for each tenant configuration';
COMMENT ON TABLE tenant_cloning_history IS 'Tracks all tenant cloning operations for audit and analytics';
COMMENT ON TABLE tenant_export_history IS 'Tracks tenant configuration export operations';
COMMENT ON TABLE tenant_import_history IS 'Tracks tenant configuration import operations';
COMMENT ON TABLE tenant_templates IS 'Stores reusable tenant configuration templates';
COMMENT ON TABLE tenant_template_usage IS 'Tracks template usage for analytics and monitoring';

COMMENT ON COLUMN tenant_configuration_versions.environment IS 'Environment type: DEVELOPMENT, INTEGRATION, USER_ACCEPTANCE, PRODUCTION';
COMMENT ON COLUMN tenant_configuration_versions.source_tenant_id IS 'Source tenant ID for cloned configurations';
COMMENT ON COLUMN tenant_configuration_versions.source_version IS 'Source version for cloned configurations';
COMMENT ON COLUMN tenant_cloning_history.operation_type IS 'Type of operation: CLONE, ROLLBACK, TEMPLATE_CREATE, TEMPLATE_APPLY';