#!/usr/bin/env python3
"""Refresh the factory's evidence with one command, and say what is stale (#2469).

WHY
---
The RepoLens benches, the Pages snapshots and the factory metrics were each refreshed by their own
script, when someone remembered. A checkout behind origin/main also served an old RepoLens to every
agent without a warning. This runs the refreshes in order and checks freshness by version and git
ancestry, not by timestamp (timestamps follow the data, #2429).

USAGE
  python3 workspace/ci/refresh-factory-evidence.py               # free refresh, in order
  python3 workspace/ci/refresh-factory-evidence.py --agent       # also the paid agent bench
  python3 workspace/ci/refresh-factory-evidence.py --history     # also history, delivery and roadmap from GitHub (needs gh)
  python3 workspace/ci/refresh-factory-evidence.py --dry-run     # print the steps only
  python3 workspace/ci/refresh-factory-evidence.py --check       # exit 1 when anything is stale
  python3 workspace/ci/refresh-factory-evidence.py --check --brief   # engines and bench stamps only (session start)

The ledger, the costs and the store are git-ignored and live in the main worktree, so the kit steps read
them there (--evidence-root overrides) even when the refresh runs in a linked worktree.
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RESULTS = ROOT / "framework" / "repolens" / "bench" / "results"
REPOLENS_INIT = ROOT / "framework" / "repolens" / "repolens" / "__init__.py"
ENGINES = (("RepoLens", "framework/repolens"), ("aisdlc", "framework/ai-sdlc"))
BASE = "origin/main"
KIT = ["python3", "framework/ai-sdlc/bin/aisdlc", "--profile", ".aisdlc/aisdlc.toml"]
BENCH = ["bash", "workspace/sdlc/repolens-bench.sh"]
JUNIE_LOGS = Path.home() / ".junie" / "logs"


def evidence_root(root: Path = ROOT) -> Path:
    """The main worktree: the first entry of `git worktree list`."""
    listing = subprocess.run(["git", "-C", str(root), "worktree", "list", "--porcelain"],
                             capture_output=True, text=True, check=True).stdout
    return Path(listing.splitlines()[0].removeprefix("worktree "))


def steps(agent: bool = False, history: bool = False, evidence: Path | None = None) -> list[tuple[str, list[str]]]:
    own = ["python3", "workspace/ci/refresh-factory-evidence.py", "--evidence-root", str(evidence or ROOT)]
    plan = [
        ("repolens index", ["bash", "framework/repolens/bin/repolens", "index"]),
        ("retrieval bench", [*BENCH, "retrieval"]),
        ("holdout bench", [*BENCH, "holdout"]),
    ]
    if agent:
        plan.append(("agent bench", [*BENCH, "agent"]))
    plan += [
        ("bench report", ["python3", "framework/repolens/bench/report.py"]),
        ("pages bench snapshot", ["python3", "workspace/ci/generate-repolens-bench.py"]),
        ("junie usage", [*own, "--import-junie-usage"]),
        ("factory metrics snapshot", [*own, "--factory-metrics"]),
    ]
    if history:
        plan += [
            ("ai sdlc history", ["python3", "workspace/ci/generate-ai-sdlc-history.py"]),
            ("delivery metrics", ["python3", "workspace/ci/generate-delivery-metrics.py"]),
            ("roadmap snapshot", ["python3", "workspace/sdlc/apply_milestone_policy.py", "--snapshot"]),
        ]
    return plan


def repolens_version(init: Path = REPOLENS_INIT) -> str:
    match = re.search(r'__version__\s*=\s*"([^"]+)"', init.read_text(encoding="utf-8"))
    return match.group(1) if match else "unknown"


def stale_results(results: Path, version: str) -> list[str]:
    stale = []
    for path in sorted(results.glob("retrieval*.json")):
        stamp = json.loads(path.read_text(encoding="utf-8")).get("repolens_version")
        if stamp != version:
            stale.append(f"{path.name} was produced by RepoLens {stamp or 'unknown'}, the checkout has {version}")
    return stale


def engines_behind(root: Path = ROOT, base: str = BASE) -> list[str]:
    if subprocess.run(["git", "-C", str(root), "rev-parse", "--verify", "--quiet", base],
                      capture_output=True).returncode != 0:
        return []
    behind = []
    for name, path in ENGINES:
        count = subprocess.run(["git", "-C", str(root), "rev-list", "--count", f"HEAD..{base}", "--", path],
                               capture_output=True, text=True, check=True).stdout.strip()
        if int(count or 0):
            behind.append(f"{name} in this checkout is behind {base} by {count} commit(s) touching {path}")
    return behind


def kit(evidence: Path) -> list[str]:
    return [*KIT[:2], "--root", str(evidence), "--profile", str(evidence / ".aisdlc" / "aisdlc.toml")]


def import_junie_usage(evidence: Path) -> int:
    runs = json.loads(subprocess.run([*kit(evidence), "run", "list", "--json"], cwd=ROOT, check=True,
                                     capture_output=True, text=True).stdout)
    for run in runs.get("entries", []):
        result = subprocess.run([*kit(evidence), "usage", "import", "--source", "junie", "--path",
                                 str(JUNIE_LOGS), "--run", run["id"]], cwd=ROOT, capture_output=True, text=True)
        if result.returncode != 0:
            print(f"usage import failed for {run['id']}: {result.stderr.strip()}", file=sys.stderr)
            return result.returncode
    return 0


def factory_metrics(evidence: Path) -> int:
    report = subprocess.run([*kit(evidence), "metrics", "factory", "--json"], cwd=ROOT, check=True,
                            capture_output=True, text=True).stdout
    with tempfile.NamedTemporaryFile("w", suffix=".json", delete=False) as handle:
        handle.write(report)
    return subprocess.run(["python3", "workspace/ci/generate-factory-metrics.py", "--from", handle.name],
                          cwd=ROOT).returncode


def check(brief: bool) -> int:
    problems = engines_behind() + stale_results(RESULTS, repolens_version())
    if not brief:
        for script in ("generate-repolens-bench.py", "generate-factory-metrics.py"):
            result = subprocess.run(["python3", f"workspace/ci/{script}", "--check"], cwd=ROOT,
                                    capture_output=True, text=True)
            if result.returncode != 0:
                problems.append(f"{script} --check: {(result.stdout + result.stderr).strip()[:300]}")
    for problem in problems:
        print(f"  STALE  {problem}")
    if problems:
        print("evidence: stale — update the checkout (git pull) and run "
              "python3 workspace/ci/refresh-factory-evidence.py")
        return 1
    print("evidence: engines level with origin/main and bench results from the current RepoLens")
    return 0


def refresh(plan: list[tuple[str, list[str]]], dry_run: bool) -> int:
    for name, argv in plan:
        print(f"== {name}: {' '.join(argv)}", flush=True)
        if dry_run:
            continue
        code = subprocess.run(argv, cwd=ROOT).returncode
        if code != 0:
            print(f"refresh stopped: {name} exited {code}", file=sys.stderr)
            return code
    return 0


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--agent", action="store_true", help="also run the paid agent bench")
    parser.add_argument("--history", action="store_true", help="also the AI SDLC history, delivery metrics and roadmap snapshot from GitHub (needs gh)")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--check", action="store_true", help="report stale engines and evidence; exit 1 if any")
    parser.add_argument("--brief", action="store_true", help="with --check: engines and bench stamps only")
    parser.add_argument("--evidence-root", type=Path, help="checkout holding .aisdlc evidence (default: main worktree)")
    parser.add_argument("--import-junie-usage", action="store_true", help=argparse.SUPPRESS)
    parser.add_argument("--factory-metrics", action="store_true", help=argparse.SUPPRESS)
    args = parser.parse_args(argv)
    evidence = args.evidence_root or evidence_root()
    if args.import_junie_usage:
        return import_junie_usage(evidence)
    if args.factory_metrics:
        return factory_metrics(evidence)
    if args.check:
        return check(args.brief)
    return refresh(steps(args.agent, args.history, evidence), args.dry_run)


if __name__ == "__main__":
    raise SystemExit(main())
