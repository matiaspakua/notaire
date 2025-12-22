#!/bin/bash

# Test script for Item endpoints

echo "Testing Item Endpoints..."
echo "========================"

# Get all items
echo "1. GET all items"
curl -X GET "http://localhost:8080/api/v1/items" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Get item by ID
echo "2. GET item by ID (ID=1)"
curl -X GET "http://localhost:8080/api/v1/items/1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# Create new item
echo "3. POST new item"
curl -X POST "http://localhost:8080/api/v1/items" \
  -H "Content-Type: application/json" \
  -d '{
    "cantidad": 10,
    "precioUnitario": 100.50,
    "subTotal": 1005.00
  }' \
  -w "\nStatus: %{http_code}\n\n"

echo "Item tests completed."
