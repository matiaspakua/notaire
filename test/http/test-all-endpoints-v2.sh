#!/bin/bash

# ============================================================================
# Notaire API - Complete Test Suite
# ============================================================================

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

# Test counter
TESTS_RUN=0
TESTS_PASSED=0

# ============================================================================
# AUTHENTICATION
# ============================================================================
echo -e "${BLUE}=== TESTING AUTHENTICATION ===${NC}"

echo -e "${GREEN}Test 1: Login${NC}"
curl -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usuario": "admin", "password": "admin"}' \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

# ============================================================================
# USUARIOS
# ============================================================================
echo -e "${BLUE}=== TESTING USUARIOS ===${NC}"

echo -e "${GREEN}Test 2: Get all usuarios${NC}"
curl -X GET "$BASE_URL/api/v1/usuarios" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

echo -e "${GREEN}Test 3: Get usuario by ID${NC}"
curl -X GET "$BASE_URL/api/v1/usuarios/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

# ============================================================================
# CONCEPTOS
# ============================================================================
echo -e "${BLUE}=== TESTING CONCEPTOS ===${NC}"

echo -e "${GREEN}Test 4: Get all conceptos${NC}"
curl -X GET "$BASE_URL/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

echo -e "${GREEN}Test 5: Get concepto by ID${NC}"
curl -X GET "$BASE_URL/api/v1/conceptos/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

echo -e "${GREEN}Test 6: Create new concepto${NC}"
curl -X POST "$BASE_URL/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Concepto Test", "monto": 500.00}' \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

# ============================================================================
# PERSONAS
# ============================================================================
echo -e "${BLUE}=== TESTING PERSONAS ===${NC}"

echo -e "${GREEN}Test 7: Get all personas${NC}"
curl -X GET "$BASE_URL/api/v1/personas" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

echo -e "${GREEN}Test 8: Get persona by ID${NC}"
curl -X GET "$BASE_URL/api/v1/personas/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

# ============================================================================
# TRAMITES
# ============================================================================
echo -e "${BLUE}=== TESTING TRAMITES ===${NC}"

echo -e "${GREEN}Test 9: Get all tramites${NC}"
curl -X GET "$BASE_URL/api/v1/tramites" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

echo -e "${GREEN}Test 10: Get tramite by ID${NC}"
curl -X GET "$BASE_URL/api/v1/tramites/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

# ============================================================================
# ESCRITURAS
# ============================================================================
echo -e "${BLUE}=== TESTING ESCRITURAS ===${NC}"

echo -e "${GREEN}Test 11: Get all escrituras${NC}"
curl -X GET "$BASE_URL/api/v1/escrituras" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

echo -e "${GREEN}Test 12: Get escritura by ID${NC}"
curl -X GET "$BASE_URL/api/v1/escrituras/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

# ============================================================================
# PRESUPUESTOS
# ============================================================================
echo -e "${BLUE}=== TESTING PRESUPUESTOS ===${NC}"

echo -e "${GREEN}Test 13: Get all presupuestos${NC}"
curl -X GET "$BASE_URL/api/v1/presupuestos" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

echo -e "${GREEN}Test 14: Get presupuesto by ID${NC}"
curl -X GET "$BASE_URL/api/v1/presupuestos/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

# ============================================================================
# ITEMS
# ============================================================================
echo -e "${BLUE}=== TESTING ITEMS ===${NC}"

echo -e "${GREEN}Test 15: Get all items${NC}"
curl -X GET "$BASE_URL/api/v1/items" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"
((TESTS_RUN++))

# ============================================================================
# SUMMARY
# ============================================================================
echo -e "${BLUE}==========================================${NC}"
echo -e "${BLUE}   Test Execution Summary${NC}"
echo -e "${BLUE}==========================================${NC}"
echo -e "Total tests run: ${YELLOW}$TESTS_RUN${NC}"
echo -e "${BLUE}==========================================${NC}\n"
echo -e "${GREEN}Test suite completed!${NC}\n"
