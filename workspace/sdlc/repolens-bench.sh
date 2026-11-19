#!/usr/bin/env bash
# Adopter entrypoints for RepoLens benches. The runners themselves take paths;
# this script supplies this repository's task files.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BENCH="$ROOT/framework/ai-sdlc/repolens/bench"
TASKS="$BENCH/tasks/notaire.yaml"
HOLD="$BENCH/tasks/notaire-holdout.yaml"
cmd="${1:-}"
shift || true
case "$cmd" in
  retrieval)
    python3 "$BENCH/retrieval_bench.py" "$TASKS" "$@"
    ;;
  holdout)
    python3 "$BENCH/retrieval_bench.py" "$HOLD" "$@"
    ;;
  agent)
    python3 "$BENCH/agent_bench.py" "${1:-$TASKS}" "${@:2}"
    ;;
  probe)
    python3 "$BENCH/providers.py" probe "$@"
    ;;
  perf)
    python3 "$BENCH/perf_bench.py" --tasks "tune=$TASKS" --tasks "holdout=$HOLD" "$@"
    ;;
  callers)
    python3 "$BENCH/caller_bench.py" --module backend-api "$@"
    ;;
  latency)
    python3 "$BENCH/cli_latency.py" --rg-pattern "class ProcessPaymentService" --rg-path backend-api/src "$@"
    ;;
  *)
    echo "usage: repolens-bench.sh retrieval|holdout|agent|probe|perf|callers|latency [args]" >&2
    exit 2
    ;;
esac
