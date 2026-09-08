#!/bin/bash
# Notaire API - Personas Tests

BASE_URL="http://localhost:8080"

echo "=== PERSONAS - GET ALL ==="
curl -X GET "$BASE_URL/api/v1/personas" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== PERSONAS - GET BY ID (1) ==="
curl -X GET "$BASE_URL/api/v1/personas/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=== PERSONAS - CREATE ==="
curl -X POST "$BASE_URL/api/v1/personas" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Carlos",
    "apellido": "López",
    "numeroIdentificacion": "87654321",
    "telefonoPersonal": "555-9876",
    "telefonoLaboral": "555-4321",
    "direccion": "Avenida Central 456",
    "email": "carlos@example.com"
  }' \
  -w "\nStatus: %{http_code}\n"
