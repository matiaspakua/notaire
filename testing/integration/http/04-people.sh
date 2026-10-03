#!/bin/bash
# Notaire API - People Tests

BASE_URL="http://localhost:8080"

echo "=== PEOPLE - GET ALL ==="
curl -X GET "$BASE_URL/api/v1/people" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== PEOPLE - GET BY ID (1) ==="
curl -X GET "$BASE_URL/api/v1/people/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== PEOPLE - CREATE ==="
curl -X POST "$BASE_URL/api/v1/people" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Carlos",
    "lastName": "López",
    "identificationNumber": "87654321",
    "telefonoPersonal": "555-9876",
    "telefonoLaboral": "555-4321",
    "direccion": "Avenida Central 456",
    "email": "carlos@example.com"
  }' \
  -w "\nStatus: %{http_code}\n"
