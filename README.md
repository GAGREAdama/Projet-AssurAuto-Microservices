# AssurAuto – Architecture microservices (Spring Boot 4 / Spring Cloud)

## 1. Prérequis (Windows)
| Outil | Version |
|---|---|
| JDK | **21** (17 minimum) – `java -version` |
| Maven | 3.9+ – `mvn -v` (ou Maven Wrapper de l'IDE) |
| Connexion Internet | première compilation (téléchargement des dépendances Maven) |
| Docker Desktop | *optionnel*, uniquement pour les bases réelles (profil `sql`) |

Versions : Spring Boot **4.0.1**, Spring Cloud **2025.1.0**, springdoc-openapi **3.1.1** (version du Guide TP).
Elles sont centralisées dans le `pom.xml` de chaque module (`<spring-cloud.version>`, `<springdoc.version>` et le `<parent>`). Si Maven ne trouve pas une version, la remplacer par la dernière 4.0.x / 2025.1.x / 3.x disponible.

## 2. Services et ports
| Ordre | Service | Port | Rôle |
|---|---|---|---|
| 1 | config-service | 8888 | Spring Cloud Config Server (config dans `config-service/src/main/resources/config/`) |
| 2 | discovery-service | 8761 | Eureka |
| 3 | client-service | 8081 | Clients (MySQL en profil `sql`) |
| 4 | contrat-service | 8082 | Contrats (PostgreSQL en profil `sql`) |
| 5 | vehicule-service | 8083 | Véhicules (MariaDB en profil `sql`) |
| 6 | sinistre-service | 8084 | Sinistres (MySQL en profil `sql`) |
| 7 | proxy-service | 9999 | Spring Cloud Gateway (routes `lb://`) |

## 3. Installation et démarrage
```bat
cd AssurAuto-Microservices
scripts\build-all.bat                 :: mvn clean install (compile + tests unitaires)
scripts\start-all.bat                 :: démarre les 7 services dans l'ordre, attend chaque "UP"
```
Bases réelles (optionnel) : `scripts\databases-up.bat` puis `scripts\start-all.bat sql`.
Sans argument, chaque service utilise **sa propre base H2 fichier** (dossier `data/` du service) : aucune installation de base requise.

Un service seul : `scripts\start-service.bat client-service` — ou en ligne de commande `cd client-service && mvn spring-boot:run`.
Arrêt : `scripts\stop-all.bat`.

**IDE** : IntelliJ / Eclipse / VS Code → importer le `pom.xml` racine (ou chaque module) comme projet Maven, lancer les classes `*Application` dans l'ordre ci-dessus.

**Plusieurs instances (load balancing)** : `scripts\start-service.bat client-service 8091` puis appeler plusieurs fois
`http://localhost:9999/client-service/api/info` : le champ `port` alterne.

## 4. URL utiles
| URL | Contenu |
|---|---|
| http://localhost:8761 | Dashboard Eureka (les 5 services + proxy doivent être UP) |
| http://localhost:8888/client-service/default | Config centralisée (JSON) ; idem `contrat-service`, `vehicule-service`, `sinistre-service`, `proxy-service` |
| http://localhost:8081/swagger-ui.html | Swagger client-service (8082 contrat, 8083 véhicule, 8084 sinistre) |
| http://localhost:8081/actuator | Actuator (health/info publics, le reste ADMIN) |
| http://localhost:9999/{service}/api/... | Accès via Gateway, ex. `http://localhost:9999/client-service/api/clients` |

Swagger : utiliser directement le port de chaque service (le bouton *Authorize* accepte `admin/admin123` ou `agent/agent123`).

## 5. Sécurité
HTTP Basic sans état sur chaque service métier. Comptes (modifiables dans `config/<service>.properties`) :
| Compte | Mot de passe | Droits |
|---|---|---|
| `agent` | `agent123` | GET / POST / PUT / PATCH sur `/api/**` ; numéro de pièce d'identité **masqué** |
| `admin` | `admin123` | tout, dont `DELETE`, endpoints Actuator sensibles, pièce d'identité en clair |

Public : Swagger, `/actuator/health`, `/actuator/info`, `/api/info`. Les appels entre services utilisent le compte technique `assurauto.internal.*` (=`agent`).
Validation Bean Validation sur tous les corps de requête ; erreurs JSON homogènes (`ApiError`) : 400 validation, 401/403 sécurité, 404, 409 doublon, 422 règle métier, 503 service distant indisponible.

## 6. Exemples de requêtes (`curl.exe` sous Windows ; fichier complet : `tests/assurauto.http`)
```bat
curl.exe -u agent:agent123 -H "Content-Type: application/json" -X POST http://localhost:9999/client-service/api/clients ^
 -d "{\"nom\":\"Kabre\",\"prenom\":\"Awa\",\"dateNaissance\":\"1990-05-01\",\"genre\":\"FEMININ\",\"adresse\":{\"rue\":\"12 rue du Marche\",\"ville\":\"Ouagadougou\",\"pays\":\"Burkina Faso\"},\"email\":\"awa@mail.com\",\"telephones\":[\"+22670000000\"],\"numeroPieceIdentite\":\"CNIB123456\",\"permisConduire\":{\"numero\":\"P-001\",\"categorie\":\"B\",\"dateObtention\":\"2012-01-15\"},\"situationFamiliale\":\"MARIE\",\"profession\":\"Ingenieure\"}"

curl.exe -u agent:agent123 http://localhost:9999/client-service/api/clients?ville=Ouagadougou
curl.exe -u agent:agent123 http://localhost:9999/contrat-service/api/contrats?clientId=1
curl.exe -u agent:agent123 http://localhost:9999/sinistre-service/api/sinistres?vehiculeId=1
curl.exe -u admin:admin123 -X POST http://localhost:8081/actuator/refresh
```
Enchaînement métier : client → contrat (vérifie le client) → véhicule (vérifie le contrat) → sinistre (vérifie contrat, période de couverture et appartenance du véhicule).

### Endpoints principaux
| Service | Endpoints |
|---|---|
| client | `POST/GET /api/clients`, `GET/PUT/DELETE /api/clients/{id}`, `GET/POST /api/clients/{id}/historique` |
| contrat | `POST/GET /api/contrats?clientId=&statut=`, `GET/PUT/DELETE /{id}`, `POST /{id}/suspendre\|reactiver\|resilier\|renouveler` |
| véhicule | `POST/GET /api/vehicules?contratId=&marque=&immatriculation=`, `GET/PUT/DELETE /{id}`, `PATCH /{id}/kilometrage` |
| sinistre | `POST/GET /api/sinistres?contratId=&vehiculeId=&statut=`, `GET/PUT/DELETE /{id}`, `POST /{id}/statut`, `GET /{id}/historique` |

## 7. Tests
* **Unitaires** (JUnit 5 + Mockito, lancés par `mvn test` / `build-all.bat`) : règles du domaine et services de chaque microservice métier, communications inter-services simulées.
* **Intégration bout en bout** (tous les services lancés) : `powershell -ExecutionPolicy Bypass -File tests\smoke-test.ps1` — vérifie Config Server, Eureka, Gateway, Swagger, Actuator, sécurité, CRUD, relations entre services, règles métier et gestion d'erreurs.

## 8. Choix de conception (DDD)
* 1 microservice = 1 bounded context = 1 base : **Relation client**, **Gestion contractuelle**, **Biens assurés**, **Gestion des incidents**. Références entre contextes **par identifiant uniquement** (`clientId`, `contratId`, `vehiculeId`), jamais d'accès à la base d'un autre service ; chaque appel REST passe par une *gateway* (anti-corruption layer) dans `infrastructure.client`.
* Packages : `domain.model` (agrégats : Client, Contrat, Vehicule, Sinistre), `domain.valueobject` (Adresse, PermisConduire, Garantie, Franchise, EquipementsSecurite, SuiviTraitement…), `domain.repository`, `domain.exception`, `application.service`, `application.mapper`, `api.controller|dto|exception`, `infrastructure.*`.
* Règles métier dans les agrégats : score de fidélité → catégorie, cycle de vie du contrat, garanties/franchises cohérentes, score de risque du véhicule, workflow de statut du sinistre.
* **CQRS** : appliqué « léger » (méthodes de commande / requête séparées dans les services applicatifs). **Event Sourcing** : volontairement non implémenté (pas nécessaire) ; l'historique des sinistres et l'historique commercial sont conservés comme journal d'étapes.
* Choix d'implémentation hors sujet officiel (sinistre) : dates, description, type d'incident, lieu, statut, montants estimé/indemnisé, historique de traitement.

## 9. Limites connues
* Supprimer un client/contrat ne vérifie pas les enregistrements dépendants des autres services (pas de saga) ; un contrat résilié n'est pas propagé automatiquement aux véhicules.
* Mots de passe en clair dans la config (usage pédagogique) ; en production : secrets chiffrés + OAuth2/JWT.
* Le Swagger « Try it out » est prévu pour un accès direct aux services (pas via le préfixe de la Gateway).
* Ordre/attente : les services métier démarrent même si un autre est absent ; les appels dépendants renvoient alors **503**.
