#!/bin/bash

# Notaire pgAdmin Setup Script
# This script helps set up the database connection in pgAdmin

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   pgAdmin Database Setup Guide${NC}"
echo -e "${BLUE}========================================${NC}\n"

echo -e "${BLUE}Step 1: Access pgAdmin${NC}"
echo -e "  URL:        ${YELLOW}http://localhost:5050${NC}"
echo -e "  Email:      ${YELLOW}admin@notaire.local${NC}"
echo -e "  Password:   ${YELLOW}admin${NC}"
echo ""

echo -e "${BLUE}Step 2: Add PostgreSQL Server${NC}"
echo "  1. Click 'Add New Server' (the + icon)"
echo "  2. Go to the 'Connection' tab"
echo "  3. Fill in these details:"
echo ""

# Read environment variables or use defaults
POSTGRES_HOST=${POSTGRES_HOST:-localhost}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
POSTGRES_DB=${POSTGRES_DB:-notaire}
POSTGRES_USER=${POSTGRES_USER:-notaire}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-notaire_password}

echo -e "${YELLOW}Connection Settings:${NC}"
echo -e "  Host name/address: ${GREEN}$POSTGRES_HOST${NC}"
echo -e "  Port:             ${GREEN}$POSTGRES_PORT${NC}"
echo -e "  Maintenance DB:    ${GREEN}$POSTGRES_DB${NC}"
echo -e "  Username:          ${GREEN}$POSTGRES_USER${NC}"
echo -e "  Password:          ${GREEN}$POSTGRES_PASSWORD${NC}"
echo ""

echo -e "${BLUE}Step 3: Save Connection${NC}"
echo "  1. Click 'Save' to store the connection"
echo "  2. You should now see the notaire database in the browser"
echo ""

echo -e "${BLUE}Optional: Advanced Settings${NC}"
echo -e "  SSL mode:          ${YELLOW}prefer${NC} (for secure connections)"
echo -e "  Connection timeout: ${YELLOW}30 seconds${NC}"
echo ""

echo -e "${GREEN}✓ Setup complete! You can now manage the database.${NC}"
echo ""

# Check if pgAdmin is running
if curl -s http://localhost:5050 > /dev/null; then
    echo -e "${GREEN}✓ pgAdmin is running and accessible${NC}"
else
    echo -e "${RED}✗ pgAdmin is not running${NC}"
    echo -e "${YELLOW}Start it with: ${NC}bash start.sh --admin"
    exit 1
fi

echo -e "${BLUE}Useful Commands:${NC}"
echo -e "  Start services:    ${YELLOW}bash start.sh${NC}"
echo -e "  Stop services:     ${YELLOW}bash stop.sh${NC}"
echo -e "  View pgAdmin logs: ${YELLOW}./logs.sh pgadmin${NC}"
echo ""