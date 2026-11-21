#!/usr/bin/env bash
# Seeds a throwaway factory, writes an owner and a reader token for the e2e test, then serves it with writes on.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KIT_BIN="${AISDLC_BIN:-$HERE/../../../bin/aisdlc}"
WORK="${E2E_FACTORY_DIR:?set E2E_FACTORY_DIR}"
PORT="${E2E_KIT_PORT:-18865}"
rm -rf "$WORK" && mkdir -p "$WORK/repo"
cd "$WORK/repo"
git init -q && git config user.email e2e@example.invalid && git config user.name e2e
printf '[project]\nname = "e2e"\npolicy = "POLICY.md"\n\n[catalogs]\ndir = ".aisdlc/catalogs"\n' > aisdlc.toml
echo "# Policy" > POLICY.md
git add -A && git commit -qm seed
python3 "$KIT_BIN" demo seed >/dev/null
python3 "$KIT_BIN" project create --id storefront --repo-id web --url https://example.invalid/web --forge github >/dev/null
python3 "$KIT_BIN" run start --project storefront --workflow factory-feature --id checkout-flow >/dev/null
owner=$(python3 "$KIT_BIN" serve-token add --identity maria --scope owner --json)
reader=$(python3 "$KIT_BIN" serve-token add --identity sam --scope read --json)
printf '{"owner": %s, "reader": %s}\n' "$owner" "$reader" > "$WORK/tokens.json"
exec python3 "$KIT_BIN" serve --write --port "$PORT"
