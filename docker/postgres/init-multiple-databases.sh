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
