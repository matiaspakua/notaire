#!/usr/bin/env bash
#
# native.sh — run the CI stack without Docker (#1665, CU76).
#
# WHY
# ---
# A Claude Code cloud session has no Docker daemon, so Playwright, Bruno and the database
# checks were treated as CI-only. PostgreSQL 16, Java, Node and Chromium are installed there,
# and running the stack by hand caught a PostgreSQL-only regression that H2 tests missed.
# This script is that setup, with the backend values CI uses (pinned by
# workspace/tests/test_native_stack.py against .github/workflows/playwright-e2e.yml).
#
# USAGE
#   bash workspace/stack/native.sh up       start PostgreSQL, backend, frontend; shim Playwright browsers
#   bash workspace/stack/native.sh down     stop what `up` started
#   bash workspace/stack/native.sh status   exit 0 only when all three answer
#   bash workspace/stack/native.sh env      print the variables the test suites read
#
# Then:  cd testing/e2e && npx playwright test --project=smoke
#        cd backend-api/api-test && npx @usebruno/cli run --env Development --env-var token="$TOKEN"
#        psql -h localhost -U notaire -d notaire -v exporter=notaire_exporter -f testing/database/checks/10_history.sql
#
# State (PID files, logs) lives in $NOTAIRE_NATIVE_DIR (default ~/.cache/notaire-native).
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
STATE="${NOTAIRE_NATIVE_DIR:-$HOME/.cache/notaire-native}"

JWT_SECRET='ci-e2e-only-jwt-secret-do-not-use-in-prod-!!'
ACTUATOR_USER=ci-e2e-actuator-user
ACTUATOR_PASSWORD=ci-e2e-actuator-pass
DB_URL=jdbc:postgresql://localhost:5432/notaire
DB_USER=notaire
DB_PASSWORD=notaire
BACKEND_URL=http://localhost:8080/api/v1
BASE_URL=http://localhost:3000

usage() { sed -n '3,22p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'; }

say() { printf '%s\n' "$*"; }
fail() { printf 'native: %s\n' "$*" >&2; exit 1; }

pg_bin() {
    local dir
    dir="$(ls -d /usr/lib/postgresql/*/bin 2>/dev/null | sort -V | tail -1)"
    [ -n "$dir" ] && printf '%s' "$dir"
}

pg_data() {
    if [ "$(id -u)" -eq 0 ]; then
        printf '/var/lib/postgresql/notaire-data'
    else
        printf '%s/pgdata' "$STATE"
    fi
}

pg_log() {
    if [ "$(id -u)" -eq 0 ]; then
        printf '/var/lib/postgresql/notaire-pg.log'
    else
        printf '%s/postgres.log' "$STATE"
    fi
}

as_pg_user() {
    if [ "$(id -u)" -eq 0 ]; then su postgres -c "$*"; else bash -c "$*"; fi
}

pg_running() {
    local bin
    bin="$(pg_bin)" || return 1
    [ -n "$bin" ] && [ -d "$(pg_data)" ] && as_pg_user "$bin/pg_ctl -D $(pg_data) status" >/dev/null 2>&1
}

pg_up() {
    local bin data
    bin="$(pg_bin)"
    [ -n "$bin" ] || fail "PostgreSQL server binaries not found under /usr/lib/postgresql"
    data="$(pg_data)"
    pg_running && return 0
    if [ ! -d "$data" ]; then
        mkdir -p "$data"
        [ "$(id -u)" -eq 0 ] && chown postgres:postgres "$data"
        as_pg_user "$bin/initdb -D $data -A trust -E UTF8 --locale=C.UTF-8" >/dev/null || fail "initdb failed"
    fi
    as_pg_user "$bin/pg_ctl -D $data -o '-p 5432 -k /tmp' -l $(pg_log) start" >/dev/null \
        || fail "PostgreSQL did not start (see $(pg_log))"
    touch "$STATE/pg.started"
    for _ in $(seq 1 20); do
        psql -h localhost -U postgres -tAc 'select 1' >/dev/null 2>&1 && break
        sleep 1
    done
    psql -h localhost -U postgres -tAc "select 1 from pg_roles where rolname='$DB_USER'" | grep -q 1 \
        || psql -h localhost -U postgres -qc "create role $DB_USER login superuser password '$DB_PASSWORD'" || fail "role"
    psql -h localhost -U postgres -tAc "select 1 from pg_database where datname='$DB_USER'" | grep -q 1 \
        || psql -h localhost -U postgres -qc "create database $DB_USER owner $DB_USER" || fail "database"
}

backend_jar() {
    ls "$REPO_ROOT"/backend-api/target/backend-api-*.jar 2>/dev/null | grep -v -- '-plain' | head -1
}

backend_up() {
    curl -fs localhost:8080/actuator/health >/dev/null 2>&1 && return 0
    local jar
    jar="$(backend_jar)"
    if [ -z "$jar" ]; then
        (cd "$REPO_ROOT" && mvn -q -B package -DskipTests -pl backend-api) || fail "backend build failed"
        jar="$(backend_jar)"
    fi
    setsid nohup java -jar "$jar" \
        --server.port=8080 \
        --spring.datasource.url="$DB_URL" \
        --spring.datasource.username="$DB_USER" \
        --spring.datasource.password="$DB_PASSWORD" \
        --spring.jpa.hibernate.ddl-auto=update \
        --jwt.secret="$JWT_SECRET" \
        --actuator.security.username="$ACTUATOR_USER" \
        --actuator.security.password="$ACTUATOR_PASSWORD" \
        > "$STATE/backend.log" 2>&1 &
    echo $! > "$STATE/backend.pid"
    for _ in $(seq 1 90); do
        curl -fs localhost:8080/actuator/health >/dev/null 2>&1 && return 0
        sleep 2
    done
    fail "backend did not become healthy (see $STATE/backend.log)"
}

frontend_up() {
    curl -fs -o /dev/null localhost:3000/login 2>/dev/null && return 0
    (
        cd "$REPO_ROOT/frontend" || exit 1
        [ -d node_modules ] || npm ci --no-audit --no-fund >/dev/null
        [ -d .next ] || npm run build >/dev/null
        BACKEND_URL="$BACKEND_URL" setsid nohup npm run start > "$STATE/frontend.log" 2>&1 &
        echo $! > "$STATE/frontend.pid"
    ) || fail "frontend start failed"
    for _ in $(seq 1 30); do
        curl -fs -o /dev/null localhost:3000/login 2>/dev/null && return 0
        sleep 1
    done
    fail "frontend did not answer (see $STATE/frontend.log)"
}

# The pinned Playwright asks for the "chrome" channel and a headless-shell revision the cloud
# image does not ship; point both at the Chromium that is installed. Only creates missing links.
playwright_shims() {
    local browsers="${PLAYWRIGHT_BROWSERS_PATH:-/opt/pw-browsers}"
    [ -d "$browsers" ] || return 0
    local chromium
    chromium="$(ls -d "$browsers"/chromium-*/chrome-linux 2>/dev/null | sort -V | tail -1)"
    if [ -n "$chromium" ] && [ ! -e /opt/google/chrome/chrome ]; then
        mkdir -p /opt/google/chrome 2>/dev/null && ln -sfn "$chromium/chrome" /opt/google/chrome/chrome 2>/dev/null \
            || say "native: could not link the chrome channel (needs write access to /opt/google)"
    fi
    local manifest="$REPO_ROOT/testing/e2e/node_modules/playwright-core/browsers.json"
    [ -f "$manifest" ] || return 0
    local wanted installed
    wanted="$(python3 -c "import json,sys;print(next(b['revision'] for b in json.load(open(sys.argv[1]))['browsers'] if b['name']=='chromium-headless-shell'))" "$manifest" 2>/dev/null)"
    installed="$(ls -d "$browsers"/chromium_headless_shell-*/chrome-linux 2>/dev/null | sort -V | tail -1)"
    if [ -n "$wanted" ] && [ -n "$installed" ] && [ ! -e "$browsers/chromium_headless_shell-$wanted" ]; then
        mkdir -p "$browsers/chromium_headless_shell-$wanted"
        ln -sfn "$installed/headless_shell" "$installed/chrome-headless-shell"
        ln -sfn "$installed" "$browsers/chromium_headless_shell-$wanted/chrome-headless-shell-linux64"
    fi
}

print_env() {
    cat <<EOF
export BASE_URL=$BASE_URL
export BACKEND_URL=$BACKEND_URL
export JWT_SECRET='$JWT_SECRET'
export ACTUATOR_USER=$ACTUATOR_USER
export ACTUATOR_PASSWORD=$ACTUATOR_PASSWORD
export PGHOST=localhost PGUSER=$DB_USER PGDATABASE=$DB_USER
EOF
}

stop_pid() {
    local file="$STATE/$1.pid"
    [ -f "$file" ] || return 0
    kill -- "-$(cat "$file")" 2>/dev/null || kill "$(cat "$file")" 2>/dev/null
    rm -f "$file"
}

cmd_status() {
    local ok=0
    pg_running && say "postgres: up" || { say "postgres: down"; ok=1; }
    curl -fs localhost:8080/actuator/health >/dev/null 2>&1 && say "backend: up" || { say "backend: down"; ok=1; }
    curl -fs -o /dev/null localhost:3000/login 2>/dev/null && say "frontend: up" || { say "frontend: down"; ok=1; }
    return $ok
}

cmd_up() {
    mkdir -p "$STATE"
    pg_up
    backend_up
    frontend_up
    playwright_shims
    say "native stack is up; variables for the suites:"
    print_env
}

cmd_down() {
    stop_pid frontend
    stop_pid backend
    if [ -f "$STATE/pg.started" ] && pg_running; then
        as_pg_user "$(pg_bin)/pg_ctl -D $(pg_data) stop" >/dev/null
        rm -f "$STATE/pg.started"
    fi
    say "native stack stopped"
}

case "${1:-}" in
    up) cmd_up ;;
    down) cmd_down ;;
    status) cmd_status ;;
    env) print_env ;;
    -h|--help|help) usage ;;
    *) usage >&2; exit 2 ;;
esac
