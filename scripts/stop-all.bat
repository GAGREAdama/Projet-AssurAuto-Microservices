@echo off
REM Arrête les services AssurAuto (ports 8888 8761 8081-8084 9999 + instances 8091-8093)
powershell -NoProfile -Command "foreach($p in 8888,8761,8081,8082,8083,8084,9999,8091,8092,8093){ Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force; Write-Host ('Port ' + $p + ' arrete') } }"
