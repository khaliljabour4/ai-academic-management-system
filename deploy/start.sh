#!/bin/bash
set -e

export PORT="${PORT:-80}"

# --- Secrets: use the ones you set, otherwise generate (fine for a demo) ---
if [ -z "$DB_PASSWORD" ]; then DB_PASSWORD="$(openssl rand -hex 16)"; fi
if [ -z "$JWT_SECRET" ]; then
  JWT_SECRET="$(openssl rand -hex 32)"
  echo "WARNING: JWT_SECRET not set, generated a temporary one (users are logged out on every restart)."
fi
export DB_PASSWORD JWT_SECRET
export DB_URL="jdbc:mysql://127.0.0.1:3306/SchoolManagement?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false"
export DB_USERNAME="school"

# --- nginx listens on the port the host gives us ---
sed -i "s/__PORT__/${PORT}/" /etc/nginx/conf.d/app.conf

# --- Database: create the data folder, the database and the user by itself ---
mkdir -p /run/mysqld && chown mysql:mysql /run/mysqld
if [ ! -d /var/lib/mysql/mysql ]; then
  mariadb-install-db --user=mysql --datadir=/var/lib/mysql > /dev/null
fi
chown -R mysql:mysql /var/lib/mysql

mariadbd --user=mysql --skip-networking=0 --bind-address=127.0.0.1 &
for i in $(seq 1 60); do
  mariadb-admin ping --silent 2>/dev/null && break
  sleep 1
done

mariadb <<SQL
CREATE DATABASE IF NOT EXISTS SchoolManagement CHARACTER SET utf8mb4;
CREATE OR REPLACE USER 'school'@'localhost'  IDENTIFIED BY '${DB_PASSWORD}';
CREATE OR REPLACE USER 'school'@'127.0.0.1'  IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON SchoolManagement.* TO 'school'@'localhost';
GRANT ALL PRIVILEGES ON SchoolManagement.* TO 'school'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL
mariadb-admin shutdown
wait || true

# --- Start everything (database, backend, AI service, nginx) ---
exec /usr/bin/supervisord -n -c /etc/supervisor/supervisord.conf
