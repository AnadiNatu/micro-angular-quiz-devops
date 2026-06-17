#!/bin/bash
# =============================================================================
# scripts/build-and-run.sh
# One-shot script to build all JARs, Docker images, and start all containers
# Run from the DEVOPS root folder (where docker-compose.yml lives)
# =============================================================================

set -e   # exit on any error
set -o pipefail

# ── Colors ────────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info()    { echo -e "${BLUE}[INFO]${NC}  $1"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ── Config ────────────────────────────────────────────────────────────────────
DEVOPS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_ROOT="$(dirname "$DEVOPS_DIR")"   # assumes devops/ is inside project root

# Update these paths to match your actual project structure
API_GATEWAY_DIR="$PROJECT_ROOT/api-gateway"
AUTH_SERVICE_DIR="$PROJECT_ROOT/auth-service"
QUESTION_SERVICE_DIR="$PROJECT_ROOT/question-service"
QUIZ_SERVICE_DIR="$PROJECT_ROOT/quiz-service"
NOTIFICATION_SERVICE_DIR="$PROJECT_ROOT/notification-service"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

# =============================================================================
# STEP 1: Check prerequisites
# =============================================================================
log_info "Checking prerequisites..."

command -v docker   >/dev/null 2>&1 || log_error "Docker not installed"
command -v mvn      >/dev/null 2>&1 || log_error "Maven not installed. Install via: sudo apt install maven"
command -v node     >/dev/null 2>&1 || log_error "Node.js not installed"
command -v npm      >/dev/null 2>&1 || log_error "npm not installed"

# Check .env file exists
if [ ! -f "$DEVOPS_DIR/.env" ]; then
  log_error ".env file not found in $DEVOPS_DIR. Copy .env.example to .env and fill in values."
fi

log_success "All prerequisites met"

# =============================================================================
# STEP 2: Build backend JARs with Maven
# =============================================================================
log_info "Building backend JARs..."

build_jar() {
  local SERVICE_NAME="$1"
  local SERVICE_DIR="$2"

  if [ ! -d "$SERVICE_DIR" ]; then
    log_warn "Directory not found: $SERVICE_DIR — skipping $SERVICE_NAME"
    return
  fi

  log_info "Building $SERVICE_NAME..."
  cd "$SERVICE_DIR"
  mvn clean package -DskipTests -B -q
  log_success "$SERVICE_NAME JAR built"
  cd "$DEVOPS_DIR"
}

build_jar "api-gateway"          "$API_GATEWAY_DIR"
build_jar "auth-service"         "$AUTH_SERVICE_DIR"
build_jar "question-service"     "$QUESTION_SERVICE_DIR"
build_jar "quiz-service"         "$QUIZ_SERVICE_DIR"
build_jar "notification-service" "$NOTIFICATION_SERVICE_DIR"

# =============================================================================
# STEP 3: Copy JARs into devops Dockerfile context folders
# =============================================================================
log_info "Copying JARs to Dockerfile context folders..."

# NOTE: The Dockerfiles use multi-stage build so they run Maven inside Docker.
# This step is only needed if you want to use pre-built JARs instead.
# The default Dockerfiles BUILD INSIDE DOCKER — so this step is OPTIONAL.
# Uncomment below only if you switch to COPY-only Dockerfiles:

# cp "$API_GATEWAY_DIR/target/api-gateway.jar"             "$DEVOPS_DIR/api-gateway/app.jar"
# cp "$AUTH_SERVICE_DIR/target/auth-service-quiz.jar"      "$DEVOPS_DIR/auth-service/app.jar"
# cp "$QUESTION_SERVICE_DIR/target/question-service.jar"   "$DEVOPS_DIR/question-service/app.jar"
# cp "$QUIZ_SERVICE_DIR/target/quiz-service.jar"           "$DEVOPS_DIR/quiz-service/app.jar"
# cp "$NOTIFICATION_SERVICE_DIR/target/notification-service.jar" "$DEVOPS_DIR/notification-service/app.jar"

log_success "JAR copy step skipped (multi-stage Docker builds handle this)"

# =============================================================================
# STEP 4: Build Angular frontend
# =============================================================================
log_info "Building Angular frontend..."

if [ -d "$FRONTEND_DIR" ]; then
  cd "$FRONTEND_DIR"
  npm ci --prefer-offline
  npm run build -- --configuration production
  log_success "Angular build complete"
  cd "$DEVOPS_DIR"
else
  log_warn "Frontend directory not found: $FRONTEND_DIR"
fi

# =============================================================================
# STEP 5: Build Docker images
# =============================================================================
log_info "Building Docker images..."

cd "$DEVOPS_DIR"

docker compose build --no-cache \
  --build-arg BUILDKIT_INLINE_CACHE=1

log_success "All Docker images built"

# =============================================================================
# STEP 6: Start all containers
# =============================================================================
log_info "Starting all containers..."

docker compose up -d

log_success "All containers started"

# =============================================================================
# STEP 7: Wait and show status
# =============================================================================
log_info "Waiting 30s for services to become healthy..."
sleep 30

echo ""
echo "════════════════════════════════════════════════════════"
echo "  Container Status"
echo "════════════════════════════════════════════════════════"
docker compose ps
echo ""
echo "════════════════════════════════════════════════════════"
echo "  Access Points"
echo "════════════════════════════════════════════════════════"
echo "  Frontend:            http://localhost"
echo "  API Gateway:         http://localhost:8080"
echo "  Auth Service:        http://localhost:8081/actuator/health"
echo "  Question Service:    http://localhost:8082/actuator/health"
echo "  Quiz Service:        http://localhost:8083/actuator/health"
echo "  Notification Service: http://localhost:8084/actuator/health"
echo "════════════════════════════════════════════════════════"
echo ""
log_success "Deployment complete!"
