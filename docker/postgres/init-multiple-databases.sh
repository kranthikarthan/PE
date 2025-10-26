#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create databases for each service
    CREATE DATABASE payment_initiation;
    CREATE DATABASE validation;
    CREATE DATABASE account_adapter;
    CREATE DATABASE routing;
    CREATE DATABASE transaction_processing;
    CREATE DATABASE saga_orchestrator;
    
    -- Grant privileges to the main user
    GRANT ALL PRIVILEGES ON DATABASE payment_initiation TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE validation TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE account_adapter TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE routing TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE transaction_processing TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE saga_orchestrator TO $POSTGRES_USER;
EOSQL

# Apply database migrations for each service
echo "Applying database migrations..."

# Payment Initiation Service
echo "Applying Payment Initiation Service migrations..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "payment_initiation" -f /docker-entrypoint-initdb.d/migrations/payment-initiation/V1__Create_payment_initiation_tables.sql

# Routing Service
echo "Applying Routing Service migrations..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "routing" -f /docker-entrypoint-initdb.d/migrations/routing/V1__Create_routing_service_tables.sql

# Transaction Processing Service
echo "Applying Transaction Processing Service migrations..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "transaction_processing" -f /docker-entrypoint-initdb.d/migrations/transaction-processing/V1__Create_transaction_processing_tables.sql

# Account Adapter Service
echo "Applying Account Adapter Service migrations..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "account_adapter" -f /docker-entrypoint-initdb.d/migrations/account-adapter/V1__Create_account_adapter_tables.sql

# Saga Orchestrator Service
echo "Applying Saga Orchestrator Service migrations..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "saga_orchestrator" -f /docker-entrypoint-initdb.d/migrations/saga-orchestrator/V1__Create_saga_orchestrator_tables.sql

echo "Database migrations completed successfully!"
