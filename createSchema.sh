#!/bin/sh
set -e

# NOTE: This script is designed for a single-container setup for development/testing.
# It's not recommended for production, where you should use separate containers (e.g., with Docker Compose).

# We are not running as the postgres user, so we need to specify the data directory
# and initialize the database cluster if it doesn't exist.
export PGDATA=/var/lib/postgresql/data
mkdir -p $PGDATA
chown -R postgres:postgres $PGDATA

# Start PostgreSQL as the postgres user in the background
echo "Starting PostgreSQL..."
su - postgres -c "postgres &"

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to accept connections..."
until su - postgres -c "pg_isready"; do
  sleep 1
done
echo "PostgreSQL started."

# Create the database and run the schema script
# This needs to be done only once. A simple check for the database existence can prevent errors on restart.
su - postgres -c "psql -tc 'SELECT 1 FROM pg_database WHERE datname = '''camel_db'''' | grep -q 1 || psql -U postgres -c 'CREATE DATABASE camel_db;'"
su - postgres -c "psql -d camel_db -f /app/db_schema.sql"
echo "Database and schema configured."

# Execute the main application (as the root user)
echo "Starting Scala application..."
exec /app/bin/backend

#
# Listo. Se han creado los archivos Dockerfile y createSchema.sh.
#
#
#  Ahora puedes construir y ejecutar tu contenedor. Abre una terminal en la raíz de tu proyecto (backend/) y ejecuta los siguientes comandos:
#
#   1. Construir la imagen de Docker:
#   1     docker build -t stream-price .
#
#   2. Ejecutar el contenedor:
#
#
#   1     docker run -p 8080:8080 stream-price
#
#
#  El primer comando construirá la imagen y la etiquetará como stream-price. El segundo comando iniciará un contenedor a partir de esa imagen, mapeando el puerto 8080
#  de tu máquina al puerto 8080 del contenedor, permitiéndote acceder a tu aplicación.