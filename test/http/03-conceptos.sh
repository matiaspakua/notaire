#!/bin/bash
# Notaire API - Conceptos Tests

BASE_URL="http://localhost:8080"

echo "=== CONCEPTOS - GET ALL ==="
curl -X GET "$BASE_URL/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== CONCEPTOS - GET BY ID (1) ==="
curl -X GET "$BASE_URL/api/v1/conceptos/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== CONCEPTOS - CREATE ==="
curl -X POST "$BASE_URL/api/v1/conceptos" \
  -H "Content-Type: application/json" \
  -d '{
    "descripcion": "Concepto Test",
    "monto": 150.50
  }' \
  -w "\nStatus: %{http_code}\n"
