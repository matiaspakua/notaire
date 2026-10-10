#!/usr/bin/env python3
"""Measure always-loaded agent context tokens (#1259).

Install at workspace/ci/agent-context-budget.py (REPO_ROOT = parents[2]).

Usage:
  python3 workspace/ci/agent-context-budget.py
  python3 workspace/ci/agent-context-budget.py --max-tokens 8000
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]

# Post-#1259 always-loaded set (keep in sync with AGENTS packing docs).
ALWAYS_LOADED = [
    "CLAUDE.md",
    "AGENTS.md",
    "CONSTITUTION-AGENT-CARD.md",
    ".claude/rules/ai-agent-workflow.md",
]


def measure(root: Path = REPO_ROOT) -> dict:
    rows = []
    total = 0
    for rel in ALWAYS_LOADED:
        p = root / rel
        if not p.is_file():
            rows.append({"path": rel, "bytes": 0, "missing": True})
            continue
        n = p.stat().st_size
        total += n
        rows.append({"path": rel, "bytes": n, "missing": False})
    return {"files": rows, "bytes": total, "tokens_est": total // 4}


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--max-tokens", type=int, default=0)
    ap.add_argument("--json", action="store_true")
    ap.add_argument("--root", type=Path, default=None)
    args = ap.parse_args(argv)
    data = measure(args.root or REPO_ROOT)
    if args.json:
        print(json.dumps(data, indent=2))
    else:
        for row in data["files"]:
            flag = " MISSING" if row["missing"] else ""
            print(f"{row['bytes']:8d}  {row['path']}{flag}")
        print(f"TOTAL_BYTES={data['bytes']} TOKENS_EST={data['tokens_est']}")
    if args.max_tokens and data["tokens_est"] > args.max_tokens:
        print(f"FAIL: {data['tokens_est']} > max {args.max_tokens}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
