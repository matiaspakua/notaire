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

# Get service argument (default: show all services)
SERVICE=${1:-}

# Show help if requested
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    echo -e "${BLUE}Notaire Application Logs${NC}"
    echo ""
    echo -e "${YELLOW}Usage: $0 [SERVICE_NAME]${NC}"
    echo ""
    echo -e "${BLUE}Available services:${NC}"
    echo -e "  backend     - Backend API logs (default if no service specified)"
    echo -e "  postgres    - PostgreSQL database logs"
    echo -e "  pgadmin     - pgAdmin web interface logs"
    echo -e "  (no args)   - Show all services logs"
    echo ""
    echo -e "${BLUE}Examples:${NC}"
    echo -e "  $0            # Show all logs"
    echo -e "  $0 backend    # Show backend only"
    echo -e "  $0 postgres   # Show database only"
    echo -e "  $0 pgadmin    # Show pgAdmin only"
    exit 0
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Notaire Application Logs${NC}"
echo -e "${BLUE}========================================${NC}"

if [ -z "$SERVICE" ]; then
    echo -e "${YELLOW}Following logs for all services${NC}\n"
    echo -e "${YELLOW}Press Ctrl+C to exit${NC}\n"
    $DC_CMD logs -f
else
    echo -e "${YELLOW}Following logs for service: $SERVICE${NC}\n"
    echo -e "${YELLOW}Press Ctrl+C to exit${NC}\n"
    $DC_CMD logs -f "$SERVICE"
fi
