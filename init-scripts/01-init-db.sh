#!/bin/sh
set -e

echo "==> [PostgreSQL Init] Initializing PlaySwap databases..."

# Enable uuid-ossp on the default POSTGRES_DB (playswap)
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -c 'CREATE EXTENSION IF NOT EXISTS "uuid-ossp";'

# Import schema into the unified playswap database used by the modular monolith
echo "==> [PostgreSQL Init] Importing auth tables into $POSTGRES_DB..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f /docker-entrypoint-initdb.d/auth_service_data.sql || echo "Warning: auth_service_data import completed with warnings."

echo "==> [PostgreSQL Init] Importing transfer tables into $POSTGRES_DB..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f /docker-entrypoint-initdb.d/transfer_service_data.sql || echo "Warning: transfer_service_data import completed with warnings."

if [ -f /docker-entrypoint-initdb.d/schema_indexes.sql ]; then
    echo "==> [PostgreSQL Init] Applying performance indexes to $POSTGRES_DB..."
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f /docker-entrypoint-initdb.d/schema_indexes.sql || echo "Warning: schema_indexes applied with warnings."
fi

# Backward compatibility: Also create legacy separate databases if needed
echo "==> [PostgreSQL Init] Setting up legacy separate databases for compatibility..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE playswap_auth;
    CREATE DATABASE playswap_transfer;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "playswap_auth" -c 'CREATE EXTENSION IF NOT EXISTS "uuid-ossp";'
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "playswap_transfer" -c 'CREATE EXTENSION IF NOT EXISTS "uuid-ossp";'

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "playswap_auth" -f /docker-entrypoint-initdb.d/auth_service_data.sql || echo "Warning: legacy auth import completed with warnings."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "playswap_transfer" -f /docker-entrypoint-initdb.d/transfer_service_data.sql || echo "Warning: legacy transfer import completed with warnings."

echo "==> [PostgreSQL Init] Database setup completed successfully!"
