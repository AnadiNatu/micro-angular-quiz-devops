#!/bin/bash
# =============================================================================
# scripts/deploy-ec2.sh
# Run this ON your EC2 instance after uploading the project folder.
#
# Upload project to EC2 first:
#   scp -r -i your-key.pem ./ ec2-user@<EC2-IP>:/home/ec2-user/quiz-app/
#   scp -i your-key.pem .env.prod ec2-user@<EC2-IP>:/home/ec2-user/quiz-app/
#
# Then SSH in and run:
#   cd /home/ec2-user/quiz-app
#   chmod +x scripts/deploy-ec2.sh
#   ./scripts/deploy-ec2.sh
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

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

[ -f ".env.prod" ] || log_error ".env.prod not found in $PROJECT_ROOT. Upload it first:
  scp -i your-key.pem .env.prod ec2-user@<EC2-IP>:$PROJECT_ROOT/"

# =============================================================================
# STEP 1: Install Docker + Compose (if not present)
# =============================================================================
if ! command -v docker &>/dev/null; then
  log_info "Installing Docker..."
  sudo yum update -y
  sudo yum install -y docker
  sudo systemctl start docker
  sudo systemctl enable docker
  sudo usermod -aG docker ec2-user
  log_success "Docker installed — you may need to re-login for group changes"
else
  log_success "Docker already installed"
fi

if ! docker compose version &>/dev/null; then
  log_info "Installing Docker Compose plugin..."
  DOCKER_COMPOSE_VERSION="2.27.0"
  sudo curl -SL \
    "https://github.com/docker/compose/releases/download/v${DOCKER_COMPOSE_VERSION}/docker-compose-linux-x86_64" \
    -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
  sudo ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
  log_success "Docker Compose installed"
else
  log_success "Docker Compose already installed"
fi

# =============================================================================
# STEP 2: Build images
# =============================================================================
log_info "Building Docker images..."
docker compose \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  --env-file .env.prod \
  build --no-cache
log_success "Images built"

# =============================================================================
# STEP 3: Stop existing containers
# =============================================================================
log_info "Stopping existing containers..."
docker compose \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  --env-file .env.prod \
  down --remove-orphans || true
log_success "Existing containers stopped"

# =============================================================================
# STEP 4: Start production containers
# =============================================================================
log_info "Starting production containers..."
docker compose \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  --env-file .env.prod \
  up -d
log_success "Production containers started"

# =============================================================================
# STEP 5: Status
# =============================================================================
log_info "Waiting 45s for services to become healthy..."
sleep 45

echo ""
echo "════════════════════════════════════════════════════════"
echo "  Production Container Status"
echo "════════════════════════════════════════════════════════"
docker compose \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  --env-file .env.prod \
  ps
echo ""

EC2_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "<your-ec2-ip>")
echo "════════════════════════════════════════════════════════"
echo "  Access Points (EC2)"
echo "════════════════════════════════════════════════════════"
echo "  Frontend:    http://$EC2_IP"
echo "  API Gateway: http://$EC2_IP:8080/actuator/health"
echo "════════════════════════════════════════════════════════"
echo ""
log_success "EC2 deployment complete!"