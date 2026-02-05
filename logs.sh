#!/bin/bash

# Notaire Application Logs Script
# This script displays logs from running services

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Colors for output
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Determine docker compose command (docker compose v2 or docker-compose v1)
if docker compose version &> /dev/null 2>&1; then
    DC_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    DC_CMD="docker-compose"
else
    echo -e "${RED}✗ Docker Compose is not installed${NC}"
    exit 1
fi

# Get service argument (default: backend)
SERVICE=${1:-backend}

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Notaire Application Logs${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}Following logs for service: $SERVICE${NC}\n"
echo -e "${YELLOW}Press Ctrl+C to exit${NC}\n"

$DC_CMD logs -f "$SERVICE"
