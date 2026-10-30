-- PostgreSQL : base dédiée de contrat-service.
-- Avec docker-compose, rien à faire (variables POSTGRES_*). Pour une installation locale, exécuter en superutilisateur :
CREATE USER assurauto WITH PASSWORD 'assurauto';
CREATE DATABASE contrat_db OWNER assurauto ENCODING 'UTF8';
