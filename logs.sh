#!/bin/bash

# Notaire Application Logs Script
# This script displays logs from running services

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Colors for output
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get service argument (default: backend)
SERVICE=${1:-backend}

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Notaire Application Logs${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${YELLOW}Following logs for service: $SERVICE${NC}\n"
echo -e "${YELLOW}Press Ctrl+C to exit${NC}\n"

docker-compose logs -f "$SERVICE"
