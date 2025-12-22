#!/bin/bash
# Notaire API - Authentication Tests

BASE_URL="http://localhost:8080"

echo "=== LOGIN TEST ==="
echo "POST /api/v1/auth/login"
curl -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usuario": "admin",
    "password": "admin"
  }' \
  -w "\nStatus: %{http_code}\n"
