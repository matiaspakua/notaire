#!/bin/bash

# DevSecOps Infra Health Check Script
# This script verifies that all infrastructure services are responding.

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

SERVICES=(
    "Dashboard:http://localhost:80"
    "SonarQube:http://localhost:9000/api/system/status"
    "Jenkins:http://localhost:8082/login"
    "Nexus:http://localhost:8081"
    "Dependency-Track:http://localhost:8085"
    "Prometheus:http://localhost:9090"
    "Grafana:http://localhost:3001/api/health"
    "Loki:http://localhost:3100/ready"
)

echo "--- Starting DevSecOps Infrastructure Health Check ---"

for entry in "${SERVICES[@]}"; do
    NAME="${entry%%:*}"
    URL="${entry#*:}"
    
    echo -n "Checking $NAME... "
    
    if curl -s --head --request GET "$URL" | grep "200 OK" > /dev/null || curl -s "$URL" | grep -E "(UP|OK|ready)" > /dev/null; then
        echo -e "${GREEN}PASSED${NC}"
    else
        echo -e "${RED}FAILED${NC} (Check logs with: docker logs devsecops-${NAME,,})"
    fi
done

echo "------------------------------------------------------"
