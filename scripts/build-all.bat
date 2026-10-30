@echo off
REM Compile, exécute les tests unitaires et package tous les modules
cd /d "%~dp0.."
call mvn clean install
