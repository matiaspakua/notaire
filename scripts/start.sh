#!/bin/bash

# Notaire Application Startup Script
# This script starts the complete Notaire application stack:
#   - PostgreSQL 16 (port 5432 or $POSTGRES_PORT) — schema is managed by Flyway (V1→V13+)
#   - Backend API  (port 8080 or $BACKEND_PORT)    — Spring Boot, JWT auth, Actuator health
#   - Next.js Frontend (port 3000 or $FRONTEND_PORT)
#   - pgAdmin (port 5050 or $PGADMIN_PORT)         — optional (--no-admin)
#   - Next.js frontend hint       — optional (--frontend); Swing GUI removed (#811)
#
# All service credentials come from the single root .env file (git-ignored).
# Flyway is the single source of truth for the database schema; PostgreSQL
# starts empty and the backend applies migrations on startup.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_DIR"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Default options
START_FRONTEND=false
SKIP_BUILD=false
WITH_ADMIN=true

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --frontend|-f)
            START_FRONTEND=true
            shift
            ;;
        --no-frontend)
            START_FRONTEND=false
            shift
            ;;
        --skip-build|-s)
            SKIP_BUILD=true
            shift
            ;;
        --admin|-a)
            WITH_ADMIN=true
            shift
            ;;
        --no-admin)
            WITH_ADMIN=false
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -f, --frontend    Print Next.js start instructions (Swing GUI removed; #811)"
            echo "  -s, --skip-build  Skip Maven build (use existing Docker images)"
            echo "  -a, --admin       Start pgAdmin for database management (default: true)"
            echo "  --no-frontend     Don't start the Swing frontend GUI (default mode)"
            echo "  --no-admin        Don't start pgAdmin"
            echo "  -h, --help        Show this help message"
            echo ""
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

fail() {
    echo -e "${RED}✗ $1${NC}" >&2
    exit 1
}

# Read a value from the root .env (the single source of truth for credentials).
# Falls back to the provided default when the key is missing or empty.
env_value() {
    local key="$1"
    local default="$2"
    local value
    value="$(grep -E "^${key}=" .env 2>/dev/null | tail -1 | cut -d= -f2- | tr -d '\r')"
    echo "${value:-$default}"
}

# The .env file is required: docker-compose.yml and the backend have no
# defaults for JWT_SECRET / ACTUATOR_USER / ACTUATOR_PASSWORD, so without it
# the stack starts but the backend fails to boot.
if [ ! -f ".env" ]; then
    fail ".env not found in $REPO_DIR. Create it from the template first:
  cp .env.example .env"
fi

POSTGRES_USER="$(env_value POSTGRES_USER admin)"
POSTGRES_DB="$(env_value POSTGRES_DB notaire)"
PGADMIN_EMAIL="$(env_value PGADMIN_DEFAULT_EMAIL admin@notaire.com)"
PGADMIN_PASSWORD="$(env_value PGADMIN_DEFAULT_PASSWORD admin)"
APP_ADMIN_USER="$(env_value APP_ADMIN_USER admin)"
APP_ADMIN_PASSWORD="$(env_value APP_ADMIN_PASSWORD admin)"
# Same precedence as docker compose: exported shell variable, then .env, then default.
POSTGRES_PORT="${POSTGRES_PORT:-$(env_value POSTGRES_PORT 5432)}"
BACKEND_PORT="${BACKEND_PORT:-$(env_value BACKEND_PORT 8080)}"
PGADMIN_PORT="${PGADMIN_PORT:-$(env_value PGADMIN_PORT 5050)}"
FRONTEND_PORT="${FRONTEND_PORT:-$(env_value FRONTEND_PORT 3000)}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Notaire Application Startup${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    fail "Docker is not installed"
fi

# Check that the Docker daemon is actually running
if ! docker info &> /dev/null; then
    fail "Docker daemon is not running. Start Docker Desktop (or the Docker service) and try again."
fi

# Determine docker compose command (docker compose v2 or docker-compose v1)
if docker compose version &> /dev/null 2>&1; then
    DC_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    DC_CMD="docker-compose"
else
    fail "Docker Compose is not installed"
fi

STEP=1

# Build all modules if not skipped
if [ "$SKIP_BUILD" = false ]; then
    echo -e "${YELLOW}Step $STEP: Building all Maven modules...${NC}"
    STEP=$((STEP + 1))
    if ! command -v mvn &> /dev/null; then
        fail "Maven is not installed. Install Maven, or use --skip-build with pre-built images."
    fi
    if mvn clean install -DskipTests > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Maven build successful${NC}\n"
    else
        echo -e "${RED}✗ Maven build failed. Showing output:${NC}"
        mvn clean install -DskipTests
        exit 1
    fi
else
    echo -e "${YELLOW}Skipping Maven build (--skip-build)${NC}\n"
fi

# Validate the compose configuration before touching anything: this catches
# missing environment variables and invalid YAML while existing containers
# are still running.
echo -e "${YELLOW}Step $STEP: Validating Docker Compose configuration...${NC}"
STEP=$((STEP + 1))
if ! $DC_CMD config --quiet > /dev/null 2>&1; then
    echo -e "${RED}✗ Docker Compose configuration is invalid:${NC}"
    $DC_CMD config
    exit 1
fi
echo -e "${GREEN}✓ Docker Compose configuration valid${NC}\n"

# Stop and remove existing containers and volumes so schema changes in
# Flyway migrations are applied from scratch. NOTE: this wipes the
# PostgreSQL and pgAdmin data volumes.
echo -e "${YELLOW}Step $STEP: Cleaning up existing containers and volumes (data will be reset)...${NC}"
STEP=$((STEP + 1))
$DC_CMD down --volumes 2>/dev/null || true
echo -e "${GREEN}✓ Containers cleaned up${NC}\n"

# Start Docker Compose services
echo -e "${YELLOW}Step $STEP: Starting Docker Compose services...${NC}"
STEP=$((STEP + 1))

DC_ARGS="up -d --build"
$DC_CMD $DC_ARGS

echo -e "${YELLOW}Step $STEP: Waiting for services to be ready...${NC}"
STEP=$((STEP + 1))
sleep 5

# Check PostgreSQL
echo -e "${YELLOW}Step $STEP: Verifying PostgreSQL...${NC}"
STEP=$((STEP + 1))
for i in {1..30}; do
    if $DC_CMD exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT 1" &> /dev/null; then
        echo -e "${GREEN}✓ PostgreSQL is ready${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}✗ PostgreSQL failed to start${NC}"
        $DC_CMD logs postgres
        exit 1
    fi
    echo "  Waiting for PostgreSQL... ($i/30)"
    sleep 1
done

# The database schema and seed data are applied by Flyway when the backend
# boots (PostgreSQL starts empty; Flyway applies V1→V13+). No manual
# init-db loading is needed — the old init-db scripts are archived at
# docs/archive/init-db/.
echo -e "${YELLOW}Step $STEP: Waiting for Flyway migrations (applied by the backend on startup)...${NC}"
STEP=$((STEP + 1))
echo -e "${GREEN}✓ Flyway is the single source of truth — migrations run automatically${NC}\n"

# Check Backend
echo -e "${YELLOW}Step $STEP: Verifying Backend API...${NC}"
STEP=$((STEP + 1))
for i in {1..90}; do
    if curl -sf http://localhost:$BACKEND_PORT/actuator/health 2>/dev/null | grep -q '"status":"UP"'; then
        echo -e "${GREEN}✓ Backend API is ready${NC}"
        break
    fi
    if [ $i -eq 90 ]; then
        echo -e "${RED}✗ Backend API failed to start${NC}"
        $DC_CMD logs backend
        exit 1
    fi
    echo "  Waiting for Backend API... ($i/90)"
    sleep 2
done

# Check pgAdmin if enabled
if [ "$WITH_ADMIN" = true ]; then
    echo -e "${YELLOW}Step $STEP: Verifying pgAdmin...${NC}"
    STEP=$((STEP + 1))
    for i in {1..60}; do
        if curl -sf http://localhost:$PGADMIN_PORT > /dev/null 2>&1; then
            echo -e "${GREEN}✓ pgAdmin is ready${NC}"
            break
        fi
        if [ $i -eq 60 ]; then
            echo -e "${RED}✗ pgAdmin failed to start${NC}"
            $DC_CMD logs pgadmin
            exit 1
        fi
        echo "  Waiting for pgAdmin... ($i/60)"
        sleep 2
    done
fi

echo -e "\n${BLUE}Step $STEP: Verifying Frontend...${NC}"
STEP=$((STEP + 1))
for i in {1..60}; do
    if curl -sf http://localhost:$FRONTEND_PORT > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Frontend is ready${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e "${RED}✗ Frontend failed to start${NC}"
        $DC_CMD logs frontend
        exit 1
    fi
    echo "  Waiting for Frontend... ($i/60)"
    sleep 2
done

echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✓ All services are running!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${BLUE}Available Services:${NC}"
echo -e "  Frontend:     ${YELLOW}http://localhost:$FRONTEND_PORT${NC}"
echo -e "  API Swagger:  ${YELLOW}http://localhost:$BACKEND_PORT/swagger-ui.html${NC}"
echo -e "  API Docs:     ${YELLOW}http://localhost:$BACKEND_PORT/v3/api-docs${NC}"
if [ "$WITH_ADMIN" = true ]; then
    echo -e "  PgAdmin:      ${YELLOW}http://localhost:$PGADMIN_PORT${NC} ($PGADMIN_EMAIL / $PGADMIN_PASSWORD)"
else
    echo -e "  PgAdmin:      ${RED}Disabled${NC}"
fi
echo -e "  PostgreSQL:   ${YELLOW}localhost:$POSTGRES_PORT${NC}"
echo ""
echo -e "${BLUE}Database Access:${NC}"
echo -e "  Username:     ${YELLOW}$POSTGRES_USER${NC} (app login: $APP_ADMIN_USER / $APP_ADMIN_PASSWORD)"
echo ""
echo ""

# Next.js is the active client; Swing GUI was removed (#811 / ADR-005).
if [ "$START_FRONTEND" = true ]; then
    echo -e "${YELLOW}Step $STEP: Frontend (Next.js) — Swing GUI retired (#811)${NC}"
    STEP=$((STEP + 1))
    echo -e "${GREEN}✓ Backend stack is up. Start the web client with:${NC}"
    echo -e "  ${YELLOW}cd frontend && npm install && npm run dev${NC}"
    echo -e "  UI E2E: ${YELLOW}cd frontend && npm run test:e2e${NC}"
    echo ""
fi

echo -e "${BLUE}Useful Commands:${NC}"
echo -e "  View logs:        ${YELLOW}bash scripts/logs.sh [backend|frontend|postgres|pgadmin]${NC}"
echo -e "  Stop services:    ${YELLOW}bash scripts/stop.sh${NC}"
echo -e "  Run tests:        ${YELLOW}cd testing/http && bash test-all-endpoints-v2.sh${NC}"
if [ "$WITH_ADMIN" = true ]; then
    echo -e "  pgAdmin setup:   ${YELLOW}bash scripts/setup-pgadmin.sh${NC}"
fi
if [ "$START_FRONTEND" = false ]; then
    echo -e "  Start frontend:   ${YELLOW}cd frontend && npm run dev${NC}"
fi
echo ""
