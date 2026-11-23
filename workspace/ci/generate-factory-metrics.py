#!/usr/bin/env python3
"""Generate github-page/lib/factory-metrics.json from `aisdlc metrics factory --json` (#2447).

The evidence ledger, the cost rows and the RepoLens usage log are local (git-ignored), so the Pages
build cannot compute these numbers on GitHub. Run this on the factory's machine and commit the
snapshot. It keeps the answers (time, tokens, money and RepoLens per intent and overall) and drops
internals. The file only changes when the data does: `generated_at` follows the data (#2429).

USAGE
  python3 workspace/ci/generate-factory-metrics.py                 # run the kit, write the snapshot
  python3 workspace/ci/generate-factory-metrics.py --from report.json
  python3 workspace/ci/generate-factory-metrics.py --check         # validate the committed snapshot
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "github-page" / "lib" / "factory-metrics.json"
KIT = [sys.executable, str(ROOT / "framework/ai-sdlc/bin/aisdlc"), "--profile", ".aisdlc/aisdlc.toml",
       "metrics", "factory", "--json"]


def _tokens(counts: dict) -> int:
    return sum(int(counts.get(k) or 0) for k in ("input", "output", "cache_read", "cache_write"))


def _cost_note(cost: dict) -> str:
    if cost.get("usd") is None:
        return "none"
    if not cost.get("fully_priced"):
        return "partial"
    return "reported" if cost.get("reported_rows") == cost.get("rows") else "priced"


def slim(report: dict) -> dict:
    overall = report["overall"]
    per_intent = overall["per_intent"]
    live = next(iter((report["repolens_effect"].get("live_ab") or {}).get("metrics", {}).values()), None) or {}
    return {
        "generated_at": report["generated_at"],
        "method": "python3 workspace/ci/generate-factory-metrics.py (aisdlc metrics factory --json)",
        "overall": {
            "intents": overall["intents"],
            "runs": overall["runs"],
            "succeeded": overall["succeeded"],
            "corrections": overall["time"]["corrections"],
            "median_active_s": per_intent["median_active_s"],
            "median_wall_s": per_intent["median_wall_s"],
            "median_usd": per_intent["median_usd"],
            "median_tokens": per_intent["median_tokens"],
            "usd": {
                "total": overall["cost"]["usd"],
                "attributed": (overall.get("attributed") or {}).get("cost", {}).get("usd"),
                "overhead": (overall.get("overhead") or {}).get("cost", {}).get("usd"),
            },
            "tokens": {**overall["tokens"], "total": _tokens(overall["tokens"])},
            "repolens_calls": overall["repolens"]["calls"],
            "repolens_p50_ms": overall["repolens"]["p50_ms"],
        },
        "intents": [
            {
                "title": intent["title"] or intent["id"],
                "runs": len(intent["runs"]),
                "active_s": intent["time"]["active_s"],
                "wall_s": intent["time"]["wall_s"],
                "corrections": intent["time"]["corrections"],
                "tokens": _tokens(intent["tokens"]),
                "usd": intent["cost"]["usd"],
                "cost_note": _cost_note(intent["cost"]),
                "repolens_calls": intent["repolens"]["calls"],
            }
            for intent in sorted(report["intents"], key=lambda i: -(i["time"]["active_s"] or 0))
        ],
        "repolens": {
            "bench": [
                {
                    "model": row["model"], "file": row["file"], "runs_per_arm": row["runs_per_arm"],
                    "cost_change_pct": row["cost_usd_change_pct"],
                    "input_tokens_change_pct": row["input_tokens_change_pct"],
                    "tool_calls_change_pct": row["tool_calls_change_pct"],
                    "time_change_pct": row["duration_change_pct"],
                    "recall_change": row["recall_change"],
                }
                for row in report["repolens_effect"]["bench"]
            ],
            "live_pairs": {"with_repolens": (live.get("a") or {}).get("n", 0),
                           "without": (live.get("b") or {}).get("n", 0)},
        },
    }


def validate(snap: dict) -> None:
    for key in ("generated_at", "method", "overall", "intents", "repolens"):
        if key not in snap:
            raise SystemExit(f"factory metrics snapshot missing {key}")
    for intent in snap["intents"]:
        if intent["usd"] == 0 and intent["cost_note"] == "none":
            raise SystemExit(f"{intent['title']}: a missing cost must be null, not 0")


def write(snap: dict, path: Path) -> bool:
    """Write the snapshot; return False when the file already holds the same data."""
    if path.exists():
        try:
            existing = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            existing = None
        if isinstance(existing, dict) and {**snap, "generated_at": existing.get("generated_at")} == existing:
            return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(snap, indent=2) + "\n", encoding="utf-8")
    return True


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--from", dest="source", type=Path, help="read a saved `metrics factory --json` report")
    args = parser.parse_args()
    if args.check:
        validate(json.loads(OUTPUT.read_text(encoding="utf-8")))
        print(f"ok {OUTPUT}")
        return 0
    if args.source:
        report = json.loads(args.source.read_text(encoding="utf-8"))
    else:
        report = json.loads(subprocess.run(KIT, cwd=ROOT, check=True, capture_output=True, text=True).stdout)
    snap = slim(report)
    validate(snap)
    print(f"{'wrote' if write(snap, OUTPUT) else 'unchanged'} {OUTPUT} ({len(snap['intents'])} intents)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
