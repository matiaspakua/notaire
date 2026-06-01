#!/bin/bash

# DevSecOps Infra + Notaire Health Check Script
# This script verifies that all infrastructure services AND
# the Notaire application services are responding.

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASSED=0
FAILED=0
TOTAL=0

check_service() {
    NAME="$1"
    URL="$2"
    EXPECTED_CODE="${3:-200}"
    AUTH="${4:-}"
    TOTAL=$((TOTAL + 1))

    echo -n "Checking $NAME... "

    CURL_OPTS="-s -m 5 -o /dev/null -w '%{http_code}' -L"
    if [ -n "$AUTH" ]; then
        CURL_OPTS="$CURL_OPTS -u $AUTH"
    fi

    HTTP_CODE=$(eval curl $CURL_OPTS "$URL" 2>/dev/null)

    if [ "$HTTP_CODE" = "$EXPECTED_CODE" ]; then
        echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED + 1))
    elif [ "$HTTP_CODE" = "000" ]; then
        echo -e "${RED}FAILED${NC} (Connection refused - service not running)"
        FAILED=$((FAILED + 1))
    else
        echo -e "${RED}FAILED${NC} (Expected $EXPECTED_CODE, got $HTTP_CODE)"
        FAILED=$((FAILED + 1))
    fi
}

check_postgres() {
    NAME="PostgreSQL"
    TOTAL=$((TOTAL + 1))
    echo -n "Checking $NAME... "

    if docker exec notary-postgres pg_isready -U admin -d notaire >/dev/null 2>&1; then
        echo -e "${GREEN}PASSED${NC} (accepting connections)"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}FAILED${NC} (not accepting connections)"
        FAILED=$((FAILED + 1))
    fi
}

check_container() {
    NAME="$1"
    CONTAINER="$2"
    TOTAL=$((TOTAL + 1))
    echo -n "Checking $NAME... "

    if docker ps --format '{{.Names}}' | grep -q "^$CONTAINER$"; then
        STATUS=$(docker inspect --format='{{.State.Status}}' "$CONTAINER" 2>/dev/null)
        HEALTH=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$CONTAINER" 2>/dev/null)
        if [ "$STATUS" = "running" ]; then
            echo -e "${GREEN}PASSED${NC} (running, health: $HEALTH)"
            PASSED=$((PASSED + 1))
        else
            echo -e "${RED}FAILED${NC} (status: $STATUS)"
            FAILED=$((FAILED + 1))
        fi
    else
        echo -e "${YELLOW}SKIPPED${NC} (not running)"
        # Don't count as failure for optional services
    fi
}

echo ""
echo "=============================================================="
echo "  Notaire System - Complete Health Check"
echo "  $(date)"
echo "=============================================================="
echo ""

echo ">>> DevSecOps Infrastructure"
echo "----------------------------------------"
check_service "Dashboard (Homer)" "http://localhost:8888" 200
check_service "SonarQube" "http://localhost:9000/api/system/status" 200
check_service "Prometheus" "http://localhost:9090/-/ready" 200
check_service "Grafana" "http://localhost:3001/api/health" 200
check_service "Loki" "http://localhost:3100/ready" 200
check_service "PostgreSQL Exporter" "http://localhost:9187/metrics" 200
check_container "Promtail" "devsecops-promtail"

echo ""
echo ">>> Notaire Application"
echo "----------------------------------------"
check_service "Backend API Health" "http://localhost:8080/actuator/health" 200
check_service "Backend API Liveness" "http://localhost:8080/actuator/health/liveness" 200
check_service "Backend API Readiness" "http://localhost:8080/actuator/health/readiness" 200
check_service "Backend API Metrics" "http://localhost:8080/actuator/prometheus" 200 "admin:admin"
check_service "Backend API Info" "http://localhost:8080/actuator/info" 200
check_service "Swagger UI" "http://localhost:8080/swagger-ui/index.html" 200
check_container "pgAdmin" "notary-pgadmin"
check_postgres

echo ""
echo "=============================================================="
echo "  Health Check Summary"
echo "=============================================================="
echo "  Total Checks : $TOTAL"
echo -e "  Passed       : ${GREEN}$PASSED${NC}"
echo -e "  Failed       : ${RED}$FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "  ${GREEN}>>> ALL SERVICES OPERATIONAL${NC}"
else
    echo -e "  ${YELLOW}>>> $FAILED service(s) not responding${NC}"
    echo "  Check logs with: docker logs <container-name>"
fi
echo "=============================================================="
