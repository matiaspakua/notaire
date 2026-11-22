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
# Profile with stages (one required gate, one advisory) and inputs/outputs for the production-line view.
cat > aisdlc.toml <<'TOML'
[project]
name = "e2e"
policy = "POLICY.md"

[catalogs]
dir = ".aisdlc/catalogs"

[[stages]]
id = "plan"
agents = ["planner"]
inputs = ["GitHub issue"]
outputs = ["OpenSpec change"]

[[stages]]
id = "red"
agents = ["tester"]
inputs = ["OpenSpec change"]
outputs = ["failing tests"]

[[stages]]
id = "green"
agents = ["implementer"]
inputs = ["failing tests"]
outputs = ["passing tests"]

[[gates]]
name = "tests pass"
stage = "green"
run = "echo ok"

[[gates]]
name = "lint check"
stage = "green"
run = "echo lint"
required = false
TOML
echo "# Policy" > POLICY.md
git add -A && git commit -qm seed
python3 "$KIT_BIN" demo seed >/dev/null
python3 "$KIT_BIN" project create --id storefront --repo-id web --url https://example.invalid/web --forge github >/dev/null
python3 "$KIT_BIN" run start --project storefront --workflow factory-feature --id checkout-flow >/dev/null

# Seed an intent from a Markdown spec file (for the intent bar e2e).
cat > "$WORK/spec.md" <<'SPEC'
# Dashboard Production Line

## Description
Add a production-line timeline view to the factory dashboard with stations, gates, and live sync.

## Acceptance criteria
- [ ] Timeline shows stations from /factory/line with gates between them
- [ ] Expanding a station shows inputs and outputs
- [ ] Live indicator shows SSE connection status

## References
- [Issue #2400](https://github.com/example/repo/issues/2400) (architecture)
- [ADR-006](https://example.invalid/adr-006) (adr)
SPEC
python3 "$KIT_BIN" intent submit --file "$WORK/spec.md" >/dev/null

# Record telemetry rows for the Metrics view e2e.
python3 "$KIT_BIN" telemetry record --stage plan --agent planner --metric duration_s --value 12.5 --ok >/dev/null
python3 "$KIT_BIN" telemetry record --stage plan --agent planner --metric usd --value 0.03 --ok >/dev/null
python3 "$KIT_BIN" telemetry record --stage red --agent tester --metric failure --value 1 >/dev/null

# Add a throwaway catalog entry that e2e can safely delete.
python3 "$KIT_BIN" catalog add --kind prompts --id throwaway-e2e --summary "E2E throwaway prompt" >/dev/null

owner=$(python3 "$KIT_BIN" serve-token add --identity maria --scope owner --json)
reader=$(python3 "$KIT_BIN" serve-token add --identity sam --scope read --json)
printf '{"owner": %s, "reader": %s}\n' "$owner" "$reader" > "$WORK/tokens.json"
exec python3 "$KIT_BIN" serve --write --port "$PORT"
