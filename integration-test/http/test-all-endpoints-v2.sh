#!/bin/bash

set -euo pipefail

# Notaire API - Complete Test Suite (strict mode)

# Base URL for the API
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}==========================================${NC}"
echo -e "${BLUE}   Notaire API - Complete Test Suite${NC}"
echo -e "${BLUE}==========================================${NC}\n"

assert_status() {
  local expected="$1"
  local method="$2"
  local url="$3"
  local data="${4:-}"
  local status

  if [ -n "$data" ]; then
    status=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" -d "$data")
  else
    status=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" -H "Content-Type: application/json")
  fi

  echo "$method $url -> $status"
  if [ "$status" -ne "$expected" ]; then
    echo -e "${RED}Expected $expected but got $status for $method $url${NC}"
    exit 1
  fi
}

echo -e "${BLUE}=== TESTING AUTHENTICATION ===${NC}"
assert_status 200 POST "$BASE_URL/api/v1/usuarios/login" '{"nombre":"admin","contrasenia":"admin"}'

echo -e "${BLUE}=== TESTING CORE READ ENDPOINTS ===${NC}"
assert_status 200 GET "$BASE_URL/api/v1/conceptos"
assert_status 200 GET "$BASE_URL/api/v1/personas"
assert_status 200 GET "$BASE_URL/api/v1/tramites"
assert_status 200 GET "$BASE_URL/api/v1/escrituras"
assert_status 200 GET "$BASE_URL/api/v1/presupuestos"
assert_status 200 GET "$BASE_URL/api/v1/items"

echo -e "${BLUE}=== TESTING CREATE ENDPOINTS ===${NC}"
assert_status 200 POST "$BASE_URL/api/v1/conceptos" '{"nombre":"Concepto Test V2","valor":500.0}'
assert_status 200 POST "$BASE_URL/api/v1/items" '{"nombre":"Item Test V2","valor":10.0,"conceptoFijo":false}'

# ============================================================================
# SUMMARY
# ============================================================================
echo -e "${BLUE}==========================================${NC}"
echo -e "${BLUE}   Test Execution Summary${NC}"
echo -e "${BLUE}==========================================${NC}"
echo -e "${GREEN}All strict endpoint checks passed${NC}"
echo -e "${BLUE}==========================================${NC}\n"
echo -e "${GREEN}Test suite completed!${NC}\n"
