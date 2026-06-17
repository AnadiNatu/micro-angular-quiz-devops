#!/bin/bash
# =============================================================================
# postgres-init/01-init-databases.sh
# Runs ONCE on first container start (postgres Docker entrypoint convention)
# Creates separate databases and users for each microservice
# =============================================================================
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL

    -- ── Auth Service Database ────────────────────────────────────────────────
    CREATE USER ${AUTH_DB_USER} WITH PASSWORD '${AUTH_DB_PASSWORD}';
    CREATE DATABASE ${AUTH_DB_NAME} OWNER ${AUTH_DB_USER};
    GRANT ALL PRIVILEGES ON DATABASE ${AUTH_DB_NAME} TO ${AUTH_DB_USER};

    -- ── Question Service Database ─────────────────────────────────────────────
    CREATE USER ${QUESTION_DB_USER} WITH PASSWORD '${QUESTION_DB_PASSWORD}';
    CREATE DATABASE ${QUESTION_DB_NAME} OWNER ${QUESTION_DB_USER};
    GRANT ALL PRIVILEGES ON DATABASE ${QUESTION_DB_NAME} TO ${QUESTION_DB_USER};

    -- ── Quiz Service Database ─────────────────────────────────────────────────
    CREATE USER ${QUIZ_DB_USER} WITH PASSWORD '${QUIZ_DB_PASSWORD}';
    CREATE DATABASE ${QUIZ_DB_NAME} OWNER ${QUIZ_DB_USER};
    GRANT ALL PRIVILEGES ON DATABASE ${QUIZ_DB_NAME} TO ${QUIZ_DB_USER};

EOSQL

echo "✅ All databases and users created successfully"
