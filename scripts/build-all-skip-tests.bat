@echo off
cd /d "%~dp0.."
call mvn clean install -DskipTests
