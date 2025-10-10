-- Create tenant authentication configuration table
CREATE TABLE IF NOT EXISTS payment_engine.tenant_auth_configuration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    auth_method VARCHAR(20) NOT NULL DEFAULT 'JWT',
    client_id VARCHAR(100),
    client_secret VARCHAR(500),
    client_id_header_name VARCHAR(100) DEFAULT 'X-Client-ID',
    client_secret_header_name VARCHAR(100) DEFAULT 'X-Client-Secret',
    auth_header_name VARCHAR(100) DEFAULT 'Authorization',
    auth_header_prefix VARCHAR(20) DEFAULT 'Bearer',
    token_endpoint VARCHAR(100),
    public_key_endpoint VARCHAR(100),
    jws_public_key VARCHAR(100),
    jws_algorithm VARCHAR(50) DEFAULT 'HS256',
    jws_issuer VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    include_client_headers BOOLEAN NOT NULL DEFAULT false,
    description VARCHAR(500),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for tenant authentication configuration
CREATE INDEX IF NOT EXISTS idx_tenant_auth_tenant_id ON payment_engine.tenant_auth_configuration(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_auth_active ON payment_engine.tenant_auth_configuration(is_active);
CREATE INDEX IF NOT EXISTS idx_tenant_auth_created_at ON payment_engine.tenant_auth_configuration(created_at);
CREATE INDEX IF NOT EXISTS idx_tenant_auth_method ON payment_engine.tenant_auth_configuration(auth_method);
CREATE INDEX IF NOT EXISTS idx_tenant_auth_client_id ON payment_engine.tenant_auth_configuration(client_id);

-- Add comments to table and columns
COMMENT ON TABLE payment_engine.tenant_auth_configuration IS 'Stores tenant-specific authentication configuration for outgoing HTTP calls';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.id IS 'Unique identifier for the configuration';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.tenant_id IS 'Tenant identifier';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.auth_method IS 'Authentication method (JWT, JWS, OAUTH2, API_KEY, BASIC)';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.client_id IS 'Client identifier for authentication';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.client_secret IS 'Client secret for authentication';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.client_id_header_name IS 'HTTP header name for client ID';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.client_secret_header_name IS 'HTTP header name for client secret';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.auth_header_name IS 'HTTP header name for authentication token';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.auth_header_prefix IS 'Prefix for authentication header (e.g., Bearer)';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.token_endpoint IS 'Endpoint for obtaining authentication tokens';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.public_key_endpoint IS 'Endpoint for obtaining public keys for JWS verification';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.jws_public_key IS 'Public key for JWS verification';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.jws_algorithm IS 'JWS signing algorithm (HS256, HS384, HS512, RS256, RS384, RS512)';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.jws_issuer IS 'JWS issuer identifier';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.is_active IS 'Whether this configuration is currently active';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.include_client_headers IS 'Whether to include client ID and secret in outgoing requests';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.description IS 'Description of the configuration';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.created_by IS 'User who created the configuration';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.updated_by IS 'User who last updated the configuration';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.created_at IS 'Timestamp when the configuration was created';
COMMENT ON COLUMN payment_engine.tenant_auth_configuration.updated_at IS 'Timestamp when the configuration was last updated';

-- Insert default configurations for existing tenants (if any)
-- This is a placeholder - in practice, you would query existing tenants and create default configs
-- INSERT INTO payment_engine.tenant_auth_configuration (tenant_id, auth_method, description, created_by)
-- SELECT DISTINCT tenant_id, 'JWT', 'Default JWT configuration', 'system'
-- FROM payment_engine.tenant_configuration_versions
-- WHERE tenant_id NOT IN (SELECT tenant_id FROM payment_engine.tenant_auth_configuration);

-- Create a function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION payment_engine.update_tenant_auth_configuration_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update updated_at timestamp
CREATE TRIGGER trigger_update_tenant_auth_configuration_updated_at
    BEFORE UPDATE ON payment_engine.tenant_auth_configuration
    FOR EACH ROW
    EXECUTE FUNCTION payment_engine.update_tenant_auth_configuration_updated_at();

-- Add constraints
ALTER TABLE payment_engine.tenant_auth_configuration 
ADD CONSTRAINT chk_tenant_auth_method 
CHECK (auth_method IN ('JWT', 'JWS', 'OAUTH2', 'API_KEY', 'BASIC'));

ALTER TABLE payment_engine.tenant_auth_configuration 
ADD CONSTRAINT chk_tenant_auth_jws_algorithm 
CHECK (jws_algorithm IN ('HS256', 'HS384', 'HS512', 'RS256', 'RS384', 'RS512'));

-- Ensure only one active configuration per tenant
CREATE UNIQUE INDEX IF NOT EXISTS idx_tenant_auth_unique_active 
ON payment_engine.tenant_auth_configuration(tenant_id) 
WHERE is_active = true;