-- =====================================================
-- IAM SERVICE DATABASE (Phase 3.2)
-- =====================================================
-- Identity and Access Management tables for user authentication,
-- authorization, and role-based access control
--
-- Security:
-- - Passwords hashed with bcrypt (never stored in plain text)
-- - Tokens stored with hash (never full token in DB)
-- - AES-256 encryption for sensitive fields
-- - Row-Level Security (RLS) for multi-tenancy
-- - Audit logging for all operations

-- =====================================================
-- USERS (Authentication identity)
-- =====================================================
CREATE TABLE users
(
    user_id          VARCHAR(50) PRIMARY KEY,
    tenant_id        VARCHAR(20) NOT NULL,
    username         VARCHAR(100) NOT NULL,
    email            VARCHAR(200) NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    full_name        VARCHAR(200) NOT NULL,
    phone_number     VARCHAR(20),
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION')),
    is_admin         BOOLEAN DEFAULT FALSE,
    mfa_enabled      BOOLEAN DEFAULT FALSE,
    mfa_method       VARCHAR(50) CHECK (mfa_method IN ('SMS', 'EMAIL', 'TOTP', 'PUSH')),
    last_login_at    TIMESTAMP,
    last_login_ip    VARCHAR(45),
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    account_locked   BOOLEAN DEFAULT FALSE,
    account_locked_until TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),

    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE RESTRICT,
    CONSTRAINT uq_users_email_tenant UNIQUE (email, tenant_id),
    CONSTRAINT uq_users_username_tenant UNIQUE (username, tenant_id)
);

CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- =====================================================
-- ROLES (Authorization groups)
-- =====================================================
CREATE TABLE roles
(
    role_id        VARCHAR(50) PRIMARY KEY,
    tenant_id      VARCHAR(20) NOT NULL,
    role_name      VARCHAR(100) NOT NULL,
    description    TEXT,
    role_type      VARCHAR(50) NOT NULL CHECK (role_type IN ('CUSTOMER', 'BUSINESS_USER', 'OPERATOR', 'ADMIN', 'CUSTOM')),
    is_built_in    BOOLEAN DEFAULT FALSE,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),

    CONSTRAINT fk_roles_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT uq_roles_name_tenant UNIQUE (role_name, tenant_id)
);

CREATE INDEX idx_roles_tenant ON roles(tenant_id);
CREATE INDEX idx_roles_type ON roles(role_type);
CREATE INDEX idx_roles_status ON roles(status);

-- =====================================================
-- PERMISSIONS (Fine-grained access rights)
-- =====================================================
CREATE TABLE permissions
(
    permission_id    VARCHAR(100) PRIMARY KEY,
    resource         VARCHAR(100) NOT NULL,
    action           VARCHAR(50) NOT NULL,
    description      TEXT,
    category         VARCHAR(50) NOT NULL CHECK (category IN ('PAYMENT', 'ACCOUNT', 'REPORTING', 'ADMIN', 'AUDIT', 'TENANT')),
    is_system        BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_permissions_resource_action UNIQUE (resource, action)
);

CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_category ON permissions(category);

-- =====================================================
-- ROLE_PERMISSIONS (Many-to-many: Roles to Permissions)
-- =====================================================
CREATE TABLE role_permissions
(
    role_permission_id VARCHAR(50) PRIMARY KEY,
    role_id           VARCHAR(50) NOT NULL,
    permission_id     VARCHAR(100) NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(100),

    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE,
    CONSTRAINT uq_role_permissions UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);

-- =====================================================
-- USER_ROLES (Many-to-many: Users to Roles)
-- =====================================================
CREATE TABLE user_roles
(
    user_role_id  VARCHAR(50) PRIMARY KEY,
    user_id       VARCHAR(50) NOT NULL,
    role_id       VARCHAR(50) NOT NULL,
    assigned_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by   VARCHAR(100),
    revoked_at    TIMESTAMP,
    revoked_by    VARCHAR(100),
    is_active     BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT uq_user_roles UNIQUE (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
CREATE INDEX idx_user_roles_is_active ON user_roles(is_active);

-- =====================================================
-- OAUTH_TOKENS (JWT and OAuth tokens)
-- =====================================================
CREATE TABLE oauth_tokens
(
    token_id          VARCHAR(100) PRIMARY KEY,
    user_id           VARCHAR(50) NOT NULL,
    tenant_id         VARCHAR(20) NOT NULL,
    token_type        VARCHAR(50) NOT NULL CHECK (token_type IN ('JWT_ACCESS', 'JWT_REFRESH', 'OAUTH2', 'API_KEY')),
    token_hash        VARCHAR(255) NOT NULL,
    scopes            VARCHAR(500),
    client_id         VARCHAR(100),
    ip_address        VARCHAR(45),
    user_agent        TEXT,
    issued_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at        TIMESTAMP NOT NULL,
    revoked_at        TIMESTAMP,
    revoked_reason    VARCHAR(200),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_oauth_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_oauth_tokens_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE
);

CREATE INDEX idx_oauth_tokens_user ON oauth_tokens(user_id);
CREATE INDEX idx_oauth_tokens_tenant ON oauth_tokens(tenant_id);
CREATE INDEX idx_oauth_tokens_token_type ON oauth_tokens(token_type);
CREATE INDEX idx_oauth_tokens_expires_at ON oauth_tokens(expires_at);
CREATE INDEX idx_oauth_tokens_revoked_at ON oauth_tokens(revoked_at);

-- =====================================================
-- LOGIN_ATTEMPTS (Track failed login attempts for security)
-- =====================================================
CREATE TABLE login_attempts
(
    attempt_id    VARCHAR(50) PRIMARY KEY,
    user_id       VARCHAR(50),
    tenant_id     VARCHAR(20) NOT NULL,
    username      VARCHAR(100) NOT NULL,
    ip_address    VARCHAR(45) NOT NULL,
    user_agent    TEXT,
    success       BOOLEAN NOT NULL DEFAULT FALSE,
    failure_reason VARCHAR(200),
    attempt_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_login_attempts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE
);

CREATE INDEX idx_login_attempts_user_id ON login_attempts(user_id);
CREATE INDEX idx_login_attempts_tenant ON login_attempts(tenant_id);
CREATE INDEX idx_login_attempts_ip ON login_attempts(ip_address);
CREATE INDEX idx_login_attempts_attempt_at ON login_attempts(attempt_at DESC);
CREATE INDEX idx_login_attempts_success ON login_attempts(success);

-- =====================================================
-- SESSIONS (Active user sessions)
-- =====================================================
CREATE TABLE sessions
(
    session_id    VARCHAR(100) PRIMARY KEY,
    user_id       VARCHAR(50) NOT NULL,
    tenant_id     VARCHAR(20) NOT NULL,
    ip_address    VARCHAR(45) NOT NULL,
    user_agent    TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at    TIMESTAMP NOT NULL,
    is_valid      BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_sessions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE
);

CREATE INDEX idx_sessions_user ON sessions(user_id);
CREATE INDEX idx_sessions_tenant ON sessions(tenant_id);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);
CREATE INDEX idx_sessions_is_valid ON sessions(is_valid);

-- =====================================================
-- Row-Level Security (RLS) for multi-tenancy
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE role_permissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE oauth_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE login_attempts ENABLE ROW LEVEL SECURITY;
ALTER TABLE sessions ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Users can only see users from their tenant
CREATE POLICY users_tenant_isolation ON users
    USING (tenant_id = current_setting('app.current_tenant_id')::varchar);

-- RLS Policy: Roles can only see roles from their tenant
CREATE POLICY roles_tenant_isolation ON roles
    USING (tenant_id = current_setting('app.current_tenant_id')::varchar);

-- RLS Policy: User roles can only see roles from their tenant
CREATE POLICY user_roles_tenant_isolation ON user_roles
    USING (user_id IN (SELECT user_id FROM users WHERE tenant_id = current_setting('app.current_tenant_id')::varchar));

-- RLS Policy: Tokens can only see tokens from their tenant
CREATE POLICY oauth_tokens_tenant_isolation ON oauth_tokens
    USING (tenant_id = current_setting('app.current_tenant_id')::varchar);

-- RLS Policy: Login attempts can only see attempts from their tenant
CREATE POLICY login_attempts_tenant_isolation ON login_attempts
    USING (tenant_id = current_setting('app.current_tenant_id')::varchar);

-- RLS Policy: Sessions can only see sessions from their tenant
CREATE POLICY sessions_tenant_isolation ON sessions
    USING (tenant_id = current_setting('app.current_tenant_id')::varchar);

-- =====================================================
-- DEFAULT ROLES (Built-in system roles)
-- =====================================================

-- Note: Insert actual roles in application code or separate migration
-- This ensures that tenants are already set up before creating roles

-- =====================================================
-- AUDIT TRAIL VIEW
-- =====================================================

-- Create a view for IAM audit trail
CREATE VIEW iam_audit_trail AS
SELECT
    'USER_LOGIN' as event_type,
    user_id,
    tenant_id,
    ip_address,
    attempt_at as event_at,
    CASE WHEN success THEN 'SUCCESS' ELSE 'FAILURE' END as status,
    failure_reason as description
FROM login_attempts
UNION ALL
SELECT
    'TOKEN_CREATED' as event_type,
    user_id,
    tenant_id,
    ip_address,
    issued_at as event_at,
    'SUCCESS' as status,
    token_type as description
FROM oauth_tokens
WHERE revoked_at IS NULL
UNION ALL
SELECT
    'TOKEN_REVOKED' as event_type,
    user_id,
    tenant_id,
    NULL as ip_address,
    revoked_at as event_at,
    'REVOKED' as status,
    revoked_reason as description
FROM oauth_tokens
WHERE revoked_at IS NOT NULL
ORDER BY event_at DESC;

-- =====================================================
-- Comments for documentation
-- =====================================================

COMMENT ON TABLE users IS 'User authentication identities - stores user credentials and metadata';
COMMENT ON TABLE roles IS 'Authorization roles that group permissions';
COMMENT ON TABLE permissions IS 'Fine-grained permissions (resource:action pairs)';
COMMENT ON TABLE oauth_tokens IS 'JWT and OAuth tokens issued to users - tokens are hashed for security';
COMMENT ON TABLE login_attempts IS 'Audit trail of login attempts (successful and failed)';
COMMENT ON TABLE sessions IS 'Active user sessions for session management';

COMMENT ON COLUMN users.password_hash IS 'Bcrypt hash of password - NEVER store plain text password';
COMMENT ON COLUMN oauth_tokens.token_hash IS 'Hash of token - full token never stored in database';
COMMENT ON COLUMN users.mfa_enabled IS 'Whether multi-factor authentication is enabled for this user';
COMMENT ON COLUMN user_roles.is_active IS 'Whether this role assignment is currently active';
