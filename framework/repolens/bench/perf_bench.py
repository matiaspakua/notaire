#!/usr/bin/env python3
"""Performance + quality harness: one JSON per label, so every optimisation is measured before/after.

    python3 bench/perf_bench.py --label baseline [--skip-index] [--compare other-label]

Measures store size (and per-table breakdown), index time (no-op, one changed file, full re-extract),
engine load time and peak memory, query latency (p50/p95 + stage breakdown), MCP cold start, and
retrieval quality on the task files passed with `--tasks name=path` (the first is indexed).
The indexed spec's `corpus.touch` is the file rewritten for the one-file step.
"""

from __future__ import annotations

import argparse
import json
import sqlite3
import statistics
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(ROOT))

from corpus import ensure_corpus, ensure_index, load_spec  # noqa: E402
from retrieval_bench import score  # noqa: E402

from repolens.config import load_config  # noqa: E402
from repolens.service import KnowledgeGraph  # noqa: E402


def parse_task_sets(values: list[str]) -> dict[str, Path]:
    sets = {}
    for item in values:
        name, sep, raw = item.partition("=")
        if not sep or not name or not raw:
            raise SystemExit(f"--tasks expects name=path, got {item!r}")
        sets[name] = Path(raw)
    return sets


def db_size(path: Path) -> dict:
    total = sum(p.stat().st_size for p in path.parent.glob(path.name + "*"))
    conn = sqlite3.connect(f"file:{path}?mode=ro", uri=True)
    try:
        rows = conn.execute("SELECT name, SUM(pgsize) FROM dbstat GROUP BY name ORDER BY 2 DESC").fetchall()
    except sqlite3.OperationalError:
        rows = []
    conn.close()
    return {"total_mb": round(total / 1e6, 1), "tables_mb": {n: round(b / 1e6, 1) for n, b in rows[:12]}}


def time_index(config: Path, full: bool) -> dict:
    kg = KnowledgeGraph.open(config)
    t0 = time.perf_counter()
    report = kg.index(full=full, log=lambda *_: None)
    kg.close()
    return {"wall_s": round(time.perf_counter() - t0, 2), **report["timings"]}


def touch_one_file(spec: dict) -> Path:
    relative = (spec.get("corpus") or {}).get("touch") or spec.get("touch")
    if not relative:
        raise SystemExit("the indexed task file needs corpus.touch (a file inside the clone)")
    target = spec["clone"] / relative
    target.write_text(target.read_text(encoding="utf-8") + "\n// perf-bench touch\n", encoding="utf-8")
    return target


def quality(kg: KnowledgeGraph, task_sets: dict[str, Path], graph: bool = True) -> dict:
    out = {}
    for name, path in task_sets.items():
        spec = load_spec(path)
        r10 = mrr = 0.0
        per = {}
        for task in spec["tasks"]:
            files = [h.path for h in kg.engine.search(task["prompt"], k=10, mode="files", graph=graph)]
            sc = score(files, task["expected"])
            r10 += sc["recall@10"]
            mrr += sc["mrr"]
            per[task["id"]] = round(sc["recall@10"], 2)
        n = len(spec["tasks"])
        out[name] = {"recall@10": round(r10 / n, 3), "mrr": round(mrr / n, 3), "per_task": per}
    return out


def latency(kg: KnowledgeGraph, task_sets: dict[str, Path], extra: list[str], repeats: int = 3) -> dict:
    prompts = [t["prompt"] for p in task_sets.values() for t in load_spec(p)["tasks"]] + extra
    samples, stages = [], {}
    for _ in range(repeats):
        for q in prompts:
            kg.engine.timings.clear()
            t0 = time.perf_counter()
            kg.engine.search(q, k=10, mode="files")
            samples.append((time.perf_counter() - t0) * 1000)
            for k, v in kg.engine.timings.items():
                stages.setdefault(k, []).append(v * 1000)
    samples.sort()
    return {"queries": len(samples), "p50_ms": round(statistics.median(samples), 1),
            "p95_ms": round(samples[int(0.95 * len(samples)) - 1], 1),
            "stages_p50_ms": {k: round(statistics.median(v), 2) for k, v in stages.items()}}


def server_rss_mb(config: Path) -> float:
    """Peak resident memory of a fresh process that loaded the engine and the model and answered a query."""
    code = f"""
import sys
sys.path.insert(0, "{ROOT}")
from pathlib import Path
from repolens.service import KnowledgeGraph
kg = KnowledgeGraph.open(Path("{config}")); kg.engine.warm(); kg.engine.search("payment gateway", k=5)
import resource
print(resource.getrusage(resource.RUSAGE_SELF).ru_maxrss / (1e6 if sys.platform == "darwin" else 1e3))
"""
    out = subprocess.run([sys.executable, "-c", code], capture_output=True, text=True, timeout=180,
                         env={"HF_HUB_OFFLINE": "1", "PATH": "/usr/bin:/bin"})
    try:
        return round(float(out.stdout.strip().splitlines()[-1]), 0)
    except (ValueError, IndexError):
        return 0.0


def mcp_cold_start(config: Path) -> dict:
    code = f"""
import asyncio, sys, time
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client
p = StdioServerParameters(command=sys.executable, args=["-m", "repolens", "-c", "{config}", "mcp"],
                          env={{"PYTHONPATH": "{ROOT}", "HF_HUB_OFFLINE": "1", "PATH": "/usr/bin:/bin"}})
async def main():
    t0 = time.perf_counter()
    async with stdio_client(p) as (r, w):
        async with ClientSession(r, w) as s:
            await s.initialize(); t1 = time.perf_counter()
            await s.call_tool("search", {{"query": "budget template controller", "k": 5}}); t2 = time.perf_counter()
            await s.call_tool("search", {{"query": "payment gateway", "k": 5}}); t3 = time.perf_counter()
    print(round(t1 - t0, 2), round(t2 - t1, 2), round(t3 - t2, 3))
asyncio.run(main())
"""
    out = subprocess.run([sys.executable, "-c", code], capture_output=True, text=True, timeout=180)
    parts = out.stdout.split()
    if len(parts) != 3:
        return {"error": out.stderr[-300:]}
    return {"init_s": float(parts[0]), "first_search_s": float(parts[1]), "warm_search_s": float(parts[2])}


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--label", required=True)
    ap.add_argument("--skip-index", action="store_true")
    ap.add_argument("--compare", help="label of an earlier run to diff against")
    ap.add_argument("--tasks", action="append", required=True, metavar="NAME=PATH",
                    help="named task file; the first one is indexed")
    ap.add_argument("--extra", action="append", default=[], metavar="KEY=VALUE",
                    help="record an externally measured number (e.g. embed_cold_min=3.9)")
    args = ap.parse_args()
    task_sets = parse_task_sets(args.tasks)
    spec = load_spec(next(iter(task_sets.values())))
    extra_queries = list(spec.get("extra_queries") or [])
    ensure_corpus(spec)
    ensure_index(spec, log=lambda *_: None)
    config = spec["config"]
    db = load_config(config).db_path
    result: dict = {"label": args.label, "stamp": time.strftime("%Y-%m-%d %H:%M"),
                    "extra": {k: float(v) for k, v in (item.split("=", 1) for item in args.extra)}}
    if not args.skip_index:
        result["index_noop"] = time_index(config, full=False)
        touched = touch_one_file(spec)
        result["index_one_file"] = time_index(config, full=False)
        subprocess.run(["git", "-C", str(spec["clone"]), "checkout", "--", str(touched)], check=True)
        time_index(config, full=False)
        result["index_full_extract"] = time_index(config, full=True)
    result["db"] = db_size(db)
    t0 = time.perf_counter()
    kg = KnowledgeGraph.open(config)
    kg.engine.warm()
    result["engine_load_s"] = round(time.perf_counter() - t0, 2)
    result["latency"] = latency(kg, task_sets, extra_queries)
    result["quality_graph"] = quality(kg, task_sets, graph=True)
    result["quality_hybrid"] = quality(kg, task_sets, graph=False)
    result["peak_rss_mb"] = server_rss_mb(config)
    result["mcp"] = mcp_cold_start(config)
    out = HERE / "results" / "perf" / f"{args.label}.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(result, indent=1), encoding="utf-8")
    print(summary(result))
    if args.compare:
        print(diff(json.loads((out.parent / f"{args.compare}.json").read_text()), result))
    return 0


def _flat(r: dict) -> dict:
    q, h = r["quality_graph"], r["quality_hybrid"]
    flat = {
        "db_mb": r["db"]["total_mb"], "load_s": r["engine_load_s"], "rss_mb": r["peak_rss_mb"],
        "p50_ms": r["latency"]["p50_ms"], "p95_ms": r["latency"]["p95_ms"],
        "tune_R@10": q["tune"]["recall@10"], "tune_MRR": q["tune"]["mrr"],
        "hold_R@10": q["holdout"]["recall@10"], "hold_MRR": q["holdout"]["mrr"],
        "hyb_tune_R@10": h["tune"]["recall@10"], "hyb_hold_R@10": h["holdout"]["recall@10"],
        "mcp_first_s": r["mcp"].get("first_search_s"),
    }
    for key in ("index_noop", "index_one_file", "index_full_extract"):
        if key in r:
            flat[key + "_s"] = r[key]["wall_s"]
    return flat


def summary(r: dict) -> str:
    return "\n".join(f"{k:<24} {v}" for k, v in _flat(r).items()) + "\nstages p50 ms: " + json.dumps(
        r["latency"]["stages_p50_ms"])


def diff(before: dict, after: dict) -> str:
    a, b = _flat(before), _flat(after)
    lines = [f"{'metric':<24} {before['label']:>12} {after['label']:>12}"]
    for k in b:
        lines.append(f"{k:<24} {a.get(k, '-')!s:>12} {b[k]!s:>12}")
    return "\n".join(lines)


if __name__ == "__main__":
    sys.exit(main())
