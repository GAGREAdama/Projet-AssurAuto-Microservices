@echo off
REM Démarre tous les services dans l'ordre (config, discovery, client, contrat, vehicule, sinistre, proxy)
REM Usage : scripts\start-all.bat [sql]
if "%1"=="" (
  powershell -ExecutionPolicy Bypass -File "%~dp0start-all.ps1"
) else (
  powershell -ExecutionPolicy Bypass -File "%~dp0start-all.ps1" -Profile %1
)
