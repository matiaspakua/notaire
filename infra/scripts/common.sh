#!/bin/bash
# Shared path and env resolution for the infra scripts. Source it, do not run it.
#
# Everything is resolved from infra/ itself. The only optional couplings to the
# application are the root .env fallback and the application checkout used by
# run-sonar.sh; both can be overridden through the variables below.

INFRA_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OBSERVABILITY_COMPOSE="$INFRA_DIR/observability/docker-compose.yml"
APP_NETWORK="${NOTAIRE_APP_NETWORK:-notaire_notary-network}"
APP_DIR="${NOTAIRE_APP_DIR:-$INFRA_DIR/..}" # INFRA_ROOT_ENV_FALLBACK

# Env file precedence: INFRA_ENV_FILE, infra/.env, then the monorepo root .env.
resolve_env_file() {
    if [ -n "${INFRA_ENV_FILE:-}" ]; then
        echo "$INFRA_ENV_FILE"
    elif [ -f "$INFRA_DIR/.env" ]; then
        echo "$INFRA_DIR/.env"
    elif [ -f "$APP_DIR/.env" ]; then
        echo "$APP_DIR/.env"
    else
        echo "$INFRA_DIR/.env"
    fi
}
