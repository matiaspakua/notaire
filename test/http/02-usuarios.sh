#!/bin/bash
# Notaire API - Usuarios Tests

BASE_URL="http://localhost:8080"

echo "=== USUARIOS - GET ALL ==="
curl -X GET "$BASE_URL/api/v1/usuarios" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== USUARIOS - GET BY ID (1) ==="
curl -X GET "$BASE_URL/api/v1/usuarios/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n"
