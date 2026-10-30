-- MySQL : base dédiée de client_db (exécuté automatiquement par docker-compose,
-- ou manuellement avec un client SQL / MySQL Workbench). Les TABLES sont créées par Hibernate (ddl-auto=update).
CREATE DATABASE IF NOT EXISTS client_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'assurauto'@'%' IDENTIFIED BY 'assurauto';
GRANT ALL PRIVILEGES ON client_db.* TO 'assurauto'@'%';
FLUSH PRIVILEGES;
