#!/bin/bash
# Notaire API - Conceptos Tests

set -euo pipefail

BASE_URL="http://localhost:8080"

assert_status() {
  local expected="$1"
  local method="$2"
  local url="$3"
  local data="${4:-}"

  local status
  if [ -n "$data" ]; then
    status=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" -d "$data")
  else
    status=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" -H "Content-Type: application/json")
  fi

  echo "$method $url -> $status"
  if [ "$status" -ne "$expected" ]; then
    echo "Expected status $expected but got $status"
    exit 1
  fi
}

echo "=== CONCEPTOS - GET ALL ==="
assert_status 200 GET "$BASE_URL/api/v1/conceptos"

echo "=== CONCEPTOS - GET BY ID (1) ==="
assert_status 200 GET "$BASE_URL/api/v1/conceptos/1"

echo "=== CONCEPTOS - CREATE ==="
assert_status 200 POST "$BASE_URL/api/v1/conceptos" '{
  "nombre": "Concepto Test API",
  "valor": 150.50
}'

echo "Conceptos tests passed"
