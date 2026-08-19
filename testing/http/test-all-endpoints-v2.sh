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
  local auth_header=()

  if [ -n "$AUTH_TOKEN" ]; then
    auth_header=(-H "Authorization: Bearer $AUTH_TOKEN")
  fi

  if [ -n "$data" ]; then
    status=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" "${auth_header[@]}" -d "$data")
  else
    status=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" "${auth_header[@]}")
  fi

  echo "$method $url -> $status"
  if [ "$status" -ne "$expected" ]; then
    echo -e "${RED}Expected $expected but got $status for $method $url${NC}"
    exit 1
  fi
}

echo -e "${BLUE}=== TESTING AUTHENTICATION ===${NC}"
AUTH_TOKEN=""
LOGIN_URL="$BASE_URL/api/v1/usuarios/login"
LOGIN_RAW=$(curl -s -w '\n%{http_code}' -X POST "$LOGIN_URL" -H "Content-Type: application/json" -d '{"nombre":"admin","contrasenia":"admin"}')
LOGIN_STATUS=$(printf '%s' "$LOGIN_RAW" | tail -n1)
LOGIN_BODY=$(printf '%s' "$LOGIN_RAW" | sed '$d')

echo "POST $LOGIN_URL -> $LOGIN_STATUS"
if [ "$LOGIN_STATUS" -ne 200 ]; then
  echo -e "${RED}Expected 200 but got $LOGIN_STATUS for POST $LOGIN_URL${NC}"
  exit 1
fi

AUTH_TOKEN=$(printf '%s' "$LOGIN_BODY" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -z "$AUTH_TOKEN" ]; then
  echo -e "${RED}Login succeeded but response had no token: $LOGIN_BODY${NC}"
  exit 1
fi

echo -e "${BLUE}=== TESTING CORE READ ENDPOINTS ===${NC}"
assert_status 200 GET "$BASE_URL/api/v1/conceptos"
assert_status 200 GET "$BASE_URL/api/v1/personas"
assert_status 200 GET "$BASE_URL/api/v1/tramites"
assert_status 200 GET "$BASE_URL/api/v1/escrituras"
assert_status 200 GET "$BASE_URL/api/v1/presupuestos"
assert_status 200 GET "$BASE_URL/api/v1/items"

echo -e "${BLUE}=== TESTING CREATE ENDPOINTS ===${NC}"
assert_status 201 POST "$BASE_URL/api/v1/conceptos" '{"nombre":"Concepto Test V2","valor":500.0}'
assert_status 201 POST "$BASE_URL/api/v1/items" '{"nombre":"Item Test V2","valor":10.0,"porcentaje":0,"conceptoFijo":false}'

# ============================================================================
# SUMMARY
# ============================================================================
echo -e "${BLUE}==========================================${NC}"
echo -e "${BLUE}   Test Execution Summary${NC}"
echo -e "${BLUE}==========================================${NC}"
echo -e "${GREEN}All strict endpoint checks passed${NC}"
echo -e "${BLUE}==========================================${NC}\n"
echo -e "${GREEN}Test suite completed!${NC}\n"
