#!/usr/bin/env bash
# Runs the whole benchmark: retrieval (free, deterministic) + agent runs (uses the Claude CLI: it costs money
# and counts against the account's usage limits; the agent step stops at the first usage-limit error)
# and renders bench/results/report.html. Usage: bench/run_all.sh [tasks.yaml] [models...]
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TASKS="${1:-$HERE/tasks/notaire.yaml}"
shift || true
MODELS=("${@:-sonnet}")
REPEAT="${REPEAT:-3}"
PARALLEL="${PARALLEL:-4}"

python3 "$HERE/retrieval_bench.py" "$TASKS"
if command -v claude >/dev/null 2>&1 && [[ "${SKIP_AGENT:-0}" != "1" ]]; then
  for model in "${MODELS[@]}"; do
    python3 "$HERE/agent_bench.py" "$TASKS" --model "$model" --repeat "$REPEAT" --parallel "$PARALLEL"
  done
else
  echo "skipping agent benchmark (no claude CLI or SKIP_AGENT=1)" >&2
fi
python3 "$HERE/report.py"
