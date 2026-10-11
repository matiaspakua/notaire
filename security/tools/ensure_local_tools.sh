#!/usr/bin/env bash
# Install the local open-source security tools used by security/run_gates.py.
# Binaries land in security/.tools/ (gitignored). Versions are pinned.
#
#   bash security/tools/ensure_local_tools.sh
#   bash security/tools/ensure_local_tools.sh --semgrep
#   bash security/tools/ensure_local_tools.sh --trivy
#
# Semgrep needs the Python venv module (python3-venv). Trivy is the upstream
# release binary. Re-running the script does not reinstall a tool that is
# already present at the pinned version.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TOOLS="$ROOT/security/.tools"
SEMGREP_VERSION="1.180.0"
TRIVY_VERSION="0.75.0"
WANT_SEMGREP=1
WANT_TRIVY=1

if [ "${1:-}" = "--semgrep" ]; then
  WANT_TRIVY=0
elif [ "${1:-}" = "--trivy" ]; then
  WANT_SEMGREP=0
elif [ -n "${1:-}" ]; then
  echo "usage: bash security/tools/ensure_local_tools.sh [--semgrep|--trivy]" >&2
  exit 2
fi

mkdir -p "$TOOLS/bin"

if [ "$WANT_SEMGREP" -eq 1 ]; then
  SEMGREP="$TOOLS/venv/bin/semgrep"
  if [ ! -x "$SEMGREP" ] || ! "$SEMGREP" --version 2>/dev/null | grep -q "$SEMGREP_VERSION"; then
    if ! python3 -m venv "$TOOLS/venv"; then
      echo "python3 -m venv failed. Install python3-venv and run this script again." >&2
      exit 1
    fi
    "$TOOLS/venv/bin/pip" install --disable-pip-version-check "semgrep==${SEMGREP_VERSION}"
  fi
  echo "$SEMGREP"
fi

if [ "$WANT_TRIVY" -eq 1 ]; then
  TRIVY="$TOOLS/bin/trivy"
  if [ ! -x "$TRIVY" ] || ! "$TRIVY" --version 2>/dev/null | grep -q "$TRIVY_VERSION"; then
    curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh \
      | sh -s -- -b "$TOOLS/bin" "v${TRIVY_VERSION}"
  fi
  echo "$TRIVY"
fi
