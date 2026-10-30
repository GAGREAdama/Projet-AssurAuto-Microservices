# Bases de données

| Service | Moteur | Port hôte | Base | Utilisateur / mot de passe |
|---|---|---|---|---|
| client-service | MySQL 8 | 3307 | client_db | assurauto / assurauto |
| contrat-service | PostgreSQL 16 | 5433 | contrat_db | assurauto / assurauto |
| vehicule-service | MariaDB 11 | 3308 | vehicule_db | assurauto / assurauto |
| sinistre-service | MySQL 8 | 3309 | sinistre_db | assurauto / assurauto |

* **Par défaut (sans rien installer)** : chaque service utilise sa propre base **H2 fichier** (`<service>/data/*`).
* **Bases réelles** : `docker compose up -d` dans ce dossier (ou scripts\databases-up.bat), puis démarrer les services avec le profil `sql`.
* Sans Docker : installer les moteurs, puis exécuter le `init.sql` de chaque dossier (ports à adapter dans `config-service/src/main/resources/config/<service>-sql.properties`).
* Les tables sont créées automatiquement par Hibernate (`ddl-auto=update`).
