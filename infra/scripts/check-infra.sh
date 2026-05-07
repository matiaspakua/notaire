#!/bin/bash

# DevSecOps Infra + Notaire Health Check Script
# This script verifies that all infrastructure services AND
# the Notaire application services are responding.

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASSED=0
FAILED=0
TOTAL=0

check_service() {
    NAME="$1"
    URL="$2"
    EXPECTED_CODE="${3:-200}"
    TOTAL=$((TOTAL + 1))
    
    echo -n "Checking $NAME... "
    
    HTTP_CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' "$URL" 2>/dev/null)
    
    if [ "$HTTP_CODE" = "$EXPECTED_CODE" ]; then
        echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED + 1))
    else
        echo -e "${RED}FAILED${NC} (Expected $EXPECTED_CODE, got $HTTP_CODE)"
        FAILED=$((FAILED + 1))
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
check_service "Dashboard (Homer)" "http://localhost:80" 200
check_service "SonarQube" "http://localhost:9000/api/system/status" 200
check_service "Jenkins" "http://localhost:8082/login" 200
check_service "Nexus" "http://localhost:8081" 200
check_service "Dependency-Track" "http://localhost:8085" 200
check_service "Prometheus" "http://localhost:9090" 200
check_service "Grafana" "http://localhost:3001/api/health" 200
check_service "Loki" "http://localhost:3100/ready" 200
check_service "PostgreSQL Exporter" "http://localhost:9187/metrics" 200

echo ""
echo ">>> Notaire Application"
echo "----------------------------------------"
check_service "Backend API Health" "http://localhost:8080/actuator/health" 200
check_service "Backend API Liveness" "http://localhost:8080/actuator/health/liveness" 200
check_service "Backend API Readiness" "http://localhost:8080/actuator/health/readiness" 200
check_service "Backend API Metrics" "http://localhost:8080/actuator/prometheus" 200
check_service "Backend API Info" "http://localhost:8080/actuator/info" 200
check_service "Swagger UI" "http://localhost:8080/swagger-ui.html" 200
check_service "pgAdmin" "http://localhost:5050" 200
check_service "PostgreSQL" "http://localhost:5432" 200

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
