@echo off
REM Démarre UN service. Usage : scripts\start-service.bat <nom-du-service> [port] [profil]
REM Exemples : scripts\start-service.bat client-service
REM            scripts\start-service.bat client-service 8091        (2e instance, test de répartition de charge)
if "%1"=="" (
  echo Usage : start-service.bat ^<nom-du-service^> [port] [profil]
  exit /b 1
)
set ARGS=
if not "%2"=="" set ARGS=--server.port=%2
set PROFILE=
if not "%3"=="" set PROFILE=-Dspring-boot.run.profiles=%3
cd /d "%~dp0..\%1"
call mvn spring-boot:run %PROFILE% "-Dspring-boot.run.arguments=%ARGS%"
