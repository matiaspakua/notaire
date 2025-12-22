#!/bin/bash

# Notaire Application Startup Script
# This script starts the complete Notaire application stack

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

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

echo -e "${YELLOW}Step 1: Building backend application...${NC}"
if mvn clean package -DskipTests -pl backend-api > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Backend build successful${NC}\n"
else
    echo -e "${RED}✗ Backend build failed${NC}"
    mvn clean package -DskipTests -pl backend-api
    exit 1
fi

echo -e "${YELLOW}Step 2: Starting Docker Compose services...${NC}"
$DC_CMD up -d --build

echo -e "${YELLOW}Step 3: Waiting for services to be ready...${NC}"
sleep 10

# Check PostgreSQL
echo -e "${YELLOW}Step 4: Verifying PostgreSQL...${NC}"
if $DC_CMD exec -T postgres pg_isready -U notaire &> /dev/null; then
    echo -e "${GREEN}✓ PostgreSQL is ready${NC}"
else
    echo -e "${RED}✗ PostgreSQL failed to start${NC}"
    $DC_CMD logs postgres
    exit 1
fi

# Check Backend
echo -e "${YELLOW}Step 5: Verifying Backend API...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:8080/swagger-ui.html > /dev/null; then
        echo -e "${GREEN}✓ Backend API is ready${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}✗ Backend API failed to start${NC}"
        $DC_CMD logs backend
        exit 1
    fi
    echo "  Waiting... ($i/30)"
    sleep 1
done

echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✓ Notaire Application is running!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${BLUE}Available Services:${NC}"
echo -e "  API Swagger:  ${YELLOW}http://localhost:8080/swagger-ui.html${NC}"
echo -e "  API Docs:     ${YELLOW}http://localhost:8080/v3/api-docs${NC}"
echo -e "  PgAdmin:      ${YELLOW}http://localhost:5050${NC} (if started with --profile admin)"
echo -e "  PostgreSQL:   ${YELLOW}localhost:5432${NC}"
echo ""
echo -e "${BLUE}Useful Commands:${NC}"
echo -e "  View logs:      ${YELLOW}$DC_CMD logs -f backend${NC}"
echo -e "  Stop services:  ${YELLOW}$DC_CMD down${NC}"
echo -e "  Run tests:      ${YELLOW}cd test/http && bash test-all-endpoints-v2.sh${NC}"
echo ""
