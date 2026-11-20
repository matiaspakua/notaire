#!/usr/bin/env bash
# Retrieval (free, deterministic) plus agent runs when a provider can authenticate.
# The agent step uses bench/providers.py: Anthropic when `claude` is logged in or
# ANTHROPIC_API_KEY is set, otherwise the Cursor agent CLI. It stops at the first
# usage-limit error. Usage: bench/run_all.sh tasks.yaml [models...]
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ $# -lt 1 ]]; then
  echo "usage: bench/run_all.sh tasks.yaml [models...]" >&2
  exit 2
fi
TASKS="$1"
shift
MODELS=("${@:-sonnet}")
REPEAT="${REPEAT:-3}"
PARALLEL="${PARALLEL:-4}"

python3 "$HERE/retrieval_bench.py" "$TASKS"
if [[ "${SKIP_AGENT:-0}" == "1" ]]; then
  echo "skipping agent benchmark (SKIP_AGENT=1)" >&2
elif python3 "$HERE/providers.py" probe >/dev/null; then
  for model in "${MODELS[@]}"; do
    python3 "$HERE/agent_bench.py" "$TASKS" --model "$model" --repeat "$REPEAT" --parallel "$PARALLEL"
  done
else
  echo "skipping agent benchmark (no authenticated anthropic or cursor provider)" >&2
  python3 "$HERE/providers.py" probe >&2 || true
fi
python3 "$HERE/report.py"
