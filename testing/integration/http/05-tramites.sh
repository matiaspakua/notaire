#!/bin/bash
# Notaire API - Tramites Tests

BASE_URL="http://localhost:8080"

echo "=== TRAMITES - GET ALL ==="
curl -X GET "$BASE_URL/api/v1/tramites" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== TRAMITES - GET BY ID (1) ==="
curl -X GET "$BASE_URL/api/v1/tramites/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n"
