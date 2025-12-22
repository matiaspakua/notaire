#!/bin/bash
# ============================================================================
# Notaire API - Test Scripts using curl
# ============================================================================
# Base URL for the API
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# ============================================================================
# USUARIOS / AUTHENTICATION
# ============================================================================
echo -e "${BLUE}=== TESTING USUARIOS / AUTHENTICATION ===${NC}"

# Login
echo -e "${GREEN}Test 1: Login${NC}"
curl -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usuario": "admin",
    "password": "admin"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# Get all usuarios
echo -e "${GREEN}Test 2: Get all usuarios${NC}"
curl -X GET "$BASE_URL/api/v1/usuarios" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Get usuario by ID
echo -e "${GREEN}Test 3: Get usuario by ID (1)${NC}"
curl -X GET "$BASE_URL/api/v1/usuarios/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# ============================================================================
# CONCEPTOS
# ============================================================================
echo -e "${BLUE}=== TESTING CONCEPTOS ===${NC}"

# Get all conceptos
echo -e "${GREEN}Test 4: Get all conceptos${NC}"
curl -X GET "$BASE_URL/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Get concepto by ID
echo -e "${GREEN}Test 5: Get concepto by ID (1)${NC}"
curl -X GET "$BASE_URL/api/v1/conceptos/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Create new concepto
echo -e "${GREEN}Test 6: Create new concepto${NC}"
curl -X POST "$BASE_URL/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "Concepto de Prueba",
    "monto": 100.00
  }' \
  -w "\nStatus: %{http_code}\n\n"

# ============================================================================
# PERSONAS
# ============================================================================
echo -e "${BLUE}=== TESTING PERSONAS ===${NC}"

# Get all personas
echo -e "${GREEN}Test 7: Get all personas${NC}"
curl -X GET "$BASE_URL/api/v1/personas" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Get persona by ID
echo -e "${GREEN}Test 8: Get persona by ID (1)${NC}"
curl -X GET "$BASE_URL/api/v1/personas/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Create new persona
echo -e "${GREEN}Test 9: Create new persona${NC}"
curl -X POST "$BASE_URL/api/v1/personas" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "apellido": "García",
    "numeroIdentificacion": "12345678",
    "telefonoPersonal": "555-1234",
    "telefonoLaboral": "555-5678",
    "direccion": "Calle Principal 123",
    "email": "juan@example.com"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# ============================================================================
# TRAMITES
# ============================================================================
echo -e "${BLUE}=== TESTING TRAMITES ===${NC}"

# Get all tramites
echo -e "${GREEN}Test 10: Get all tramites${NC}"
curl -X GET "$BASE_URL/api/v1/tramites" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Get tramite by ID
echo -e "${GREEN}Test 11: Get tramite by ID (1)${NC}"
curl -X GET "$BASE_URL/api/v1/tramites/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# ============================================================================
# ESCRITURAS
# ============================================================================
echo -e "${BLUE}=== TESTING ESCRITURAS ===${NC}"

# Get all escrituras
echo -e "${GREEN}Test 12: Get all escrituras${NC}"
curl -X GET "$BASE_URL/api/v1/escrituras" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Get escritura by ID
echo -e "${GREEN}Test 13: Get escritura by ID (1)${NC}"
curl -X GET "$BASE_URL/api/v1/escrituras/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# ============================================================================
# PRESUPUESTOS
# ============================================================================
echo -e "${BLUE}=== TESTING PRESUPUESTOS ===${NC}"

# Get all presupuestos
echo -e "${GREEN}Test 14: Get all presupuestos${NC}"
curl -X GET "$BASE_URL/api/v1/presupuestos" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Get presupuesto by ID
echo -e "${GREEN}Test 15: Get presupuesto by ID (1)${NC}"
curl -X GET "$BASE_URL/api/v1/presupuestos/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# ============================================================================
# SWAGGER DOCUMENTATION
# ============================================================================
echo -e "${BLUE}=== SWAGGER DOCUMENTATION ===${NC}"
echo -e "${GREEN}Swagger UI: $BASE_URL/swagger-ui.html${NC}"
echo -e "${GREEN}API Docs JSON: $BASE_URL/v3/api-docs${NC}"

echo -e "\n${GREEN}✓ All tests completed!${NC}"
