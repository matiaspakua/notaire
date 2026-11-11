#!/bin/bash

# Ensure .env has a local NOTAIRE_MFA_ENCRYPTION_KEY so OPTIONAL MFA enrolment
# and TS-0046 work after stack start (ADR-032 / #1640). Empty values become a
# random Base64 32-byte key; any other value is kept. Never commit .env.
# Usage: ensure-mfa-encryption-key.sh <env-file>

set -e

ENV_FILE="${1:?usage: ensure-mfa-encryption-key.sh <env-file>}"
[ -f "$ENV_FILE" ] || : > "$ENV_FILE"

current="$(grep -E '^NOTAIRE_MFA_ENCRYPTION_KEY=' "$ENV_FILE" | tail -1 | cut -d= -f2- | tr -d '\r' || true)"
case "$current" in
    "") ;;
    *) exit 0 ;;
esac

generated="$(openssl rand -base64 32)"
tmp="$(mktemp)"
grep -vE '^NOTAIRE_MFA_ENCRYPTION_KEY=' "$ENV_FILE" > "$tmp" || true
printf 'NOTAIRE_MFA_ENCRYPTION_KEY=%s\n' "$generated" >> "$tmp"
cat "$tmp" > "$ENV_FILE"
rm -f "$tmp"
echo "Generated a random NOTAIRE_MFA_ENCRYPTION_KEY in $ENV_FILE"
