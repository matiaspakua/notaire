#!/bin/bash

# Notaire Application Stop Script
# This script stops the complete Notaire application stack

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Notaire Application Shutdown${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Determine docker compose command (docker compose v2 or docker-compose v1)
if docker compose version &> /dev/null 2>&1; then
    DC_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    DC_CMD="docker-compose"
else
    echo -e "${RED}✗ Docker Compose is not installed${NC}"
    exit 1
fi

echo -e "${YELLOW}Stopping Docker Compose services...${NC}"

# Show what services are currently running
echo -e "${BLUE}Current services:${NC}"
$DC_CMD ps

echo -e "\n${YELLOW}Shutting down all services (PostgreSQL, Backend API, pgAdmin)...${NC}"

if $DC_CMD down; then
    echo -e "${GREEN}✓ All services stopped successfully${NC}"
    echo -e "  - PostgreSQL (port 5432)"
    echo -e "  - Backend API (port 8080)"  
    echo -e "  - pgAdmin (port 5050)"
else
    echo -e "${RED}✗ Failed to stop services${NC}"
    exit 1
fi

echo -e "\n${GREEN}✓ Notaire Application shutdown complete${NC}"
echo -e "${BLUE}Data preserved in Docker volumes:${NC}"
echo -e "  - PostgreSQL data: ${YELLOW}postgres_data${NC}"
echo -e "  - pgAdmin config:  ${YELLOW}pgadmin_data${NC}"
echo ""
