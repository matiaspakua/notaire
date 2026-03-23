#!/bin/bash
# Notaire API - Escrituras Tests

BASE_URL="http://localhost:8080"

echo "=== ESCRITURAS - GET ALL ==="
curl -X GET "$BASE_URL/api/v1/escrituras" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== ESCRITURAS - GET BY ID (1) ==="
curl -X GET "$BASE_URL/api/v1/escrituras/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n"
