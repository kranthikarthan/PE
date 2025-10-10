CREATE TABLE IF NOT EXISTS payment_audit (
    id SERIAL PRIMARY KEY,
    uetr UUID NOT NULL,
    tenant_id VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
