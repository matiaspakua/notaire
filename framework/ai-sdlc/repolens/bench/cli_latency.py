#!/usr/bin/env python3
"""Wall time of CLI queries as an agent runs them (a new process per call, through bin/repolens).

    python3 bench/cli_latency.py --label NAME [--runs 5]

Three query types from the factory loop: a prose search, an exact symbol, and `impact`. One warm-up call
first (it absorbs a refresh when HEAD moved). For comparison, `rg` for the same exact symbol. Results land in
bench/results/perf/cli-<label>.json.
"""

from __future__ import annotations

import argparse
import json
import statistics
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
LAUNCHER = HERE.parent / "bin" / "repolens"
QUERIES = {
    "search_prose": ["search", "where are payments processed", "-k", "5"],
    "search_exact": ["search", "ProcessPaymentService", "-k", "5"],
    "impact": ["impact", "ProcessPaymentService.process"],
}


def timed(cmd: list[str], cwd: Path) -> tuple[float, int, int]:
    clock = time.perf_counter()
    out = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True)
    return time.perf_counter() - clock, out.returncode, len(out.stdout.encode())


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--label", required=True)
    ap.add_argument("--runs", type=int, default=5)
    args = ap.parse_args()
    root = Path(subprocess.run(["git", "rev-parse", "--show-toplevel"], cwd=HERE, capture_output=True,
                               text=True).stdout.strip())
    timed(["bash", str(LAUNCHER), *QUERIES["search_exact"]], root)
    result: dict = {"label": args.label, "stamp": time.strftime("%Y-%m-%d %H:%M"), "runs": args.runs, "queries": {}}
    for name, query in QUERIES.items():
        samples = [timed(["bash", str(LAUNCHER), *query], root) for _ in range(args.runs)]
        walls = [s[0] for s in samples]
        result["queries"][name] = {"p50_s": round(statistics.median(walls), 3), "max_s": round(max(walls), 3),
                                   "exit": samples[-1][1], "bytes": samples[-1][2]}
    rg = [timed(["rg", "-n", "class ProcessPaymentService", "backend-api/src"], root)[0] for _ in range(args.runs)]
    result["rg_exact_p50_s"] = round(statistics.median(rg), 4)
    out = HERE / "results" / "perf" / f"cli-{args.label}.json"
    out.write_text(json.dumps(result, indent=1), encoding="utf-8")
    print(json.dumps(result, indent=1))
    return 0


if __name__ == "__main__":
    sys.exit(main())
