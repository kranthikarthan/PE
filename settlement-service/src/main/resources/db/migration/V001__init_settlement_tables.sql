-- Settlement Service Database Schema
-- This migration creates tables for settlement processing functionality

-- Settlement batches table
CREATE TABLE settlement_batches (
    batch_id VARCHAR(50) PRIMARY KEY,
    batch_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_debit DECIMAL(19,2) NOT NULL,
    total_credit DECIMAL(19,2) NOT NULL,
    net_position DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalized_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT chk_settlement_batch_amounts_positive CHECK (total_debit >= 0 AND total_credit >= 0),
    CONSTRAINT chk_settlement_batch_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- Settlement transactions table
CREATE TABLE settlement_transactions (
    settlement_txn_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    settlement_status VARCHAR(20) NOT NULL,
    included_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_settlement_transaction_batch FOREIGN KEY (batch_id) REFERENCES settlement_batches(batch_id) ON DELETE CASCADE,
    CONSTRAINT chk_settlement_transaction_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_settlement_transaction_status CHECK (settlement_status IN ('PENDING', 'SETTLED', 'FAILED', 'REVERSED'))
);

-- Settlement workflows table
CREATE TABLE settlement_workflows (
    workflow_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    workflow_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    
    CONSTRAINT fk_settlement_workflow_batch FOREIGN KEY (batch_id) REFERENCES settlement_batches(batch_id) ON DELETE CASCADE,
    CONSTRAINT chk_settlement_workflow_status CHECK (status IN ('STARTED', 'IN_PROGRESS', 'COMPLETED', 'FAILED'))
);

-- Settlement positions table
CREATE TABLE settlement_positions (
    position_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    debit_amount DECIMAL(19,2) DEFAULT 0,
    credit_amount DECIMAL(19,2) DEFAULT 0,
    net_position DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_settlement_position_batch FOREIGN KEY (batch_id) REFERENCES settlement_batches(batch_id) ON DELETE CASCADE,
    CONSTRAINT chk_settlement_position_amounts_non_negative CHECK (debit_amount >= 0 AND credit_amount >= 0)
);

-- Netting cycles table
CREATE TABLE netting_cycles (
    cycle_id VARCHAR(50) PRIMARY KEY,
    cycle_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_participants INTEGER NOT NULL,
    net_amount DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT chk_netting_cycle_participants_positive CHECK (total_participants > 0),
    CONSTRAINT chk_netting_cycle_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- Netting positions table
CREATE TABLE netting_positions (
    position_id VARCHAR(50) PRIMARY KEY,
    cycle_id VARCHAR(50) NOT NULL,
    participant_id VARCHAR(50) NOT NULL,
    net_position DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_netting_position_cycle FOREIGN KEY (cycle_id) REFERENCES netting_cycles(cycle_id) ON DELETE CASCADE
);

-- Netting transactions table
CREATE TABLE netting_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    cycle_id VARCHAR(50) NOT NULL,
    from_participant VARCHAR(50) NOT NULL,
    to_participant VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_netting_transaction_cycle FOREIGN KEY (cycle_id) REFERENCES netting_cycles(cycle_id) ON DELETE CASCADE,
    CONSTRAINT chk_netting_transaction_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_netting_transaction_status CHECK (status IN ('PENDING', 'SETTLED', 'FAILED')),
    CONSTRAINT chk_netting_transaction_different_participants CHECK (from_participant != to_participant)
);

-- Settlement orchestrations table
CREATE TABLE settlement_orchestrations (
    orchestration_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    orchestration_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    
    CONSTRAINT fk_settlement_orchestration_batch FOREIGN KEY (batch_id) REFERENCES settlement_batches(batch_id) ON DELETE CASCADE,
    CONSTRAINT chk_settlement_orchestration_status CHECK (status IN ('STARTED', 'IN_PROGRESS', 'COMPLETED', 'FAILED'))
);

-- Settlement states table
CREATE TABLE settlement_states (
    state_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    state_name VARCHAR(50) NOT NULL,
    state_data JSONB,
    entered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_settlement_state_batch FOREIGN KEY (batch_id) REFERENCES settlement_batches(batch_id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_settlement_batches_batch_date ON settlement_batches(batch_date);
CREATE INDEX idx_settlement_batches_status ON settlement_batches(status);
CREATE INDEX idx_settlement_batches_clearing_system ON settlement_batches(clearing_system);

CREATE INDEX idx_settlement_transactions_batch_id ON settlement_transactions(batch_id);
CREATE INDEX idx_settlement_transactions_transaction_id ON settlement_transactions(transaction_id);
CREATE INDEX idx_settlement_transactions_status ON settlement_transactions(settlement_status);

CREATE INDEX idx_settlement_workflows_batch_id ON settlement_workflows(batch_id);
CREATE INDEX idx_settlement_workflows_status ON settlement_workflows(status);

CREATE INDEX idx_settlement_positions_batch_id ON settlement_positions(batch_id);
CREATE INDEX idx_settlement_positions_account ON settlement_positions(account_number);

CREATE INDEX idx_netting_cycles_cycle_date ON netting_cycles(cycle_date);
CREATE INDEX idx_netting_cycles_status ON netting_cycles(status);

CREATE INDEX idx_netting_positions_cycle_id ON netting_positions(cycle_id);
CREATE INDEX idx_netting_positions_participant ON netting_positions(participant_id);

CREATE INDEX idx_netting_transactions_cycle_id ON netting_transactions(cycle_id);
CREATE INDEX idx_netting_transactions_status ON netting_transactions(status);

CREATE INDEX idx_settlement_orchestrations_batch_id ON settlement_orchestrations(batch_id);
CREATE INDEX idx_settlement_orchestrations_status ON settlement_orchestrations(status);

CREATE INDEX idx_settlement_states_batch_id ON settlement_states(batch_id);
CREATE INDEX idx_settlement_states_state_name ON settlement_states(state_name);


