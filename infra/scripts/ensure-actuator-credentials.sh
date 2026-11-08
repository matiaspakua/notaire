#!/bin/bash

# Make sure the env file holds a usable actuator scrape credential (issue #1657).
# A missing or change-me ACTUATOR_PASSWORD becomes a random value; any other value is kept.
# Usage: ensure-actuator-credentials.sh <env-file>

set -e

ENV_FILE="${1:?usage: ensure-actuator-credentials.sh <env-file>}"
[ -f "$ENV_FILE" ] || : > "$ENV_FILE"

current="$(grep -E '^ACTUATOR_PASSWORD=' "$ENV_FILE" | tail -1 | cut -d= -f2- | tr -d '\r' || true)"
case "$current" in
    ""|change-me*) ;;
    *) exit 0 ;;
esac

generated="$(openssl rand -hex 24)"
tmp="$(mktemp)"
grep -vE '^ACTUATOR_PASSWORD=' "$ENV_FILE" > "$tmp" || true
printf 'ACTUATOR_PASSWORD=%s\n' "$generated" >> "$tmp"
cat "$tmp" > "$ENV_FILE"
rm -f "$tmp"
grep -qE '^ACTUATOR_USER=' "$ENV_FILE" || printf 'ACTUATOR_USER=notaire-metrics\n' >> "$ENV_FILE"
echo "Generated a random ACTUATOR_PASSWORD in $ENV_FILE"
