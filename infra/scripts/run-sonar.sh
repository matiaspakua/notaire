#!/bin/bash

# Notaire — Run SonarQube static analysis against the backend module.
#
# Handles SonarQube Community first-run flow automatically:
#   1. Waits for SonarQube to be UP.
#   2. Changes the default admin password (Sonar forces this on first login).
#   3. Generates an analysis token (idempotent).
#   4. Runs `mvn sonar:sonar` for backend-api so SonarQube shows real data.
#
# Credentials come from the single root .env file.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_DIR"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'

# shellcheck disable=SC1091
set -a; [ -f "$REPO_DIR/.env" ] && . "$REPO_DIR/.env"; set +a

SONAR_URL="http://localhost:9000"
SONAR_USER="${SONAR_ADMIN_USER:-admin}"
SONAR_DEFAULT_PASS="admin"
SONAR_NEW_PASS="${SONAR_ADMIN_PASSWORD:-admin}"
PROJECT_KEY="notaire-backend"

echo -e "${BLUE}=== SonarQube analysis for Notaire backend ===${NC}\n"

# 1. Wait for SonarQube to report UP
echo -ne "${YELLOW}Waiting for SonarQube to be UP...${NC}"
for i in $(seq 1 80); do
    STATUS=$(curl -s -m 5 "$SONAR_URL/api/system/status" 2>/dev/null | sed -n 's/.*"status":"\([A-Z]*\)".*/\1/p')
    if [ "$STATUS" = "UP" ]; then echo -e " ${GREEN}UP${NC}"; break; fi
    echo -n "."; sleep 5
    if [ "$i" = "80" ]; then echo -e " ${RED}timeout${NC}"; exit 1; fi
done

# Helper: does a given password authenticate?
auth_works() {
    local pass="$1"
    local code
    code=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -u "$SONAR_USER:$pass" "$SONAR_URL/api/authentication/validate" 2>/dev/null)
    [ "$code" = "200" ] && curl -s -m 5 -u "$SONAR_USER:$pass" "$SONAR_URL/api/authentication/validate" | grep -q '"valid":true'
}

# 2. Establish a working password
if auth_works "$SONAR_NEW_PASS"; then
    WORKING_PASS="$SONAR_NEW_PASS"
    echo -e "${GREEN}✓ Authenticated with configured password${NC}"
elif auth_works "$SONAR_DEFAULT_PASS"; then
    echo -e "${YELLOW}First login — changing default admin password...${NC}"
    curl -s -u "$SONAR_USER:$SONAR_DEFAULT_PASS" -X POST \
        "$SONAR_URL/api/users/change_password" \
        --data-urlencode "login=$SONAR_USER" \
        --data-urlencode "previousPassword=$SONAR_DEFAULT_PASS" \
        --data-urlencode "password=$SONAR_NEW_PASS" >/dev/null || true
    if auth_works "$SONAR_NEW_PASS"; then
        WORKING_PASS="$SONAR_NEW_PASS"
        echo -e "${GREEN}✓ Password updated${NC}"
    else
        WORKING_PASS="$SONAR_DEFAULT_PASS"
    fi
else
    echo -e "${RED}✗ Could not authenticate to SonarQube with configured or default password${NC}"
    exit 1
fi

# 3. Generate / reuse an analysis token
if [ -z "${SONAR_TOKEN:-}" ]; then
    echo -e "${YELLOW}Generating analysis token...${NC}"
    # Revoke any existing token with the same name, then (re)generate.
    curl -s -u "$SONAR_USER:$WORKING_PASS" -X POST \
        "$SONAR_URL/api/user_tokens/revoke" --data-urlencode "name=notaire-ci" >/dev/null 2>&1 || true
    SONAR_TOKEN=$(curl -s -u "$SONAR_USER:$WORKING_PASS" -X POST \
        "$SONAR_URL/api/user_tokens/generate" --data-urlencode "name=notaire-ci" \
        | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
    if [ -n "$SONAR_TOKEN" ]; then
        # Persist token back into .env for reuse
        if grep -q '^SONAR_TOKEN=' "$REPO_DIR/.env" 2>/dev/null; then
            sed -i.bak "s|^SONAR_TOKEN=.*|SONAR_TOKEN=$SONAR_TOKEN|" "$REPO_DIR/.env" && rm -f "$REPO_DIR/.env.bak"
        else
            echo "SONAR_TOKEN=$SONAR_TOKEN" >> "$REPO_DIR/.env"
        fi
        echo -e "${GREEN}✓ Token generated and saved to .env${NC}"
    fi
fi

if [ -z "$SONAR_TOKEN" ]; then
    echo -e "${RED}✗ Failed to obtain a Sonar token${NC}"
    exit 1
fi

# 4. Run analysis (jacoco report first so coverage is reported)
echo -e "${YELLOW}Running Maven build + Sonar analysis (this can take a few minutes)...${NC}"
mvn -B -pl backend-api -am clean test \
    org.jacoco:jacoco-maven-plugin:report \
    org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.host.url="$SONAR_URL" \
    -Dsonar.token="$SONAR_TOKEN" \
    -Dsonar.projectKey="$PROJECT_KEY" \
    -Dsonar.projectName="Notaire Backend" \
    -Dsonar.coverage.jacoco.xmlReportPaths=backend-api/target/site/jacoco/jacoco.xml

echo ""
echo -e "${GREEN}✓ Analysis submitted.${NC} View results at ${YELLOW}$SONAR_URL/dashboard?id=$PROJECT_KEY${NC}"
