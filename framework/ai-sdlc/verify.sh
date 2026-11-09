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
# Optional means optional (#2180): hosts without ensurepip/python3-venv must still run the stdlib suite.
TEST_ENV=()

_otel_venv_usable() {
  [[ -x "$OPTIONAL_VENV/bin/python" && -x "$OPTIONAL_VENV/bin/pip" ]]
}

_bootstrap_optional_otel() {
  if [[ ! -f "$OPTIONAL_REQ" ]]; then
    return 0
  fi

  if _otel_venv_usable; then
    return 0
  fi

  # Leftover half-venv (python symlink, no pip) from a prior ensurepip failure — drop it.
  if [[ -e "$OPTIONAL_VENV" ]]; then
    rm -rf "$OPTIONAL_VENV"
  fi

  if [[ -n "${AISDLC_TEST_FORCE_VENV_FAIL:-}" ]]; then
    echo "WARN: optional OpenTelemetry venv forced failure (AISDLC_TEST_FORCE_VENV_FAIL) — continuing stdlib-only" >&2
    return 0
  fi

  local err
  err="$(mktemp)"
  if ! python3 -m venv "$OPTIONAL_VENV" >"$err" 2>&1; then
    echo "WARN: optional OpenTelemetry venv unavailable (continuing stdlib-only): $(tr '\n' ' ' <"$err")" >&2
    rm -rf "$OPTIONAL_VENV"
    rm -f "$err"
    return 0
  fi
  rm -f "$err"

  if ! "$OPTIONAL_VENV/bin/pip" install -q -r "$OPTIONAL_REQ"; then
    echo "WARN: optional OpenTelemetry pip install failed — continuing stdlib-only" >&2
    rm -rf "$OPTIONAL_VENV"
    return 0
  fi
}

_bootstrap_optional_otel

if _otel_venv_usable; then
  SITE="$("$OPTIONAL_VENV/bin/python" -c 'import site; print(site.getsitepackages()[0])')"
  TEST_ENV=("env" "PYTHONPATH=$SITE${PYTHONPATH:+:$PYTHONPATH}")
fi

if [[ -n "${AISDLC_TEST_VERIFY_CMD:-}" ]]; then
  # Test-only override (#2180): run a short command instead of the full suite.
  "${TEST_ENV[@]}" bash -c "$AISDLC_TEST_VERIFY_CMD"
else
  "${TEST_ENV[@]}" python3 -m unittest discover -s framework/ai-sdlc/tests
fi
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml doctor
