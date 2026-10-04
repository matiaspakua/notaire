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
BODY_FILE="$(mktemp)"
trap 'rm -f "$BODY_FILE"' EXIT
HTTP_CODE=$(curl -sS -o "$BODY_FILE" -w "%{http_code}" -X POST "$BASE_URL/api/v1/usuarios/login" \
  -H "Content-Type: application/json" \
  -d '{"name":"admin","password":"admin"}')
HTTP_BODY=$(cat "$BODY_FILE")

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

# 3) Authorization: anonymous calls are rejected, the login token is accepted
echo -e "${YELLOW}3. Smoke test GET /api/v1/conceptos: anonymous rejected, login token accepted...${NC}"
TOKEN=$(printf '%s' "$HTTP_BODY" | tr -d '\n' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -z "$TOKEN" ]; then
  echo -e "${RED}   Login response carries no token.${NC}"
  exit 1
fi
ANON_CODE=$(curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/conceptos")
AUTH_CODE=$(curl -sS -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/v1/conceptos")
if [ "$ANON_CODE" = "401" ] && [ "$AUTH_CODE" = "200" ]; then
  echo -e "${GREEN}   Anonymous HTTP $ANON_CODE, authenticated HTTP $AUTH_CODE.${NC}"
else
  echo -e "${RED}   Expected anonymous 401 and authenticated 200, got $ANON_CODE and $AUTH_CODE.${NC}"
  exit 1
fi

echo ""
echo -e "${GREEN}=== E2E integration test finished ===${NC}"
echo "  Active UI client: cd frontend && npm run dev"
echo "  Active UI E2E:    bash testing/scripts/run.sh e2e"
echo "  Swing desktop E2E is retired (#811 / ADR-012)."
