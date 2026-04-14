#!/bin/bash

# Test script for Item endpoints

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

echo "Testing Item Endpoints..."
echo "========================"

# Get all items
echo "1. GET all items"
assert_status 200 GET "$BASE_URL/api/v1/items"

# Get item by ID
echo "2. GET item by ID (ID=1)"
assert_status 200 GET "$BASE_URL/api/v1/items/1"

# Create new item
echo "3. POST new item"
assert_status 200 POST "$BASE_URL/api/v1/items" '{
  "nombre": "Item test API",
  "valor": 100.50,
  "conceptoFijo": false
}'

echo "Item tests completed."
