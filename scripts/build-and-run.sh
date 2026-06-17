#!/bin/bash
# =============================================================================
# scripts/build-and-run.sh
# One-shot script: builds all JARs, Docker images, starts all containers.
# Run from the PROJECT ROOT (where docker-compose.yml lives).
# =============================================================================

set -e
set -o pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC}  $1"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ── Config ────────────────────────────────────────────────────────────────────
# This script lives in scripts/ — project root is one level up
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

API_GATEWAY_DIR="$PROJECT_ROOT/api-gateway"
AUTH_SERVICE_DIR="$PROJECT_ROOT/auth-service"
QUESTION_SERVICE_DIR="$PROJECT_ROOT/question-service"
QUIZ_SERVICE_DIR="$PROJECT_ROOT/quiz-service"
NOTIFICATION_SERVICE_DIR="$PROJECT_ROOT/notification-service"
FRONTEND_DIR="$PROJECT_ROOT/quiz_angular_ui"

# =============================================================================
# STEP 1: Prerequisites
# =============================================================================
log_info "Checking prerequisites..."

command -v docker >/dev/null 2>&1 || log_error "Docker not installed"
command -v mvn    >/dev/null 2>&1 || log_error "Maven not installed"
command -v node   >/dev/null 2>&1 || log_error "Node.js not installed"
command -v npm    >/dev/null 2>&1 || log_error "npm not installed"

[ -f "$PROJECT_ROOT/.env" ] || log_error ".env not found in $PROJECT_ROOT. Copy .env.example → .env and fill in values."

log_success "Prerequisites met"

# =============================================================================
# STEP 2: Build backend JARs
# =============================================================================
log_info "Building backend JARs..."

build_jar() {
  local NAME="$1"
  local DIR="$2"
  if [ ! -d "$DIR" ]; then
    log_warn "Directory not found: $DIR — skipping $NAME"
    return
  fi
  log_info "Building $NAME..."
  cd "$DIR"
  mvn clean package -DskipTests -B -q
  log_success "$NAME JAR built"
  cd "$PROJECT_ROOT"
}

build_jar "api-gateway"          "$API_GATEWAY_DIR"
build_jar "auth-service"         "$AUTH_SERVICE_DIR"
build_jar "question-service"     "$QUESTION_SERVICE_DIR"
build_jar "quiz-service"         "$QUIZ_SERVICE_DIR"
build_jar "notification-service" "$NOTIFICATION_SERVICE_DIR"

# =============================================================================
# STEP 3: Build Angular frontend
# =============================================================================
log_info "Building Angular frontend..."

if [ -d "$FRONTEND_DIR" ]; then
  cd "$FRONTEND_DIR"
  npm ci --prefer-offline
  npm run build -- --configuration production
  log_success "Angular build complete"
  cd "$PROJECT_ROOT"
else
  log_warn "Frontend directory not found: $FRONTEND_DIR"
fi

# =============================================================================
# STEP 4: Build Docker images
# =============================================================================
log_info "Building Docker images..."
cd "$PROJECT_ROOT"
docker compose build --no-cache
log_success "All Docker images built"

# =============================================================================
# STEP 5: Start containers
# =============================================================================
log_info "Starting all containers..."
docker compose up -d
log_success "All containers started"

# =============================================================================
# STEP 6: Status
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
echo "  Frontend:             http://localhost"
echo "  API Gateway:          http://localhost:8080/actuator/health"
echo "  Auth Service:         http://localhost:8081/actuator/health"
echo "  Question Service:     http://localhost:8082/actuator/health"
echo "  Quiz Service:         http://localhost:8083/actuator/health"
echo "  Notification Service: http://localhost:8084/actuator/health"
echo "════════════════════════════════════════════════════════"
echo ""
log_success "Deployment complete!"