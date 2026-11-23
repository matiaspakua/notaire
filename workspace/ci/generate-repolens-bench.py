#!/usr/bin/env python3
"""Generate github-page/lib/repolens-bench-snapshot.json from committed RepoLens bench results (#2315).

WHY
---
The AI Factory Pages showcase must chart offline retrieval/agent benches and the live
factory-loop A/B table without inventing numbers. This script copies summaries from
framework/repolens/bench/results/ into a typed snapshot the Next app can import
offline. Headline factor labels reuse the same rounding as bench/report.py::_factor.

The ``generated_at`` timestamp only changes when the underlying data changes (#2429).
When the snapshot the script would write is identical to the existing file apart from
the timestamp, the existing ``generated_at`` is preserved so the tracked file remains
byte-identical and a Pages build does not dirty the git tree.

USAGE
  python3 workspace/ci/generate-repolens-bench.py
  python3 workspace/ci/generate-repolens-bench.py --check   # validate existing snapshot
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RESULTS = ROOT / "framework" / "repolens" / "bench" / "results"
OUTPUT = ROOT / "github-page" / "lib" / "repolens-bench-snapshot.json"

RETRIEVAL_FILES = {
    "tuning": "retrieval.json",
    "holdout": "retrieval-notaire-holdout.json",
}
AGENT_FILES = {
    "haikuTuning": "agent-haiku.json",
    "haikuHoldout": "agent-haiku-notaire-holdout.json",
    "sonnetHoldout": "agent-sonnet-holdout-0.3.0.json",
    "sonnetScale": "agent-sonnet-scale.json",
}
FACTORY_LOOP = "factory-loop-comparison.md"


def factor(before: float, after: float, higher_better: bool, word: str) -> str:
    """Match framework/repolens/bench/report.py::_factor."""
    if before == after:
        return "same"
    if not before or not after:
        return ""
    better = after > before if higher_better else after < before
    ratio = max(before, after) / min(before, after)
    if ratio >= 2:
        shown = f"{ratio:.0f}x" if ratio >= 10 else f"{ratio:.1f}x"
        return f"{shown} {word}" if better else f"{shown} worse"
    return f"{(after - before) / before:+.0%}"


def arm_metrics(summary: dict, arm: str) -> dict:
    row = summary[arm]
    return {
        "recall_at_5": row["recall@5"],
        "recall_at_10": row["recall@10"],
        "recall_at_20": row["recall@20"],
        "mrr": row["mrr"],
        "latency_ms": row["latency_ms"],
        "output_bytes": row["output_bytes"],
    }


def agent_arm(summary: dict, arm: str) -> dict:
    row = summary[arm]
    return {
        "recall": row["recall"],
        "tool_calls": row["tool_calls"],
        "turns": row["turns"],
        "duration_s": row["duration_s"],
        "cost_usd": row["cost_usd"],
        "input_tokens": row["input_tokens"],
        "output_tokens": row["output_tokens"],
        "runs": int(row["runs"]),
    }


def load_retrieval(name: str) -> dict:
    path = RESULTS / name
    data = json.loads(path.read_text(encoding="utf-8"))
    summary = data["summary"]
    return {
        "path": f"framework/repolens/bench/results/{name}",
        "taskset": data.get("taskset"),
        "commit": data.get("commit"),
        "tasks": int(data["tasks"]),
        "k": data.get("k"),
        "arms": {
            "grep": arm_metrics(summary, "grep"),
            "hybrid": arm_metrics(summary, "hybrid"),
            "graph": arm_metrics(summary, "graph"),
        },
    }


def load_agent(key: str, name: str) -> dict:
    path = RESULTS / name
    data = json.loads(path.read_text(encoding="utf-8"))
    summary = data["summary"]
    return {
        "path": f"framework/repolens/bench/results/{name}",
        "model": data.get("model"),
        "taskset": data.get("taskset"),
        "commit": data.get("commit"),
        "repeat": data.get("repeat"),
        "baseline": agent_arm(summary, "baseline"),
        "graph": agent_arm(summary, "graph"),
    }


def parse_factory_loop(md: str) -> dict:
    """Extract paired locate rows and the committed caveat prose."""
    pairs: list[dict] = []
    allowed_pass = {"grep", "repolens", "repolens search", "repolens impact"}
    for line in md.splitlines():
        if not line.startswith("|"):
            continue
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cells) < 6 or not cells[0].startswith("#"):
            continue
        # 9-col table: Issue, Order, Pass, Calls, Output bytes, Wall time, Named files, Hit rate, Notes
        # 7-col table: Issue, Order, Pass, Calls, Output bytes, Wall time, Hit rate, Notes
        issue, order, pass_name, calls, nbytes, wall = cells[:6]
        pass_name_l = pass_name.lower()
        if pass_name_l not in allowed_pass:
            continue
        if not calls.isdigit():
            continue
        if len(cells) >= 8 and "`" in cells[6]:
            named, hit = cells[6], cells[7]
        elif len(cells) >= 7:
            named, hit = None, cells[6]
        else:
            named, hit = None, ""
        pairs.append(
            {
                "issue": issue.split()[0],
                "label": issue,
                "order": order,
                "pass": pass_name_l,
                "calls": int(calls),
                "output_bytes": int(nbytes.replace(",", "")),
                "wall": wall,
                "hit_rate": hit,
                "named_files": named or None,
            }
        )

    caveat = (
        "Paired locate sample from live factory work. Open-to-merge time is dominated by the "
        "owner's merge, not by search. No percentage improvement is claimed for the factory-loop "
        "pairs when n is small — see factory-loop-comparison.md."
    )
    if "No percentage improvement is claimed" in md:
        caveat = (
            "No percentage improvement is claimed for the paired factory-loop sample when n is "
            "small (see factory-loop-comparison.md). Numbers are command output bytes and wall "
            "time, not source files read afterwards."
        )
    implemented = None
    m = re.search(r"Implemented sample[^\n]*\(n\s*=\s*(\d+)\s+issues", md)
    if m:
        implemented = int(m.group(1))
    return {"pairs": pairs, "implemented_issues_n": implemented, "caveat": caveat}


def build() -> dict:
    missing = [RESULTS / n for n in list(RETRIEVAL_FILES.values()) + list(AGENT_FILES.values()) + [FACTORY_LOOP]
               if not (RESULTS / n).exists()]
    if missing:
        raise SystemExit(f"missing bench sources: {', '.join(str(p) for p in missing)}")

    retrieval = {key: load_retrieval(name) for key, name in RETRIEVAL_FILES.items()}
    agents = {key: load_agent(key, name) for key, name in AGENT_FILES.items()}
    factory_md = (RESULTS / FACTORY_LOOP).read_text(encoding="utf-8")
    factory = parse_factory_loop(factory_md)

    t, h = retrieval["tuning"]["arms"], retrieval["holdout"]["arms"]
    sonnet = agents["sonnetHoldout"]
    headlines = {
        "tuning_recall": factor(t["grep"]["recall_at_10"], t["graph"]["recall_at_10"], True, "more"),
        "holdout_recall": factor(h["grep"]["recall_at_10"], h["graph"]["recall_at_10"], True, "more"),
        "latency": factor(t["grep"]["latency_ms"], t["graph"]["latency_ms"], False, "faster"),
        "bytes": factor(t["grep"]["output_bytes"] / 1024, t["graph"]["output_bytes"] / 1024, False, "less"),
        "sonnet_tool_calls": factor(
            sonnet["baseline"]["tool_calls"], sonnet["graph"]["tool_calls"], False, "fewer"
        ),
        "sonnet_recall": factor(sonnet["baseline"]["recall"], sonnet["graph"]["recall"], True, "more"),
    }

    sources = (
        [retrieval[k]["path"] for k in retrieval]
        + [agents[k]["path"] for k in agents]
        + [f"framework/repolens/bench/results/{FACTORY_LOOP}"]
    )
    return {
        "generated_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "method": "python3 workspace/ci/generate-repolens-bench.py",
        "sources": [{"path": p} for p in sources],
        "headlines": headlines,
        "retrieval": retrieval,
        "agents": agents,
        "factoryLoop": factory,
    }


def validate(snap: dict) -> None:
    required = ("generated_at", "method", "sources", "headlines", "retrieval", "agents", "factoryLoop")
    for key in required:
        if key not in snap:
            raise SystemExit(f"snapshot missing {key}")
    if snap["headlines"]["tuning_recall"] != "5.0x more":
        raise SystemExit(f"unexpected tuning_recall label: {snap['headlines']['tuning_recall']}")
    if snap["headlines"]["holdout_recall"] != "2.6x more":
        raise SystemExit(f"unexpected holdout_recall label: {snap['headlines']['holdout_recall']}")
    if snap["headlines"]["latency"] != "41x faster":
        raise SystemExit(f"unexpected latency label: {snap['headlines']['latency']}")
    if snap["headlines"]["bytes"] != "89x less":
        raise SystemExit(f"unexpected bytes label: {snap['headlines']['bytes']}")
    if snap["headlines"]["sonnet_tool_calls"] != "2.0x fewer":
        raise SystemExit(f"unexpected sonnet tool-call label: {snap['headlines']['sonnet_tool_calls']}")


def write(snap: dict, path: Path) -> bool:
    """Write *snap* to *path*; return False when the file already holds the same data.

    The timestamp follows the data (#2429): when only `generated_at` would differ, the
    existing file is left untouched, so a Pages build does not dirty the git tree.
    """
    if path.exists():
        try:
            existing = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            existing = None
        if isinstance(existing, dict) and "generated_at" in existing:
            if {**snap, "generated_at": existing["generated_at"]} == existing:
                return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(snap, indent=2) + "\n", encoding="utf-8")
    return True


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()
    if args.check:
        if not OUTPUT.exists():
            raise SystemExit(f"missing {OUTPUT}")
        validate(json.loads(OUTPUT.read_text(encoding="utf-8")))
        print(f"ok {OUTPUT}")
        return 0
    snap = build()
    validate(snap)
    if write(snap, OUTPUT):
        print(f"wrote {OUTPUT} ({len(snap['sources'])} sources)")
    else:
        print(f"unchanged {OUTPUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
