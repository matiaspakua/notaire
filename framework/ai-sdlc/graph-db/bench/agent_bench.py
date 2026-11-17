#!/usr/bin/env python3
"""Agent benchmark: the same prompt, model and built-in tools, with and without the repolens MCP server.

    python3 bench/agent_bench.py bench/tasks/notaire.yaml --model sonnet [--repeat 1] [--parallel 3]
           [--tasks id,id] [--budget 1.0] [--arms baseline,graph]

Each run is a headless Claude Code session (`claude -p`) in a clean clone of the repository:
  baseline  built-in Read, Grep, Glob (the standard grep-driven harness)
  graph     the same three tools + the repolens MCP server (search, node, impact, path, outline, snippet...)
No extra system prompt is added to either arm; the MCP server describes itself through its own
instructions and tool descriptions. Raw transcripts land in bench/results/runs/<stamp>/.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
sys.path.insert(0, str(HERE))

from corpus import ensure_corpus, ensure_index, head, load_spec  # noqa: E402

BUILTIN = ["Read", "Grep", "Glob"]


def mcp_config(spec: dict, out: Path) -> Path:
    cfg = {"mcpServers": {"repolens": {
        "command": sys.executable,
        "args": ["-m", "repolens", "-c", str(spec["config"]), "mcp"],
        "env": {"PYTHONPATH": str(ROOT), "HF_HUB_OFFLINE": "1", "TOKENIZERS_PARALLELISM": "false"},
    }}}
    out.write_text(json.dumps(cfg, indent=1), encoding="utf-8")
    return out


def command(prompt: str, arm: str, model: str, budget: float, mcp: Path) -> list[str]:
    cmd = ["claude", "-p", prompt, "--model", model, "--output-format", "stream-json", "--verbose",
           "--setting-sources", "", "--no-session-persistence", "--strict-mcp-config",
           "--max-budget-usd", str(budget), "--tools", ",".join(BUILTIN)]
    allowed = list(BUILTIN)
    if arm == "graph":
        cmd += ["--mcp-config", str(mcp)]
        allowed.append("mcp__repolens")
    return cmd + ["--allowedTools", *allowed]


STOP = threading.Event()
LIMIT = re.compile(r"usage_limit_reached|hit your .{0,40}limit|rate_limit_error", re.I)


def run_one(job: dict) -> dict | None:
    """One headless session. Returns None when the batch was stopped (account usage limit reached)."""
    if STOP.is_set():
        return None
    env = {k: v for k, v in os.environ.items() if not k.startswith(("CLAUDECODE", "CLAUDE_CODE_", "CLAUDE_AGENT"))}
    t0 = time.perf_counter()
    try:
        proc = subprocess.run(job["cmd"], cwd=job["cwd"], env=env, capture_output=True, text=True,
                              timeout=job["timeout"])
        stdout, error = proc.stdout, proc.stderr[-500:] if proc.returncode else ""
    except subprocess.TimeoutExpired as exc:
        stdout, error = (exc.stdout or b"").decode() if isinstance(exc.stdout, bytes) else (exc.stdout or ""), "timeout"
    wall = time.perf_counter() - t0
    job["log"].write_text(stdout, encoding="utf-8")
    if LIMIT.search(stdout[-3000:]):
        STOP.set()
        return None
    return {**parse(stdout, job["expected"]), "task": job["task"], "category": job["category"], "arm": job["arm"],
            "rep": job["rep"], "wall_s": round(wall, 1), "error": error}


def parse(stream: str, expected: list[str]) -> dict:
    tools: dict[str, int] = {}
    result: dict = {}
    mcp_status = ""
    first_tool_ms = None
    for line in stream.splitlines():
        try:
            ev = json.loads(line)
        except ValueError:
            continue
        if ev.get("type") == "system" and ev.get("subtype") == "init":
            mcp_status = ",".join(f"{s['name']}:{s['status']}" for s in ev.get("mcp_servers", []))
        elif ev.get("type") == "assistant":
            for block in ev.get("message", {}).get("content", []):
                if block.get("type") == "tool_use":
                    tools[block["name"]] = tools.get(block["name"], 0) + 1
        elif ev.get("type") == "result":
            result = ev
    usage = result.get("usage") or {}
    answer = result.get("result") or ""
    files = _files(answer)
    found = [any(re.search(rx, f) for f in files) for rx in expected]
    return {
        "recall": round(sum(found) / len(expected), 3) if expected else 0.0,
        "files_listed": len(files),
        "tool_calls": sum(tools.values()),
        "graph_calls": sum(n for t, n in tools.items() if t.startswith("mcp__repolens")),
        "tools": tools,
        "turns": result.get("num_turns", 0),
        "duration_s": round((result.get("duration_ms") or 0) / 1000, 1),
        "cost_usd": round(result.get("total_cost_usd") or 0.0, 4),
        "input_tokens": (usage.get("input_tokens", 0) + usage.get("cache_creation_input_tokens", 0)
                         + usage.get("cache_read_input_tokens", 0)),
        "output_tokens": usage.get("output_tokens", 0),
        "is_error": bool(result.get("is_error")) or not result,
        "mcp": mcp_status,
        "first_tool_ms": first_tool_ms,
        "answer": answer[-1500:],
    }


def _files(answer: str) -> list[str]:
    m = re.findall(r"FILES:\s*(.+)", answer)
    if not m:
        return re.findall(r"[\w./-]+\.\w{1,5}", answer)
    raw = m[-1]
    return [p.strip(" `*'\"") for p in re.split(r"[,\s]+", raw) if p.strip(" `*'\"")]


def summarize(rows: list[dict]) -> dict:
    out = {}
    metrics = ("recall", "tool_calls", "turns", "duration_s", "cost_usd", "input_tokens", "output_tokens")
    for arm in sorted({r["arm"] for r in rows}):
        sel = [r for r in rows if r["arm"] == arm and not r["is_error"]]
        if sel:
            out[arm] = {m: round(sum(r[m] for r in sel) / len(sel), 6) for m in metrics}
            out[arm]["runs"] = len(sel)
            out[arm]["errors"] = sum(1 for r in rows if r["arm"] == arm and r["is_error"])
    return out


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("tasks", type=Path)
    ap.add_argument("--model", default="sonnet")
    ap.add_argument("--arms", default="baseline,graph")
    ap.add_argument("--repeat", type=int, default=1)
    ap.add_argument("--parallel", type=int, default=3)
    ap.add_argument("--budget", type=float, default=1.0, help="max USD per run")
    ap.add_argument("--timeout", type=int, default=900)
    ap.add_argument("--only", help="comma-separated task ids")
    ap.add_argument("--out", type=Path)
    args = ap.parse_args()

    spec = load_spec(args.tasks)
    ensure_corpus(spec)
    ensure_index(spec, log=lambda m: print(m, file=sys.stderr))
    stamp = time.strftime("%Y%m%d-%H%M%S")
    run_dir = HERE / "results" / "runs" / f"{stamp}-{args.model}"
    run_dir.mkdir(parents=True, exist_ok=True)
    mcp = mcp_config(spec, run_dir / "mcp.json")
    tasks = [t for t in spec["tasks"] if not args.only or t["id"] in args.only.split(",")]
    suffix = "\n\n" + spec.get("answer_suffix", "")
    jobs = []
    for rep in range(args.repeat):
        for task in tasks:
            for arm in args.arms.split(","):
                jobs.append({
                    "task": task["id"], "category": task.get("category", task.get("project", "")),
                    "arm": arm, "rep": rep,
                    "expected": task["expected"], "cwd": str(spec["clone"]), "timeout": args.timeout,
                    "cmd": command(task["prompt"] + suffix, arm, args.model, args.budget, mcp),
                    "log": run_dir / f"{task['id']}-{arm}-{rep}.jsonl",
                })
    print(f"{len(jobs)} runs ({args.model}), transcripts in {run_dir}", file=sys.stderr)
    rows = []
    with ThreadPoolExecutor(max_workers=args.parallel) as pool:
        for row in pool.map(run_one, jobs):
            if row is None:
                continue
            rows.append(row)
            print(f"{row['task']:<24} {row['arm']:<9} recall={row['recall']:.2f} tools={row['tool_calls']:>2} "
                  f"(graph {row['graph_calls']}) {row['duration_s']:>5}s ${row['cost_usd']:.3f} "
                  f"in={row['input_tokens']} {row['mcp']} {row['error'][:80]}", file=sys.stderr)
    summary = summarize(rows)
    print(json.dumps(summary, indent=1))
    if STOP.is_set():
        partial = run_dir / "partial.json"
        partial.write_text(json.dumps({"model": args.model, "rows": rows}, indent=1), encoding="utf-8")
        print(f"STOPPED: the account usage limit was reached after {len(rows)}/{len(jobs)} runs. "
              f"Partial rows are in {partial}; existing result files were left untouched.", file=sys.stderr)
        return 3
    taskset = args.tasks.stem
    suffix_name = "" if taskset == "notaire" else f"-{taskset}"
    out = args.out or HERE / "results" / f"agent-{args.model}{suffix_name}.json"
    meta = {"model": args.model, "corpus": spec.get("name"), "commit": head(spec), "repeat": args.repeat,
            "taskset": taskset,
            "stamp": stamp, "tools_baseline": BUILTIN, "tools_graph": [*BUILTIN, "mcp__repolens"]}
    out.write_text(json.dumps({**meta, "summary": summary, "rows": rows}, indent=1), encoding="utf-8")
    print(f"wrote {out}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
