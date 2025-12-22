#!/bin/bash
# Notaire API - Presupuestos Tests

BASE_URL="http://localhost:8080"

echo "=== PRESUPUESTOS - GET ALL ==="
curl -X GET "$BASE_URL/api/v1/presupuestos" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== PRESUPUESTOS - GET BY ID (1) ==="
curl -X GET "$BASE_URL/api/v1/presupuestos/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n"
