#!/bin/bash

# Notaire Infrastructure Startup Script
# Brings up the Observability + SonarQube stack (infra/observability/docker-compose.yml) and
# connects it to the already-running Notaire application network.
#
# Reads ALL credentials from a single git-ignored env file (see common.sh for
# the lookup order; template: infra/.env.example).

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
. "$SCRIPT_DIR/common.sh"

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

ENV_FILE="$(resolve_env_file)"
INFRA_COMPOSE="$OBSERVABILITY_COMPOSE"

# Determine docker compose command
if docker compose version &> /dev/null 2>&1; then
    DC_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    DC_CMD="docker-compose"
else
    echo -e "${RED}✗ Docker Compose is not installed${NC}"
    exit 1
fi

# Ensure a local .env exists (create from example on first run)
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${YELLOW}No env file found — creating $ENV_FILE from infra/.env.example${NC}"
    cp "$INFRA_DIR/.env.example" "$ENV_FILE"
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Notaire Infrastructure Startup${NC}"
echo -e "${BLUE}========================================${NC}\n"

# The infra stack attaches to the application network (external). The app must
# be up first so that network exists.
if ! docker network inspect "$APP_NETWORK" &> /dev/null; then
    echo -e "${RED}✗ Application network '$APP_NETWORK' not found.${NC}"
    echo -e "  Start the application first: ${YELLOW}bash scripts/start.sh${NC}"
    exit 1
fi

echo -e "${YELLOW}Starting infrastructure services...${NC}"
$DC_CMD --env-file "$ENV_FILE" -f "$INFRA_COMPOSE" up -d --build
echo -e "${GREEN}✓ Infra containers started${NC}\n"

# Wait for the key HTTP services to become ready.
wait_for() {
    local name="$1" url="$2" max="${3:-60}" auth="${4:-}"
    echo -ne "${YELLOW}Waiting for $name...${NC}"
    for i in $(seq 1 "$max"); do
        local code
        if [ -n "$auth" ]; then
            code=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -u "$auth" "$url" 2>/dev/null || echo 000)
        else
            code=$(curl -s -m 5 -o /dev/null -w '%{http_code}' "$url" 2>/dev/null || echo 000)
        fi
        if [ "$code" = "200" ]; then
            echo -e " ${GREEN}ready${NC}"
            return 0
        fi
        echo -n "."
        sleep 3
    done
    echo -e " ${RED}timeout${NC} (still starting — check: docker logs <container>)"
    return 1
}

wait_for "Prometheus" "http://localhost:9090/-/ready" 30 || true
wait_for "Loki"       "http://localhost:3100/ready"   40 || true
wait_for "Grafana"    "http://localhost:3001/api/health" 40 || true
wait_for "SonarQube"  "http://localhost:9000/api/system/status" 80 || true

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ Infrastructure is up${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${BLUE}Observability:${NC}"
echo -e "  Homer Dashboard:  ${YELLOW}http://localhost:8888${NC}"
echo -e "  Grafana:          ${YELLOW}http://localhost:3001${NC}  (\$GRAFANA_ADMIN_USER / \$GRAFANA_ADMIN_PASSWORD)"
echo -e "  Prometheus:       ${YELLOW}http://localhost:9090${NC}"
echo -e "  Loki:             ${YELLOW}http://localhost:3100${NC}"
echo -e "  Postgres Exporter:${YELLOW}http://localhost:9187/metrics${NC}"
echo -e "${BLUE}Quality:${NC}"
echo -e "  SonarQube:        ${YELLOW}http://localhost:9000${NC}  (\$SONAR_ADMIN_USER / \$SONAR_ADMIN_PASSWORD)"
echo ""
echo -e "  Run code analysis: ${YELLOW}bash $SCRIPT_DIR/run-sonar.sh${NC}"
echo -e "  Health check:      ${YELLOW}bash $SCRIPT_DIR/check-infra.sh${NC}"
echo ""
