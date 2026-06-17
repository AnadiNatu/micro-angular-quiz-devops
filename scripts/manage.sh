#!/bin/bash
# =============================================================================
# scripts/manage.sh
# Handy management commands for the quiz platform containers
#
# Usage:
#   ./scripts/manage.sh logs              — tail all service logs
#   ./scripts/manage.sh logs auth-service — tail one service log
#   ./scripts/manage.sh restart           — restart all containers
#   ./scripts/manage.sh stop              — stop all containers (data safe)
#   ./scripts/manage.sh status            — show status + health
#   ./scripts/manage.sh clean             — remove containers + images (keeps DB volume)
#   ./scripts/manage.sh nuke              — remove EVERYTHING including DB data (⚠ DANGER)
#   ./scripts/manage.sh db-shell          — open psql shell in postgres container
#   ./scripts/manage.sh db-backup         — backup all databases to ./backups/
# =============================================================================

DEVOPS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DEVOPS_DIR"

COMPOSE_CMD="docker compose"
ENV_FILE=".env"

# Use prod env if --prod flag passed
if [[ "$*" == *"--prod"* ]]; then
  COMPOSE_CMD="docker compose -f docker-compose.yml -f docker-compose.prod.yml"
  ENV_FILE=".env.prod"
fi

case "$1" in

  logs)
    SERVICE="${2:-}"
    if [ -n "$SERVICE" ]; then
      $COMPOSE_CMD --env-file "$ENV_FILE" logs -f "$SERVICE"
    else
      $COMPOSE_CMD --env-file "$ENV_FILE" logs -f
    fi
    ;;

  restart)
    SERVICE="${2:-}"
    if [ -n "$SERVICE" ]; then
      echo "Restarting $SERVICE..."
      $COMPOSE_CMD --env-file "$ENV_FILE" restart "$SERVICE"
    else
      echo "Restarting all services..."
      $COMPOSE_CMD --env-file "$ENV_FILE" restart
    fi
    ;;

  stop)
    echo "Stopping all containers (volumes and data preserved)..."
    $COMPOSE_CMD --env-file "$ENV_FILE" stop
    echo "Done. Run 'docker compose up -d' to restart."
    ;;

  status)
    echo "════════════════════════════════════════"
    echo "  Container Status"
    echo "════════════════════════════════════════"
    $COMPOSE_CMD --env-file "$ENV_FILE" ps
    echo ""
    echo "════════════════════════════════════════"
    echo "  Health Endpoints"
    echo "════════════════════════════════════════"
    for PORT in 8080 8081 8082 8083 8084; do
      STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/actuator/health 2>/dev/null || echo "DOWN")
      echo "  :$PORT → $STATUS"
    done
    ;;

  clean)
    echo "Removing containers and images (DB volume PRESERVED)..."
    $COMPOSE_CMD --env-file "$ENV_FILE" down --rmi local --remove-orphans
    echo "Done. DB data intact in volume: quiz-postgres-data"
    ;;

  nuke)
    echo "⚠️  WARNING: This will DELETE ALL DATA including the database volume!"
    read -rp "Type 'yes-delete-everything' to confirm: " CONFIRM
    if [ "$CONFIRM" = "yes-delete-everything" ]; then
      $COMPOSE_CMD --env-file "$ENV_FILE" down -v --rmi local --remove-orphans
      echo "Everything removed."
    else
      echo "Aborted."
    fi
    ;;

  db-shell)
    echo "Opening PostgreSQL shell..."
    docker exec -it quiz-postgres psql -U postgres
    ;;

  db-backup)
    BACKUP_DIR="$DEVOPS_DIR/backups/$(date +%Y-%m-%d_%H-%M-%S)"
    mkdir -p "$BACKUP_DIR"
    source "$ENV_FILE"

    echo "Backing up databases to $BACKUP_DIR..."

    for DB in "$AUTH_DB_NAME" "$QUESTION_DB_NAME" "$QUIZ_DB_NAME"; do
      echo "  Dumping $DB..."
      docker exec quiz-postgres pg_dump -U postgres "$DB" > "$BACKUP_DIR/${DB}.sql"
      echo "  ✅ $DB backed up"
    done

    echo "Backup complete: $BACKUP_DIR"
    ls -lh "$BACKUP_DIR"
    ;;

  db-restore)
    DB_NAME="${2:-}"
    BACKUP_FILE="${3:-}"
    if [ -z "$DB_NAME" ] || [ -z "$BACKUP_FILE" ]; then
      echo "Usage: ./manage.sh db-restore <db_name> <backup_file.sql>"
      exit 1
    fi
    echo "Restoring $DB_NAME from $BACKUP_FILE..."
    docker exec -i quiz-postgres psql -U postgres "$DB_NAME" < "$BACKUP_FILE"
    echo "Restore complete."
    ;;

  *)
    echo "Usage: $0 {logs|restart|stop|status|clean|nuke|db-shell|db-backup|db-restore} [--prod]"
    exit 1
    ;;
esac
