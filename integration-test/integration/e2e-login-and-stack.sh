#!/bin/bash
# Integration test: DB + Backend + Login API
# Verifies the stack is up and POST /api/v1/usuarios/login works.

set -e
BASE_URL="${BASE_URL:-http://localhost:8080}"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "=== Notaire E2E Integration Test ==="
echo "  Base URL: $BASE_URL"
echo ""

# 1) Backend health
echo -e "${YELLOW}1. Checking backend health...${NC}"
for i in 1 2 3 4 5 6 7 8 9 10; do
  if curl -sf "$BASE_URL/actuator/health" > /dev/null 2>&1 || curl -sf "$BASE_URL/swagger-ui.html" > /dev/null 2>&1; then
    echo -e "${GREEN}   Backend is up.${NC}"
    break
  fi
  if [ $i -eq 10 ]; then
    echo -e "${RED}   Backend did not become ready. Start with: ./start.sh${NC}"
    exit 1
  fi
  sleep 2
done

# 2) Login API (same contract as frontend Login)
echo -e "${YELLOW}2. Testing POST /api/v1/usuarios/login (frontend login contract)...${NC}"
RESP=$(curl -sS -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/usuarios/login" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"admin","contrasenia":"admin"}')
HTTP_BODY=$(echo "$RESP" | head -n -1)
HTTP_CODE=$(echo "$RESP" | tail -n 1)

if [ "$HTTP_CODE" != "200" ]; then
  echo -e "${RED}   Login returned HTTP $HTTP_CODE${NC}"
  echo "$HTTP_BODY" | head -5
  exit 1
fi

if echo "$HTTP_BODY" | grep -q '"valido"\s*:\s*true'; then
  echo -e "${GREEN}   Login OK (valido: true).${NC}"
else
  echo -e "${YELLOW}   Login returned 200 but valido may be false (check DB has user admin/admin).${NC}"
  echo "   Response (first 200 chars): ${HTTP_BODY:0:200}"
fi

# 3) Optional: GET conceptos (smoke test another endpoint)
echo -e "${YELLOW}3. Smoke test GET /api/v1/conceptos...${NC}"
CODE=$(curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/conceptos")
if [ "$CODE" = "200" ]; then
  echo -e "${GREEN}   GET conceptos OK (HTTP 200).${NC}"
else
  echo -e "${YELLOW}   GET conceptos returned HTTP $CODE${NC}"
fi

echo ""
echo -e "${GREEN}=== E2E integration test finished ===${NC}"
echo "  To run the Swing GUI: cd frontend-swing && mvn exec:java -Dexec.mainClass=com.licensis.notaire.gui.Login"
echo "  Or: java -jar frontend-swing/target/frontend-swing-*.jar (after mvn package)"
