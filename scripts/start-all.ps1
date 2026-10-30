<#
  Démarre tous les microservices AssurAuto dans l'ordre imposé, en attendant que chacun soit UP.
  Usage :  powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1 [-Profile sql]
  -Profile sql : bases réelles (MySQL/PostgreSQL/MariaDB via database\docker-compose.yml) au lieu de H2.
#>
param([string]$Profile = "")

$root = Split-Path -Parent $PSScriptRoot
$services = @(
    @{ name = "config-service";    port = 8888; biz = $false },
    @{ name = "discovery-service"; port = 8761; biz = $false },
    @{ name = "client-service";    port = 8081; biz = $true },
    @{ name = "contrat-service";   port = 8082; biz = $true },
    @{ name = "vehicule-service";  port = 8083; biz = $true },
    @{ name = "sinistre-service";  port = 8084; biz = $true },
    @{ name = "proxy-service";     port = 9999; biz = $false }
)

function Wait-Up($name, $port) {
    for ($i = 0; $i -lt 120; $i++) {
        try {
            $r = Invoke-RestMethod -Uri "http://localhost:$port/actuator/health" -TimeoutSec 3
            if ($r.status -eq "UP") { Write-Host "  -> $name est UP" -ForegroundColor Green; return }
        } catch { }
        Start-Sleep -Seconds 3
    }
    throw "$name n'a pas démarré dans le délai imparti (voir sa fenêtre de console)"
}

foreach ($s in $services) {
    Write-Host "Démarrage de $($s.name) (port $($s.port))..." -ForegroundColor Cyan
    $mvn = "mvn spring-boot:run"
    if ($Profile -ne "" -and $s.biz) { $mvn += " -Dspring-boot.run.profiles=$Profile" }
    $cmd = "title $($s.name) && cd /d `"$root\$($s.name)`" && $mvn"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k", $cmd
    Wait-Up $s.name $s.port
}
Write-Host ""
Write-Host "Tous les services sont démarrés." -ForegroundColor Green
Write-Host "Eureka : http://localhost:8761   Gateway : http://localhost:9999"
