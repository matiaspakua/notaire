#!/bin/bash

# Notaire Application Startup Script
# This script starts the complete Notaire application stack

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
            echo "  -f, --frontend    Start the Swing frontend GUI after backend is ready"
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

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Notaire Application Startup${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker is not installed${NC}"
    exit 1
fi

# Determine docker compose command (docker compose v2 or docker-compose v1)
if docker compose version &> /dev/null 2>&1; then
    DC_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    DC_CMD="docker-compose"
else
    echo -e "${RED}✗ Docker Compose is not installed${NC}"
    exit 1
fi

STEP=1

# Build all modules if not skipped
if [ "$SKIP_BUILD" = false ]; then
    echo -e "${YELLOW}Step $STEP: Building all Maven modules...${NC}"
    STEP=$((STEP + 1))
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

# Stop and remove existing containers to handle credential changes
echo -e "${YELLOW}Step $STEP: Cleaning up existing containers...${NC}"
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
    if $DC_CMD exec -T postgres psql -U admin -d notaire -c "SELECT 1" &> /dev/null; then
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

# Load database initialization scripts if tables don't exist
echo -e "${YELLOW}Step $STEP: Loading database schema and data...${NC}"
STEP=$((STEP + 1))
TABLE_COUNT=$($DC_CMD exec -T postgres psql -U admin -d notaire -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | tr -d ' ' || echo "0")

if [ "$TABLE_COUNT" = "0" ] || [ -z "$TABLE_COUNT" ]; then
    echo "  Loading schema from init-db/01-schema.sql..."
    $DC_CMD exec -T postgres psql -U admin -d notaire -f /docker-entrypoint-initdb.d/01-schema.sql &> /dev/null || true
    echo "  Loading data from init-db/02-data.sql..."
    $DC_CMD exec -T postgres psql -U admin -d notaire -f /docker-entrypoint-initdb.d/02-data.sql &> /dev/null || true
    echo -e "${GREEN}✓ Database schema and data loaded${NC}"
else
    echo -e "${GREEN}✓ Database already initialized ($TABLE_COUNT tables)${NC}"
fi

# Check Backend
echo -e "${YELLOW}Step $STEP: Verifying Backend API...${NC}"
STEP=$((STEP + 1))
for i in {1..60}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        echo -e "${GREEN}✓ Backend API is ready${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e "${RED}✗ Backend API failed to start${NC}"
        $DC_CMD logs backend
        exit 1
    fi
    echo "  Waiting for Backend API... ($i/60)"
    sleep 2
done

# Check pgAdmin if enabled
if [ "$WITH_ADMIN" = true ]; then
    echo -e "${YELLOW}Step $STEP: Verifying pgAdmin...${NC}"
    STEP=$((STEP + 1))
    for i in {1..45}; do
        if curl -s http://localhost:5050 > /dev/null; then
            echo -e "${GREEN}✓ pgAdmin is ready${NC}"
            break
        fi
        if [ $i -eq 45 ]; then
            echo -e "${RED}✗ pgAdmin failed to start${NC}"
            $DC_CMD logs pgadmin
            exit 1
        fi
        echo "  Waiting for pgAdmin... ($i/45)"
        sleep 2
    done
fi

echo -e "\n${BLUE}Step $STEP: Verifying Frontend...${NC}"
STEP=$((STEP + 1))
for i in {1..30}; do
    if curl -s http://localhost:3000 > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Frontend is ready${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}✗ Frontend failed to start${NC}"
        $DC_CMD logs frontend
        exit 1
    fi
    echo "  Waiting for Frontend... ($i/30)"
    sleep 2
done

echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✓ All services are running!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${BLUE}Available Services:${NC}"
echo -e "  Frontend:     ${YELLOW}http://localhost:3000${NC}"
echo -e "  API Swagger:  ${YELLOW}http://localhost:8080/swagger-ui.html${NC}"
echo -e "  API Docs:     ${YELLOW}http://localhost:8080/v3/api-docs${NC}"
if [ "$WITH_ADMIN" = true ]; then
    echo -e "  PgAdmin:      ${YELLOW}http://localhost:5050${NC} (admin@notaire.com / admin)"
else
    echo -e "  PgAdmin:      ${RED}Disabled${NC}"
fi
echo -e "  PostgreSQL:   ${YELLOW}localhost:5432${NC}"
echo ""
echo -e "${BLUE}Database Access:${NC}"
echo -e "  Username:     ${YELLOW}admin${NC} (app login)"
echo -e "  Password:     ${YELLOW}admin${NC} (app login)"
if [ "$WITH_ADMIN" = true ]; then
    echo -e "  PgAdmin:      ${YELLOW}admin@notaire.com / admin${NC}"
fi
echo ""
echo ""

# Start Frontend Swing application if requested
if [ "$START_FRONTEND" = true ]; then
    echo -e "${YELLOW}Step $STEP: Starting Frontend Swing application...${NC}"
    STEP=$((STEP + 1))
    
    # Check if DISPLAY is available (for GUI)
    if [ -z "$DISPLAY" ] && [ "$(uname)" != "Darwin" ]; then
        echo -e "${RED}✗ No DISPLAY environment variable set. Cannot start GUI.${NC}"
        echo -e "  Set DISPLAY or run on a system with a graphical environment."
        exit 1
    fi
    
    FRONTEND_JAR="$REPO_DIR/frontend-swing/target/frontend-swing-1.0-SNAPSHOT-jar-with-dependencies.jar"
    
    if [ ! -f "$FRONTEND_JAR" ]; then
        echo -e "${RED}✗ Frontend JAR not found at: $FRONTEND_JAR${NC}"
        echo -e "  Run without --skip-build to build the frontend first."
        exit 1
    fi
    
    echo -e "${GREEN}✓ Starting Swing GUI with dependencies...${NC}"
    # Run frontend in background
    java -jar "$FRONTEND_JAR" &
    FRONTEND_PID=$!
    echo -e "${GREEN}✓ Frontend started (PID: $FRONTEND_PID)${NC}"
    echo ""
    echo -e "${BLUE}Frontend:${NC}"
    echo -e "  Process ID:   ${YELLOW}$FRONTEND_PID${NC}"
    echo -e "  Stop with:    ${YELLOW}kill $FRONTEND_PID${NC}"
    echo ""
fi

echo -e "${BLUE}Useful Commands:${NC}"
echo -e "  View logs:        ${YELLOW}bash scripts/logs.sh [backend|frontend|postgres|pgadmin]${NC}"
echo -e "  Stop services:    ${YELLOW}bash scripts/stop.sh${NC}"
echo -e "  Run tests:        ${YELLOW}cd test/http && bash test-all-endpoints-v2.sh${NC}"
if [ "$WITH_ADMIN" = true ]; then
    echo -e "  pgAdmin setup:   ${YELLOW}bash scripts/setup-pgadmin.sh${NC}"
fi
if [ "$START_FRONTEND" = false ]; then
    echo -e "  Start frontend:   ${YELLOW}java -jar frontend-swing/target/frontend-swing-1.0-SNAPSHOT-jar-with-dependencies.jar${NC}"
fi
echo ""
