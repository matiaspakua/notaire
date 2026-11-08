#!/usr/bin/env bash
# Verifies the ai-sdlc-framework module only: its self-tests and the adopter profile. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

KIT="$ROOT/framework/ai-sdlc"
OPTIONAL_VENV="$KIT/.venv-telemetry"
OPTIONAL_REQ="$KIT/optional/telemetry.txt"

# Optional OTel packages prove the real NoOp / OTEL_SDK_DISABLED path (#2125). Kit runtime stays stdlib-only.
# Do not export PYTHONPATH into the parent shell — a leaked site-packages breaks Semgrep SEC-SAST-01.
TEST_ENV=()
if [[ -f "$OPTIONAL_REQ" ]]; then
  if [[ ! -x "$OPTIONAL_VENV/bin/python" ]]; then
    python3 -m venv "$OPTIONAL_VENV"
    "$OPTIONAL_VENV/bin/pip" install -q -r "$OPTIONAL_REQ"
  fi
  SITE="$("$OPTIONAL_VENV/bin/python" -c 'import site; print(site.getsitepackages()[0])')"
  TEST_ENV=("env" "PYTHONPATH=$SITE${PYTHONPATH:+:$PYTHONPATH}")
fi

"${TEST_ENV[@]}" python3 -m unittest discover -s framework/ai-sdlc/tests
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml doctor
