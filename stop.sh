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

if $DC_CMD down; then
    echo -e "${GREEN}✓ Services stopped successfully${NC}"
else
    echo -e "${RED}✗ Failed to stop services${NC}"
    exit 1
fi

echo -e "\n${GREEN}✓ Notaire Application shutdown complete${NC}"
echo ""
