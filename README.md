# ================================================================
# QUIZ PLATFORM — COMPLETE DEVOPS GUIDE
# ================================================================
# Services: api-gateway | auth-service | question-service |
#           quiz-service | notification-service | frontend (Angular)
# Database: PostgreSQL 16 (Docker volume or AWS RDS)
# Stack:    Spring Boot 3 + Angular 17 + Nginx + Docker Compose
# ================================================================

---

## TABLE OF CONTENTS

1. [Project Structure](#1-project-structure)
2. [.env File Guide — What Goes Where](#2-env-file-guide--what-goes-where)
3. [Application Properties — What Goes Where](#3-application-properties--what-goes-where)
4. [Fix Required Before Dockerizing](#4-fix-required-before-dockerizing)
5. [Local Docker Setup — Step by Step](#5-local-docker-setup--step-by-step)
6. [AWS EC2 + RDS Production Setup](#6-aws-ec2--rds-production-setup)
7. [Docker Commands Reference](#7-docker-commands-reference)
8. [Database Persistence — How It Works](#8-database-persistence--how-it-works)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. PROJECT STRUCTURE

```
your-project-root/
│
├── api-gateway/                    ← Spring Boot project
│   ├── src/main/resources/
│   │   ├── application.properties          ← base config
│   │   ├── application-local.properties    ← local run
│   │   ├── application-docker.properties   ← docker run
│   │   └── application-prod.properties     ← AWS prod
│   ├── pom.xml
│   └── Dockerfile                  ← PLACE HERE (copy from devops/api-gateway/)
│
├── auth-service/                   ← Spring Boot project
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-local.properties
│   │   ├── application-docker.properties
│   │   └── application-prod.properties
│   ├── pom.xml
│   └── Dockerfile                  ← PLACE HERE
│
├── question-service/               ← Spring Boot project
│   ├── src/main/resources/
│   │   ├── application-local.properties
│   │   ├── application-docker.properties
│   │   └── application-prod.properties
│   └── Dockerfile                  ← PLACE HERE
│
├── quiz-service/                   ← Spring Boot project
│   ├── src/main/resources/
│   │   ├── application-local.properties
│   │   ├── application-docker.properties
│   │   └── application-prod.properties
│   └── Dockerfile                  ← PLACE HERE
│
├── notification-service/           ← Spring Boot project
│   ├── src/main/resources/
│   │   ├── application-local.properties
│   │   ├── application-docker.properties
│   │   └── application-prod.properties
│   └── Dockerfile                  ← PLACE HERE
│
├── frontend/                       ← Angular project
│   ├── src/environments/
│   │   ├── environment.ts              ← local (copy from devops/frontend/)
│   │   └── environment.production.ts  ← prod (copy from devops/frontend/)
│   ├── proxy.conf.json             ← PLACE HERE (copy from devops/frontend/)
│   ├── nginx.conf                  ← PLACE HERE (copy from devops/frontend/)
│   ├── Dockerfile                  ← PLACE HERE (copy from devops/frontend/)
│   ├── package.json
│   └── angular.json
│
└── devops/                         ← THIS FOLDER (all files generated here)
    ├── .env                        ← SECRETS (never commit)
    ├── .env.example                ← placeholder (safe to commit)
    ├── .env.prod                   ← AWS secrets (never commit)
    ├── .gitignore
    ├── docker-compose.yml          ← main compose
    ├── docker-compose.prod.yml     ← AWS prod override
    ├── postgres-init/
    │   └── 01-init-databases.sh    ← creates DBs on first run
    └── scripts/
        ├── build-and-run.sh        ← one-shot local setup
        ├── deploy-ec2.sh           ← EC2 deployment
        └── manage.sh               ← day-to-day management
```

---

## 2. .ENV FILE GUIDE — WHAT GOES WHERE

### File Locations

```
devops/
├── .env             ← LOCAL DOCKER secrets (NEVER commit to Git)
├── .env.example     ← Placeholder template (COMMIT this to Git)
└── .env.prod        ← AWS PRODUCTION secrets (NEVER commit to Git)
```

### What Each File Contains

| File | Secrets? | Commit? | Used When |
|------|----------|---------|-----------|
| `.env.example` | NO — placeholders only | ✅ YES | Developers copy this to start |
| `.env` | YES — real local values | ❌ NO | `docker compose up` locally |
| `.env.prod` | YES — real AWS values | ❌ NO | `docker compose --env-file .env.prod up` on EC2 |

### .env vs .env.prod Key Differences

| Variable | .env (local) | .env.prod (AWS) |
|----------|--------------|-----------------|
| `POSTGRES_HOST` | `postgres` (docker service) | `your-rds.xxxx.rds.amazonaws.com` |
| `SPRING_PROFILES_ACTIVE` | `docker` | `prod` |
| `POSTGRES_USER/PASSWORD` | local root user | NOT USED (RDS manages this) |
| `DB_PASSWORDS` | dev passwords | strong prod passwords |
| `JWT_SECRET` | dev secret | strong prod secret (32+ chars) |

---

## 3. APPLICATION PROPERTIES — WHAT GOES WHERE

### File Naming Convention
Spring Boot auto-loads `application-{profile}.properties` based on `SPRING_PROFILES_ACTIVE`.

### Property File Placement
Copy from `devops/<service-name>/` into `<service-name>/src/main/resources/`:

```
devops/auth-service/application-local.properties
    → auth-service/src/main/resources/application-local.properties

devops/auth-service/application-docker.properties
    → auth-service/src/main/resources/application-docker.properties

devops/auth-service/application-prod.properties
    → auth-service/src/main/resources/application-prod.properties
```
Repeat for ALL 5 services.

### What Each Profile File Contains

| Property | local | docker | prod |
|----------|-------|--------|------|
| DB host | `localhost` | `postgres` (container) | RDS endpoint |
| DB credentials | hardcoded | `${ENV_VAR}` | `${ENV_VAR}` |
| JWT secret | hardcoded | `${JWT_SECRET}` | `${JWT_SECRET}` |
| ddl-auto | `update` | `update` | `update` |
| show-sql | `true` | `false` | `false` |
| Feign URLs | `localhost:PORT` | `service-name:PORT` | `service-name:PORT` |
| Actuator | all exposed | all exposed | health only |
| Log level | DEBUG | DEBUG | WARN |
| SSL (DB) | none | none | `?sslmode=require` |

---

## 4. FIX REQUIRED BEFORE DOCKERIZING

### ⚠️ CRITICAL: PostgreSQL vs MySQL syntax fix

Your `QuestionRepository.java` uses MySQL syntax for random selection:
```java
// ❌ WRONG — MySQL only
@Query("SELECT q FROM Question q WHERE q.category = :category ORDER BY RAND() LIMIT :numQ")

// ✅ CORRECT — PostgreSQL
@Query(value = "SELECT * FROM questions WHERE category = :category ORDER BY RANDOM() LIMIT :numQ", nativeQuery = true)
```

### Fix Angular API URL (IMPORTANT)

In your Angular services, change any hardcoded `localhost:8080` to use the environment:

```typescript
// ❌ WRONG — hardcoded
private apiUrl = 'http://localhost:8080/api/auth';

// ✅ CORRECT — uses environment
import { environment } from '../../environments/environment';
private apiUrl = `${environment.apiBaseUrl}/api/auth`;
```

The `environment.production.ts` has `apiBaseUrl: ''` (empty string),
so in production `${environment.apiBaseUrl}/api/auth` becomes `/api/auth`
which Nginx proxies to `http://api-gateway:8080/api/auth`.

### Fix angular.json to use proxy + environments

Add this to your `angular.json` under `projects > architect > serve > configurations > development`:
```json
"proxyConfig": "proxy.conf.json"
```

Add production file replacements:
```json
"fileReplacements": [
  {
    "replace": "src/environments/environment.ts",
    "with": "src/environments/environment.production.ts"
  }
]
```

### Fix pom.xml finalName (so Dockerfile knows JAR name)

Add `<finalName>` in each service's `pom.xml` under `<build>`:
```xml
<build>
    <finalName>api-gateway</finalName>         <!-- for api-gateway -->
    <finalName>auth-service-quiz</finalName>   <!-- for auth-service -->
    <finalName>question-service</finalName>    <!-- for question-service -->
    <finalName>quiz-service</finalName>        <!-- for quiz-service -->
    <finalName>notification-service</finalName><!-- for notification-service -->
</build>
```

---

## 5. LOCAL DOCKER SETUP — STEP BY STEP

### Prerequisites
- Docker Desktop installed and running
- Java 21 + Maven installed
- Node.js 20 + npm installed

### Step 1: Copy All Generated Files to Your Projects

```bash
# Copy Dockerfiles into each backend service root
cp devops/api-gateway/Dockerfile          api-gateway/Dockerfile
cp devops/auth-service/Dockerfile         auth-service/Dockerfile
cp devops/question-service/Dockerfile     question-service/Dockerfile
cp devops/quiz-service/Dockerfile         quiz-service/Dockerfile
cp devops/notification-service/Dockerfile notification-service/Dockerfile

# Copy application properties into each service
cp devops/api-gateway/application-local.properties   api-gateway/src/main/resources/
cp devops/api-gateway/application-docker.properties  api-gateway/src/main/resources/
cp devops/api-gateway/application-prod.properties    api-gateway/src/main/resources/
# Repeat for auth-service, question-service, quiz-service, notification-service

# Copy frontend files
cp devops/frontend/Dockerfile             frontend/Dockerfile
cp devops/frontend/nginx.conf             frontend/nginx.conf
cp devops/frontend/proxy.conf.json        frontend/proxy.conf.json
cp devops/frontend/environment.ts         frontend/src/environments/environment.ts
cp devops/frontend/environment.production.ts  frontend/src/environments/environment.production.ts
```

### Step 2: Create Your .env File

```bash
cd devops/
cp .env.example .env
# Now EDIT .env with your real values (DB passwords, JWT secret, mail, OpenAI key)
nano .env
```

### Step 3: Apply the RAND() → RANDOM() Fix

Edit `question-service/src/main/java/.../QuestionRepository.java`
Change `RAND()` → `RANDOM()` and add `nativeQuery = true`.

### Step 4: Apply the Angular API URL Fix

Edit each Angular service file to use `environment.apiBaseUrl` instead of hardcoded `localhost:8080`.

### Step 5: Add finalName to Each pom.xml

Edit each backend service's `pom.xml` and add `<finalName>` as shown in Section 4.

### Step 6: Build and Start Everything

```bash
cd devops/

# Option A: One-shot script (recommended)
chmod +x scripts/build-and-run.sh
./scripts/build-and-run.sh

# Option B: Manual step-by-step
# Build images
docker compose build

# Start all containers
docker compose up -d

# Watch logs
docker compose logs -f
```

### Step 7: Verify Everything is Running

```bash
# Check container status
docker compose ps

# Check health
curl http://localhost:8080/actuator/health   # API Gateway
curl http://localhost:8081/actuator/health   # Auth
curl http://localhost:8082/actuator/health   # Question
curl http://localhost:8083/actuator/health   # Quiz
curl http://localhost:8084/actuator/health   # Notification

# Access the app
open http://localhost
```

---

## 6. AWS EC2 + RDS PRODUCTION SETUP

### 6A: AWS RDS Setup (do this first)

1. Go to AWS Console → RDS → Create Database
2. Choose: PostgreSQL 16, Free Tier (or production tier)
3. DB identifier: `quiz-platform-db`
4. Master username: `postgres`
5. Set master password (save it)
6. VPC: same VPC as your EC2 instance
7. Public access: NO (EC2 will connect privately)
8. Create the database

After RDS is created:
```sql
-- Connect to RDS from EC2 and create app databases + users
psql -h your-rds-endpoint.rds.amazonaws.com -U postgres

CREATE USER auth_user WITH PASSWORD 'your-strong-password';
CREATE DATABASE auth_db OWNER auth_user;
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;

CREATE USER question_user WITH PASSWORD 'your-strong-password';
CREATE DATABASE question_db OWNER question_user;
GRANT ALL PRIVILEGES ON DATABASE question_db TO question_user;

CREATE USER quiz_user WITH PASSWORD 'your-strong-password';
CREATE DATABASE quiz_db OWNER quiz_user;
GRANT ALL PRIVILEGES ON DATABASE quiz_db TO quiz_user;
```

### 6B: EC2 Setup

1. Launch EC2: Amazon Linux 2023, t3.medium (recommended), 20GB EBS
2. Security Group — INBOUND rules:
   - Port 22  (SSH) — your IP only
   - Port 80  (HTTP) — 0.0.0.0/0
   - Port 443 (HTTPS) — 0.0.0.0/0 (if using SSL)
   - Port 8080 (optional for API debugging) — your IP only

3. RDS Security Group — INBOUND rules:
   - Port 5432 from EC2 security group ID (not IP — so it auto-updates)

### 6C: Upload Files to EC2

```bash
# From your local machine:

# 1. Upload the devops folder
scp -r -i your-key.pem ./devops ec2-user@<EC2-IP>:/home/ec2-user/quiz-app/

# 2. Upload .env.prod SEPARATELY (never put in git)
scp -i your-key.pem ./devops/.env.prod ec2-user@<EC2-IP>:/home/ec2-user/quiz-app/devops/
```

### 6D: Deploy on EC2

```bash
# SSH into EC2
ssh -i your-key.pem ec2-user@<EC2-IP>

# Go to devops folder
cd /home/ec2-user/quiz-app/devops

# Edit .env.prod with your RDS endpoint
nano .env.prod

# Run deploy script
chmod +x scripts/deploy-ec2.sh
./scripts/deploy-ec2.sh
```

### 6E: AWS S3 for File Uploads (if applicable)

1. Create S3 bucket in ap-south-1
2. Set bucket policy for your app's IAM user
3. Use IAM Role on EC2 (preferred over access keys):
   - Create IAM Role with S3 permissions
   - Attach to EC2 instance
   - Remove `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` from `.env.prod`
   - Spring's AWS SDK auto-detects the role

---

## 7. DOCKER COMMANDS REFERENCE

### Sequential Commands (copy-paste ready)

```bash
# ── STEP 1: Build all images ──────────────────────────────────────
docker compose build

# Build single image (faster for one change)
docker compose build auth-service

# Build without cache (fresh build)
docker compose build --no-cache

# ── STEP 2: Start all containers ──────────────────────────────────
docker compose up -d

# Start and watch logs
docker compose up

# Start single service
docker compose up -d auth-service

# ── STEP 3: Verify ────────────────────────────────────────────────
docker compose ps
docker compose logs -f
docker compose logs -f auth-service   # single service

# ── STEP 4: Stop (DATA IS SAFE) ───────────────────────────────────
docker compose stop

# ── STEP 5: Start again ───────────────────────────────────────────
docker compose start

# ── Other useful commands ─────────────────────────────────────────

# Remove containers (keeps volumes/data)
docker compose down

# Remove containers + images (keeps volumes/data)
docker compose down --rmi local

# Remove EVERYTHING including data (⚠ DANGER — deletes database!)
docker compose down -v

# See resource usage
docker stats

# Enter a running container shell
docker exec -it quiz-auth-service sh
docker exec -it quiz-postgres psql -U postgres

# View environment variables in a container
docker exec quiz-auth-service env | grep -v PASSWORD

# Check image sizes
docker images | grep quiz

# ── Production commands ───────────────────────────────────────────

# Start prod (on EC2)
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d

# Rebuild and redeploy single service (zero-downtime for others)
docker compose build auth-service
docker compose up -d --no-deps auth-service
```

### Management Script Shortcuts

```bash
# All management via the manage.sh script:
./scripts/manage.sh status
./scripts/manage.sh logs auth-service
./scripts/manage.sh restart quiz-service
./scripts/manage.sh stop
./scripts/manage.sh db-shell
./scripts/manage.sh db-backup

# Production variants
./scripts/manage.sh status --prod
./scripts/manage.sh logs --prod
```

---

## 8. DATABASE PERSISTENCE — HOW IT WORKS

### Local Docker
```yaml
volumes:
  postgres-data:          # Named Docker volume
    driver: local
    name: quiz-postgres-data
```
- Data lives in: `~/.docker/volumes/quiz-postgres-data/`
- **Survives**: `docker compose stop`, `docker compose down`, machine restart
- **Destroyed by**: `docker compose down -v` (never run this in prod)

### AWS EC2
- Same named volume, stored on EBS disk
- **Survives**: EC2 stop/start, container restart
- **Protected by**: EBS automatic backups (enable in AWS console)

### AWS RDS (Production recommended)
- Fully managed by AWS
- **Survives**: everything automatically
- **Backups**: automatic daily snapshots (configure retention)
- **Multi-AZ**: enable for high availability

### Backup Command
```bash
# Backup all databases to ./backups/
./scripts/manage.sh db-backup

# Restore a backup
./scripts/manage.sh db-restore auth_db ./backups/2024-01-15_10-30-00/auth_db.sql
```

---

## 9. TROUBLESHOOTING

### Container won't start
```bash
docker compose logs auth-service
# Look for: connection refused, port in use, missing env var
```

### DB connection refused
```bash
# Check postgres is healthy
docker compose ps postgres
# Should show: healthy

# Check env vars are set in container
docker exec quiz-auth-service env | grep POSTGRES
```

### Angular shows blank page
```bash
# Check nginx is serving files
docker exec -it quiz-frontend ls /usr/share/nginx/html
# Should show: index.html, main.js, styles.css etc.

# Check nginx config
docker exec quiz-frontend nginx -t
```

### API calls fail from frontend
```bash
# Check nginx proxy is routing correctly
docker exec -it quiz-frontend cat /etc/nginx/conf.d/default.conf
# Verify proxy_pass points to http://api-gateway:8080
```

### Port already in use
```bash
# Find what's using port 8080
sudo lsof -i :8080
# Kill it or change the port in docker-compose.yml
```

### Out of disk space (EC2)
```bash
# Check disk
df -h
# Clean unused Docker images
docker system prune -f
# Clean old volumes (careful!)
docker volume prune -f
```
