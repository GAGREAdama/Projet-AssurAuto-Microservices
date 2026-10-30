@echo off
REM Lance les 4 bases réelles (nécessite Docker Desktop) : MySQL, PostgreSQL, MariaDB, MySQL
cd /d "%~dp0..\database"
docker compose up -d
