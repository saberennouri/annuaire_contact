# Annuaire scolaire Sfax 2

Annuaire des collèges, lycées, écoles primaires (publiques et privées) et circonscriptions d'inspection de Sfax 2,
construit à partir de deux documents PDF (agenda secondaire du 23/10/2026, annuaire primaire du 10/09/2026).

| Couche | Technologie |
|---|---|
| Front | Angular (composants standalone, signals), arabe/français, mode sombre |
| API | Spring Boot 3.3, Java 21, Spring Data JPA, Spring Security, Actuator, springdoc (Swagger) |
| Base | PostgreSQL 16 (prod) ou H2 en mémoire (dev et tests) |
| Conteneurs | Dockerfiles multi-stage, nginx (fichiers statiques et reverse proxy), Docker Compose |
| CI | GitHub Actions : tests back, build front, images poussées sur GHCR |

## Lancer le projet

### 1. Tout en conteneurs (PostgreSQL + API + front)
```bash
cp .env.example .env        # puis changer les deux mots de passe
docker compose up --build
```
Ouvrir http://localhost:8080 (Swagger : http://localhost:8080/swagger-ui.html).
Les 296 fiches sont chargées au premier démarrage si la base est vide.

### 2. Développement (sans Docker)
```bash
cd backend && mvn spring-boot:run          # API sur :8080, base H2 en mémoire
cd frontend && npm ci && npm start         # front sur :4200, /api proxifié vers :8080
```

### 3. Front seul (sans Java)
```bash
node tools/mock-api.mjs                    # API factice en lecture seule sur :8080
cd frontend && npm ci && npm start
```

## API

| Méthode | Route | Rôle |
|---|---|---|
| GET | `/api/establishments?category=&kind=&delegation=&q=&ids=&page=&size=` | Recherche paginée (`size` ≤ 200) |
| GET | `/api/establishments/counts?q=` | Nombre de résultats par catégorie |
| GET | `/api/establishments/facets?category=&q=` | Effectifs par type et par délégation |
| GET | `/api/establishments/{id}` | Une fiche |
| POST, PUT, DELETE | `/api/establishments[/{id}]` | Administration (HTTP Basic, rôle ADMIN) |

`category` : `SECONDARY`, `PRIMARY`, `PRIVATE_PRIMARY`, `PRESCHOOL`, `INSPECTION` (plusieurs valeurs séparées par des virgules).
La recherche ignore les diacritiques et unifie les variantes de lettres arabes (أ إ آ → ا, ة → ه, ى → ي).
Un numéro saisi avec espaces (`97 362 117`) est reconnu.

Exemple d'écriture :
```bash
curl -u admin:VOTRE_MOT_DE_PASSE -H 'Content-Type: application/json' \
  -d '{"category":"PRIMARY","delegation":"قرقنة","name":"مدرسة جديدة","phones":[],"contacts":[]}' \
  http://localhost:8080/api/establishments
```

## Tests
```bash
cd backend && mvn verify      # normalisation arabe, comptes du seed (63/192/14/2/25), filtres, sécurité
```

## Points d'attention
- Le jeu de données a été transcrit depuis les PDF ; les numéros et e-mails ont été recoupés avec leur couche texte,
  les noms arabes relus sur image. Un numéro de Kerkennah n'a que 7 chiffres dans la source (`5461188`), il est conservé tel quel.
- `ddl-auto: update` convient pour démarrer ; passer à Flyway avant une vraie mise en production.
- Les mots de passe viennent de l'environnement (`.env`), jamais du dépôt.

## Pistes DevOps pour la suite
1. Migrations Flyway et tests d'intégration avec Testcontainers (PostgreSQL réel).
2. Déploiement Kubernetes (Deployment, Service, Ingress, Secrets) ou sur un VPS avec Compose et Traefik/Caddy (HTTPS).
3. Observabilité : Micrometer, Prometheus, Grafana ; logs structurés.
4. Analyse de qualité et de sécurité dans la CI (SonarQube, Trivy sur les images, Dependabot).
5. Infrastructure as code (Terraform) et déploiement continu depuis GHCR.
