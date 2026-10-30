<#
  Test d'intégration de bout en bout, via la Gateway (tous les services doivent tourner).
  Usage : powershell -ExecutionPolicy Bypass -File tests\smoke-test.ps1
#>
param([string]$Gateway = "http://localhost:9999")

$agent = @{ Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("agent:agent123")) }
$admin = @{ Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123")) }
$script:ok = 0; $script:ko = 0

function Call($method, $url, $headers, $body = $null) {
    $p = @{ Method = $method; Uri = $url; UseBasicParsing = $true }
    if ($headers) { $p.Headers = $headers }
    if ($body) { $p.Body = ($body | ConvertTo-Json -Depth 10); $p.ContentType = "application/json; charset=utf-8" }
    try {
        $r = Invoke-WebRequest @p
        $json = $null
        if ($r.Content) { try { $json = $r.Content | ConvertFrom-Json } catch { } }
        return @{ status = [int]$r.StatusCode; body = $json }
    } catch {
        $code = 0
        if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
        return @{ status = $code; body = $null }
    }
}
function Check($name, $condition) {
    if ($condition) { Write-Host "[OK]   $name" -ForegroundColor Green; $script:ok++ }
    else { Write-Host "[FAIL] $name" -ForegroundColor Red; $script:ko++ }
}
$d = { param($n) (Get-Date).AddDays($n).ToString("yyyy-MM-dd") }
$suffix = [guid]::NewGuid().ToString("N").Substring(0, 6)

Write-Host "== Infrastructure ==" -ForegroundColor Cyan
Check "Config Server sert la config de client-service" ((Call GET "http://localhost:8888/client-service/default" $null).status -eq 200)
Check "Eureka accessible" ((Call GET "http://localhost:8761/" $null).status -eq 200)
$apps = Call GET "http://localhost:8761/eureka/apps" @{ Accept = "application/json" }
Check "Eureka liste CLIENT-SERVICE / CONTRAT-SERVICE / VEHICULE-SERVICE / SINISTRE-SERVICE / PROXY-SERVICE" (
    $apps.status -eq 200 -and ($apps.body.applications.application.name -contains "CLIENT-SERVICE") -and
    ($apps.body.applications.application.name -contains "CONTRAT-SERVICE") -and
    ($apps.body.applications.application.name -contains "VEHICULE-SERVICE") -and
    ($apps.body.applications.application.name -contains "SINISTRE-SERVICE") -and
    ($apps.body.applications.application.name -contains "PROXY-SERVICE"))
Check "Gateway -> client-service /api/info" ((Call GET "$Gateway/client-service/api/info" $null).status -eq 200)
Check "Actuator health (public)" ((Call GET "http://localhost:8081/actuator/health" $null).status -eq 200)
Check "Actuator metrics protégé sans login (401)" ((Call GET "http://localhost:8081/actuator/metrics" $null).status -eq 401)
Check "Actuator metrics avec ADMIN (200)" ((Call GET "http://localhost:8081/actuator/metrics" $admin).status -eq 200)
foreach ($p in 8081, 8082, 8083, 8084) {
    Check "Swagger UI sur le port $p" ((Call GET "http://localhost:$p/swagger-ui/index.html" $null).status -eq 200)
    Check "OpenAPI JSON sur le port $p" ((Call GET "http://localhost:$p/v3/api-docs" $null).status -eq 200)
}

Write-Host "== Sécurité ==" -ForegroundColor Cyan
Check "GET /api/clients sans authentification -> 401" ((Call GET "$Gateway/client-service/api/clients" $null).status -eq 401)
Check "Mauvais mot de passe -> 401" ((Call GET "$Gateway/client-service/api/clients" @{ Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("agent:faux")) }).status -eq 401)

Write-Host "== Client ==" -ForegroundColor Cyan
$client = @{ nom = "Kabre"; prenom = "Awa"; dateNaissance = "1990-05-01"; genre = "FEMININ";
    adresse = @{ rue = "12 rue du Marche"; ville = "Ouagadougou"; pays = "Burkina Faso" };
    email = "awa.$suffix@mail.com"; telephones = @("+22670000000", "+22671111111");
    numeroPieceIdentite = "CNIB$suffix"; permisConduire = @{ numero = "P-$suffix"; categorie = "B"; dateObtention = "2012-01-15" };
    situationFamiliale = "MARIE"; profession = "Ingenieure" }
$r = Call POST "$Gateway/client-service/api/clients" $agent $client
Check "Création client -> 201" ($r.status -eq 201)
$clientId = $r.body.id
Check "Pièce d'identité en clair pour ADMIN" ((Call GET "$Gateway/client-service/api/clients/$clientId" $admin).body.numeroPieceIdentite -eq "CNIB$suffix")
Check "Pièce d'identité masquée pour AGENT" ((Call GET "$Gateway/client-service/api/clients/$clientId" $agent).body.numeroPieceIdentite -like "*****$($suffix.Substring(3))")
Check "Email en doublon -> 409" ((Call POST "$Gateway/client-service/api/clients" $agent $client).status -eq 409)
$bad = $client.Clone(); $bad.email = "pas-un-email"
Check "Données invalides -> 400" ((Call POST "$Gateway/client-service/api/clients" $agent $bad).status -eq 400)
Check "Recherche par ville" ((Call GET "$Gateway/client-service/api/clients?ville=Ouagadougou" $agent).body.Count -ge 1)
$client.profession = "Directrice"
Check "Modification client (PUT)" ((Call PUT "$Gateway/client-service/api/clients/$clientId" $agent $client).body.profession -eq "Directrice")
Check "Client inexistant -> 404" ((Call GET "$Gateway/client-service/api/clients/999999" $agent).status -eq 404)

Write-Host "== Contrat ==" -ForegroundColor Cyan
$contrat = @{ clientId = $clientId; typeContrat = "TOUS_RISQUES"; dateDebut = (& $d -30); dateFin = (& $d 335);
    renouvellementAutomatique = $true; primeAnnuelle = 450000;
    garanties = @(@{ type = "RESPONSABILITE_CIVILE"; plafond = 100000000 }, @{ type = "DOMMAGES_COLLISION"; plafond = 8000000 }, @{ type = "VOL"; plafond = 6000000 });
    franchises = @(@{ garantie = "DOMMAGES_COLLISION"; montant = 100000 });
    options = @("ASSISTANCE") }
$r = Call POST "$Gateway/contrat-service/api/contrats" $agent $contrat
Check "Création contrat -> 201 (client vérifié via client-service)" ($r.status -eq 201)
$contratId = $r.body.id
$ghost = $contrat.Clone(); $ghost.clientId = 999999
Check "Contrat pour client inexistant -> 422" ((Call POST "$Gateway/contrat-service/api/contrats" $agent $ghost).status -eq 422)
Check "Historique client enrichi (SOUSCRIPTION)" ((Call GET "$Gateway/client-service/api/clients/$clientId/historique" $agent).body.Count -ge 1)
Check "Contrats du client" ((Call GET "$Gateway/contrat-service/api/contrats?clientId=$clientId" $agent).body.Count -eq 1)

Write-Host "== Véhicule ==" -ForegroundColor Cyan
$veh = @{ contratId = $contratId; immatriculation = "AB-$suffix"; marque = "Toyota"; modele = "Corolla"; anneeFabrication = 2021;
    vin = "JTDKB20U09" + (Get-Random -Minimum 1000000 -Maximum 9999999); datePremiereMiseEnCirculation = "2021-03-01";
    kilometrageDeclare = 42000; zoneStationnement = "Ouagadougou Centre"; typeGarage = "PRIVE"; usagePrincipal = "PERSONNEL";
    kilometrageAnnuelEstime = 12000; systemeAlarme = $true; dispositifGeolocalisation = $false; autresEquipements = @("Anti-demarrage") }
$r = Call POST "$Gateway/vehicule-service/api/vehicules" $agent $veh
Check "Enregistrement véhicule -> 201 (contrat vérifié via contrat-service)" ($r.status -eq 201)
$vehId = $r.body.id
$veh2 = $veh.Clone(); $veh2.contratId = 999999
Check "Véhicule pour contrat inexistant -> 422" ((Call POST "$Gateway/vehicule-service/api/vehicules" $agent $veh2).status -eq 422)
Check "Kilométrage qui diminue -> 422" ((Call PATCH "$Gateway/vehicule-service/api/vehicules/$vehId/kilometrage" $agent @{ kilometrage = 100 }).status -eq 422)
Check "Mise à jour kilométrage" ((Call PATCH "$Gateway/vehicule-service/api/vehicules/$vehId/kilometrage" $agent @{ kilometrage = 45000 }).body.kilometrageDeclare -eq 45000)
Check "Véhicules du contrat" ((Call GET "$Gateway/vehicule-service/api/vehicules?contratId=$contratId" $agent).body.Count -eq 1)

Write-Host "== Sinistre ==" -ForegroundColor Cyan
$sin = @{ contratId = $contratId; vehiculeId = $vehId; dateSinistre = (& $d -2); description = "Collision au carrefour";
    typeIncident = "COLLISION"; lieu = "Avenue Kwame Nkrumah, Ouagadougou"; montantEstime = 1500000 }
$r = Call POST "$Gateway/sinistre-service/api/sinistres" $agent $sin
Check "Déclaration sinistre -> 201 (contrat + véhicule vérifiés)" ($r.status -eq 201)
$sinId = $r.body.id
$sin2 = $sin.Clone(); $sin2.dateSinistre = (& $d -400)
Check "Sinistre hors période de couverture -> 422" ((Call POST "$Gateway/sinistre-service/api/sinistres" $agent $sin2).status -eq 422)
Check "Transition interdite (DECLARE -> INDEMNISE) -> 422" ((Call POST "$Gateway/sinistre-service/api/sinistres/$sinId/statut" $agent @{ statut = "INDEMNISE"; montantIndemnisation = 1000 }).status -eq 422)
Check "DECLARE -> EN_EXPERTISE" ((Call POST "$Gateway/sinistre-service/api/sinistres/$sinId/statut" $agent @{ statut = "EN_EXPERTISE"; commentaire = "Expert mandate" }).body.statut -eq "EN_EXPERTISE")
Check "EN_EXPERTISE -> ACCEPTE" ((Call POST "$Gateway/sinistre-service/api/sinistres/$sinId/statut" $agent @{ statut = "ACCEPTE" }).body.statut -eq "ACCEPTE")
Check "ACCEPTE -> INDEMNISE" ((Call POST "$Gateway/sinistre-service/api/sinistres/$sinId/statut" $agent @{ statut = "INDEMNISE"; montantIndemnisation = 1200000 }).body.montantIndemnisation -eq 1200000)
Check "Sinistres du contrat" ((Call GET "$Gateway/sinistre-service/api/sinistres?contratId=$contratId" $agent).body.Count -eq 1)
Check "Sinistres du véhicule" ((Call GET "$Gateway/sinistre-service/api/sinistres?vehiculeId=$vehId" $agent).body.Count -eq 1)
Check "Historique de traitement (4 étapes)" ((Call GET "$Gateway/sinistre-service/api/sinistres/$sinId/historique" $agent).body.Count -eq 4)

Write-Host "== Autorisations ==" -ForegroundColor Cyan
Check "DELETE client avec AGENT -> 403" ((Call DELETE "$Gateway/client-service/api/clients/$clientId" $agent).status -eq 403)

Write-Host ""
Write-Host "Résultat : $($script:ok) réussis, $($script:ko) échoués" -ForegroundColor $(if ($script:ko -eq 0) { "Green" } else { "Red" })
exit $script:ko
