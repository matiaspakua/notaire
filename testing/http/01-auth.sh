#!/bin/bash
# Notaire API - Authentication Tests

set -euo pipefail

BASE_URL="http://localhost:8080"

echo "=== LOGIN TEST ==="
echo "POST /api/v1/usuarios/login"

status=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/usuarios/login" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "admin",
    "contrasenia": "admin"
  }')

echo "Status: $status"
if [ "$status" -ne 200 ]; then
  echo "Login test failed"
  exit 1
fi

echo "Login test passed"
