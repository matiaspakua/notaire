#!/usr/bin/env python3
"""Lightweight CLI comparison experiment for AI SDLC + RepoLens.

Runs 4 representative tasks against available CLIs (agy, kiro-cli) and captures
metrics: recall, duration, tool calls, errors. Handles each CLI's native output format.

Usage:
    python3 cli_experiment.py [--provider agy,kiro] [--tasks totp-verify,person-create,folio-slice,hex-adr]
"""
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent.parent.parent  # project root
TASKS_FILE = HERE / "tasks" / "notaire.yaml"
RESULTS_DIR = HERE / "results"

# Simple YAML parser for notaire.yaml (no external deps)
def load_tasks(path: Path) -> dict:
    """Load tasks from notaire.yaml without PyYAML."""
    text = path.read_text(encoding="utf-8")
    tasks = []
    current = {}
    in_expected = False
    answer_suffix = ""
    in_suffix = False
    for line in text.splitlines():
        stripped = line.strip()
        if line.startswith("answer_suffix:"):
            val = line.split(":", 1)[1].strip()
            if val == ">-":
                in_suffix = True
                continue
            answer_suffix = val + " "
            continue
        if in_suffix and line.startswith("  ") and not line.startswith("    "):
            answer_suffix += line.strip() + " "
            in_suffix = False
            continue
        if in_suffix:
            continue
        if stripped.startswith("- id:") and not line.startswith("      "):
            if current:
                tasks.append(current)
            current = {"id": stripped.split(":", 1)[1].strip(), "expected": [], "category": "", "prompt": ""}
            in_expected = False
        elif stripped.startswith("category:") and current and not in_expected:
            current["category"] = stripped.split(":", 1)[1].strip()
        elif stripped.startswith("prompt:") and current:
            current["prompt"] = stripped.split(":", 1)[1].strip()
        elif stripped == "expected:" and current:
            in_expected = True
        elif stripped.startswith("- ") and in_expected and current:
            current["expected"].append(stripped[2:])
    if current and current.get("id"):
        tasks.append(current)
    return {
        "name": "notaire",
        "corpus": str(ROOT),
        "tasks": tasks,
        "answer_suffix": answer_suffix.strip(),
    }


def parse_agy_output(stdout: str) -> dict:
    """Parse AGY stream-json output."""
    result = {"tool_calls": 0, "tools": {}, "turns": 1, "answer": "", "error": "", "input_tokens": 0, "output_tokens": 0}
    for line in stdout.splitlines():
        try:
            ev = json.loads(line)
        except ValueError:
            continue
        if ev.get("event") == "result":
            r = ev.get("result", {})
            result["answer"] = r.get("response", "")
            result["turns"] = r.get("num_turns", 1)
            result["error"] = r.get("error", "")
            usage = r.get("usage", {})
            result["input_tokens"] = usage.get("input_tokens", 0)
            result["output_tokens"] = usage.get("output_tokens", 0)
        elif ev.get("event") == "step_update":
            su = ev.get("step_update", {})
            if su.get("step_type") == "tool_use":
                tool_name = su.get("tool_name", "unknown")
                result["tool_calls"] += 1
                result["tools"][tool_name] = result["tools"].get(tool_name, 0) + 1
    return result


def parse_kiro_output(stdout: str) -> dict:
    """Parse kiro-cli plain text output."""
    # kiro outputs plain text, no structured tool call info
    return {
        "tool_calls": 0,
        "tools": {},
        "turns": 1,
        "answer": stdout.strip(),
        "error": "",
        "input_tokens": 0,
        "output_tokens": 0,
    }


# CLI configurations
CLI_CONFIGS = {
    "agy": {
        "command": "agy",
        "model": "claude-sonnet-4-6",
        "build_cmd": lambda prompt, mcp_path: [
            "agy", "--print", prompt,
            "--output-format", "stream-json",
            "--model", "claude-sonnet-4-6",
        ],
        "parse_output": parse_agy_output,
    },
    "kiro": {
        "command": "kiro-cli",
        "model": "claude-sonnet-4.5",
        "build_cmd": lambda prompt, mcp_path: [
            "kiro-cli", "chat",
            "--model", "claude-sonnet-4.5",
            "--no-interactive", "--trust-all-tools",
            prompt,
        ],
        "parse_output": parse_kiro_output,
    },
}


def extract_files(answer: str) -> list[str]:
    """Extract file paths from the answer."""
    m = re.findall(r"FILES:\s*(.+)", answer)
    if m:
        raw = m[-1]
        return [p.strip(" `*'\"") for p in re.split(r"[,\s]+", raw) if p.strip(" `*'\"")]
    return re.findall(r"[\w./-]+\.\w{1,5}", answer)


def compute_recall(answer: str, expected: list[str]) -> float:
    """Compute recall against expected file patterns."""
    if not expected:
        return 0.0
    files = extract_files(answer)
    found = [any(re.search(rx, f) for f in files) for rx in expected]
    return round(sum(found) / len(expected), 3)


def run_task(cli_id: str, task: dict, suffix: str, cwd: str, timeout: int = 120) -> dict:
    """Run one task against one CLI."""
    config = CLI_CONFIGS[cli_id]
    prompt = task["prompt"] + "\n\n" + suffix
    cmd = config["build_cmd"](prompt, None)

    t0 = time.perf_counter()
    try:
        proc = subprocess.run(
            cmd, cwd=cwd, capture_output=True, text=True, timeout=timeout,
            env={**os.environ},
        )
        stdout = proc.stdout
        error = proc.stderr[-500:] if proc.returncode != 0 else ""
    except subprocess.TimeoutExpired as exc:
        stdout = (exc.stdout or b"").decode() if isinstance(exc.stdout, bytes) else (exc.stdout or "")
        error = "timeout"
    wall = round(time.perf_counter() - t0, 1)

    parsed = config["parse_output"](stdout)
    recall = compute_recall(parsed["answer"], task["expected"])

    return {
        "task": task["id"],
        "category": task.get("category", ""),
        "cli": cli_id,
        "model": config["model"],
        "arm": "baseline",
        "recall": recall,
        "tool_calls": parsed["tool_calls"],
        "tools": parsed["tools"],
        "turns": parsed["turns"],
        "duration_s": wall,
        "input_tokens": parsed["input_tokens"],
        "output_tokens": parsed["output_tokens"],
        "is_error": bool(parsed["error"]) or bool(error),
        "error": parsed["error"] or error[:200],
        "answer": parsed["answer"][-1500:],
    }


def summarize(rows: list[dict]) -> dict:
    """Summarize results by CLI."""
    by_cli = {}
    for r in rows:
        cli = r["cli"]
        if cli not in by_cli:
            by_cli[cli] = {"runs": 0, "errors": 0, "total_recall": 0.0, "total_duration": 0.0, "total_tool_calls": 0}
        by_cli[cli]["runs"] += 1
        if r["is_error"]:
            by_cli[cli]["errors"] += 1
        else:
            by_cli[cli]["total_recall"] += r["recall"]
            by_cli[cli]["total_duration"] += r["duration_s"]
            by_cli[cli]["total_tool_calls"] += r["tool_calls"]
    summary = {}
    for cli, stats in by_cli.items():
        successful = stats["runs"] - stats["errors"]
        summary[cli] = {
            "runs": stats["runs"],
            "errors": stats["errors"],
            "avg_recall": round(stats["total_recall"] / successful, 3) if successful else 0.0,
            "avg_duration_s": round(stats["total_duration"] / successful, 1) if successful else 0.0,
            "avg_tool_calls": round(stats["total_tool_calls"] / successful, 1) if successful else 0.0,
        }
    return summary


def main() -> int:
    ap = argparse.ArgumentParser(description="CLI comparison experiment")
    ap.add_argument("--provider", default="agy,kiro", help="comma-separated CLI ids")
    ap.add_argument("--tasks", default="totp-verify,person-create,folio-slice,hex-adr")
    ap.add_argument("--timeout", type=int, default=120)
    ap.add_argument("--out", type=Path, default=None)
    args = ap.parse_args()

    spec = load_tasks(TASKS_FILE)
    task_ids = args.tasks.split(",")
    tasks = [t for t in spec["tasks"] if t["id"] in task_ids]
    providers = args.provider.split(",")
    suffix = spec.get("answer_suffix", "")

    print(f"Tasks: {[t['id'] for t in tasks]}", file=sys.stderr)
    print(f"Providers: {providers}", file=sys.stderr)
    print(f"CWD: {spec['corpus']}", file=sys.stderr)

    rows = []
    for cli_id in providers:
        if cli_id not in CLI_CONFIGS:
            print(f"Skipping unknown provider: {cli_id}", file=sys.stderr)
            continue
        # Quick availability check
        try:
            r = subprocess.run([CLI_CONFIGS[cli_id]["command"], "--version"], capture_output=True, text=True, timeout=5)
            if r.returncode != 0:
                print(f"{cli_id} not available, skipping", file=sys.stderr)
                continue
        except (OSError, subprocess.TimeoutExpired):
            print(f"{cli_id} not available, skipping", file=sys.stderr)
            continue

        print(f"\n=== Running {cli_id} ({CLI_CONFIGS[cli_id]['model']}) ===", file=sys.stderr)
        for task in tasks:
            print(f"  {task['id']}...", file=sys.stderr, end="", flush=True)
            row = run_task(cli_id, task, suffix, spec["corpus"], args.timeout)
            rows.append(row)
            status = "ERROR" if row["is_error"] else f"recall={row['recall']:.2f}"
            print(f" {status} {row['duration_s']}s", file=sys.stderr)

    summary = summarize(rows)
    print(f"\n=== Summary ===", file=sys.stderr)
    for cli, stats in summary.items():
        print(f"  {cli}: runs={stats['runs']}, errors={stats['errors']}, "
              f"avg_recall={stats['avg_recall']:.3f}, avg_duration={stats['avg_duration_s']}s", file=sys.stderr)

    out = args.out or RESULTS_DIR / "cli-comparison.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    result = {
        "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S"),
        "tasks": [t["id"] for t in tasks],
        "providers": providers,
        "summary": summary,
        "rows": rows,
    }
    out.write_text(json.dumps(result, indent=1), encoding="utf-8")
    print(f"\nResults: {out}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
