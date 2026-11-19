#!/usr/bin/env python3
"""Federated search: does asking every indexed repository cost accuracy against asking the right one?

    python3 bench/federation_bench.py --label NAME

Stores: this repository (Notaire, held-out tasks) and the scale corpus (spring-framework + vscode, scale tasks),
both registered by an index run. For each task: recall@10 and MRR of the expected files when only the owning
store answers, and when `repos.search(..., "all")` merges every registered store with each relevance
temperature in `--tau` (`inf` = unweighted interleaving); plus the federated latency. Relevance and hits are
fetched once per task; only the fusion differs between temperatures.
"""

from __future__ import annotations

import argparse
import json
import math
import re
import statistics
import sys
import time
from pathlib import Path
from unittest.mock import patch

import yaml

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
sys.path.insert(0, str(HERE))

from corpus import bench_home  # noqa: E402

from repolens import repos  # noqa: E402
from repolens.service import KnowledgeGraph  # noqa: E402


def score(paths: list[str], expected: list[str]) -> tuple[float, float]:
    found = [any(re.search(rx, p) for p in paths[:10]) for rx in expected]
    first = next((r for r, p in enumerate(paths, 1) if any(re.search(rx, p) for rx in expected)), None)
    return sum(found) / len(expected), (1.0 / first if first else 0.0)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--label", required=True)
    ap.add_argument("--tau", default="0.005,0.01,0.02,0.05,0.1,inf", help="relevance temperatures to compare")
    args = ap.parse_args()
    taus = [float(t) for t in args.tau.split(",")]
    configured = repos.TAU
    root = HERE.parents[3]
    sets = [
        ("notaire-holdout", yaml.safe_load((HERE / "tasks" / "notaire-holdout.yaml").read_text())["tasks"],
         KnowledgeGraph.open(root / "repolens.toml")),
        ("scale", yaml.safe_load((HERE / "tasks" / "scale.yaml").read_text())["tasks"],
         KnowledgeGraph.open(bench_home() / "scale" / "scale-fastembed.toml")),
    ]
    registered = [e["name"] for e in repos.listed()]
    result: dict = {"label": args.label, "stamp": time.strftime("%Y-%m-%d %H:%M"), "registered": registered, "sets": {}}
    for name, tasks, kg in sets:
        own, fed, ms = [], {t: [] for t in taus}, []
        for task in tasks:
            clock = time.perf_counter()  # first, so no store has this query's vector memoised yet
            repos.TAU = configured
            repos.search(kg, task["prompt"], 20, "all")  # as shipped: irrelevant repositories are not searched
            ms.append((time.perf_counter() - clock) * 1000)
            alone = [h.path for h in kg.engine.search(task["prompt"], k=20, mode="files")]
            own.append(score(alone, task["expected"]))
            answers = []
            repos.TAU = math.inf  # every repository searched, so each temperature fuses the same answers
            with patch.object(repos, "fuse", lambda a, _k, into=answers: into.extend(a) or []):
                repos.search(kg, task["prompt"], 20, "all")
            for tau in taus:
                repos.TAU = tau
                fed[tau].append(score([h.path for _n, h in repos.fuse(answers, 20)], task["expected"]))
        result["sets"][name] = {
            "tasks": len(tasks),
            "own_store": {"recall@10": round(statistics.mean(r for r, _ in own), 3),
                          "mrr": round(statistics.mean(m for _, m in own), 3)},
            "all_repos": {str(tau): {"recall@10": round(statistics.mean(r for r, _ in fed[tau]), 3),
                                     "mrr": round(statistics.mean(m for _, m in fed[tau]), 3)} for tau in taus},
            "federated_p50_ms": round(statistics.median(ms), 1),
            "tau": configured,
        }
        kg.close()
    out = HERE / "results" / f"federation-{args.label}.json"
    out.write_text(json.dumps(result, indent=1), encoding="utf-8")
    print(json.dumps(result, indent=1))
    return 0


if __name__ == "__main__":
    sys.exit(main())
