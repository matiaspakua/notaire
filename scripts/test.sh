#!/bin/bash

# Notaire Application Test Runner
# This script runs the test suite against the running API

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Notaire API Test Suite${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Check if API is running
echo -e "${YELLOW}Checking if API is running...${NC}"
if ! curl -s http://localhost:8080/swagger-ui.html > /dev/null 2>&1; then
    echo -e "${RED}✗ API is not running at http://localhost:8080${NC}"
    echo -e "${YELLOW}Start the application first with: bash start.sh${NC}"
    exit 1
fi
echo -e "${GREEN}✓ API is running${NC}\n"

# Run the test suite
echo -e "${YELLOW}Running API test suite...${NC}\n"
cd "$PROJECT_DIR/test/http"

if [ -f "test-all-endpoints-v2.sh" ]; then
    bash test-all-endpoints-v2.sh
else
    echo -e "${RED}✗ Test suite not found${NC}"
    exit 1
fi
