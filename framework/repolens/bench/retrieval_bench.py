#!/usr/bin/env python3
"""Retrieval benchmark (deterministic, no LLM): ripgrep keyword ranking vs repolens hybrid vs repolens graph.

    python3 bench/retrieval_bench.py tasks.yaml [-k 10] [--out bench/results/retrieval.json]

Arms
  grep    keywords from the prompt, one `rg -i -c -F` per keyword, files ranked by TF-IDF of the matches
          (how a grep-driven agent narrows a repo before reading files)
  hybrid  repolens BM25 + embeddings, no graph walk (ablation)
  graph   repolens BM25 + embeddings anchors, then typed Personalized PageRank (the full system)
"""

from __future__ import annotations

import argparse
import json
import math
import re
import subprocess
import sys
import time
from pathlib import Path

import yaml

HERE = Path(__file__).resolve().parent
TAIL = 10  # the MCP search tool lists this many one-line results after the detailed top k
sys.path.insert(0, str(HERE.parent))

from corpus import ensure_corpus, ensure_index, head, load_spec  # noqa: E402

from repolens import __version__ as REPOLENS_VERSION  # noqa: E402
from repolens import render  # noqa: E402
from repolens.engine import STOPWORDS  # noqa: E402
from repolens.service import KnowledgeGraph  # noqa: E402


def keywords(prompt: str) -> list[str]:
    words = re.findall(r"[A-Za-z][A-Za-z0-9_]+", prompt)
    out = []
    for w in words:
        low = w.lower()
        if len(low) >= 3 and low not in STOPWORDS and low not in out:
            out.append(low)
    return out


def grep_arm(repo: Path, prompt: str, k: int) -> tuple[list[str], int, float]:
    t0 = time.perf_counter()
    kws = keywords(prompt)
    counts: dict[str, dict[str, int]] = {}
    out_bytes = 0
    for kw in kws:
        res = subprocess.run(["rg", "-i", "-c", "-F", kw, "."], cwd=repo, capture_output=True, text=True)
        per = {}
        for line in res.stdout.splitlines():
            path, _, n = line.rpartition(":")
            if path and n.isdigit():
                per[path.removeprefix("./")] = int(n)
        counts[kw] = per
    n_files = _corpus_size(repo)
    scores: dict[str, float] = {}
    for _kw, per in counts.items():
        if not per:
            continue
        idf = math.log(n_files / len(per))
        for path, n in per.items():
            scores[path] = scores.get(path, 0.0) + idf * (1 + math.log(n))
    ranked = sorted(scores, key=scores.get, reverse=True)[:k + TAIL]
    for path in ranked[:k]:
        res = subprocess.run(["rg", "-n", "-i", "-F", *sum((["-e", kw] for kw in kws), []), path],
                             cwd=repo, capture_output=True, text=True)
        out_bytes += len(res.stdout.encode())
    return ranked, out_bytes, (time.perf_counter() - t0) * 1000


def _corpus_size(repo: Path, _cache: dict = {}) -> int:  # noqa: B006 - per-process memo
    if repo not in _cache:
        res = subprocess.run(["rg", "--files", "."], cwd=repo, capture_output=True, text=True)
        _cache[repo] = max(1, len(res.stdout.splitlines()))
    return _cache[repo]


def kg_arm(kg: KnowledgeGraph, prompt: str, k: int, graph: bool) -> tuple[list[str], int, float, int]:
    """Returns files (top k + TAIL), bytes of the top-k rendering, latency, and bytes of the MCP rendering
    (top k detailed + TAIL one-line entries)."""
    t0 = time.perf_counter()
    hits = kg.engine.search(prompt, k=k + TAIL, mode="files", graph=graph)
    ms = (time.perf_counter() - t0) * 1000
    top = len(render.hits(hits[:k], prompt).encode())
    return [h.path for h in hits if h.path], top, ms, len(render.hits(hits, prompt, detailed=k).encode())


def score(files: list[str], expected: list[str]) -> dict:
    found_at = []
    for rx in expected:
        pos = next((i for i, f in enumerate(files) if re.search(rx, f)), None)
        found_at.append(pos)
    hits = [p for p in found_at if p is not None]
    def recall(n):
        return sum(1 for p in found_at if p is not None and p < n) / len(expected)
    return {"recall@5": recall(5), "recall@10": recall(10), "recall@20": recall(20),
            "mrr": 1.0 / (min(hits) + 1) if hits else 0.0}



def result_meta(spec: dict, k: int, taskset: str) -> dict:
    """What produced a result: the corpus commit and the RepoLens version, so stale results can be found."""
    return {"k": k, "corpus": spec.get("name"), "commit": head(spec), "repolens_version": REPOLENS_VERSION,
            "tasks": len(spec["tasks"]), "taskset": taskset}

def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("tasks", type=Path)
    ap.add_argument("-k", type=int, default=10)
    ap.add_argument("--out", type=Path)
    args = ap.parse_args()
    taskset = args.tasks.stem
    spec_preview = yaml.safe_load(args.tasks.read_text(encoding="utf-8"))
    default = spec_preview.get("output_name") or f"retrieval-{taskset}.json"
    args.out = args.out or HERE / "results" / default
    spec = load_spec(args.tasks)
    ensure_corpus(spec)
    ensure_index(spec, log=lambda m: print(m, file=sys.stderr))
    corpus = spec["clone"]
    kg = KnowledgeGraph.open(spec["config"])
    kg.engine.warm()
    rows = []
    for task in spec["tasks"]:
        arms = {
            "grep": grep_arm(corpus, task["prompt"], args.k),
            "hybrid": kg_arm(kg, task["prompt"], args.k, graph=False),
            "graph": kg_arm(kg, task["prompt"], args.k, graph=True),
        }
        for arm, (files, nbytes, ms, *tail) in arms.items():
            row = {"task": task["id"], "category": task["category"], "arm": arm, "latency_ms": round(ms, 1),
                   "output_bytes": nbytes, "output_bytes_tail": tail[0] if tail else nbytes, "files": files,
                   **score(files, task["expected"])}
            rows.append(row)
        line = "  ".join(f"{a}: R@10={r['recall@10']:.2f} {r['latency_ms']:.0f}ms"
                         for a, r in zip(arms, rows[-3:], strict=False))
        print(f"{task['id']:<24} {line}")
    summary = {}
    for arm in ("grep", "hybrid", "graph"):
        sel = [r for r in rows if r["arm"] == arm]
        summary[arm] = {m: round(sum(r[m] for r in sel) / len(sel), 3)
                        for m in ("recall@5", "recall@10", "recall@20", "mrr", "latency_ms", "output_bytes",
                                  "output_bytes_tail")}
    print(json.dumps(summary, indent=1))
    args.out.parent.mkdir(parents=True, exist_ok=True)
    meta = result_meta(spec, args.k, taskset)
    args.out.write_text(json.dumps({**meta, "summary": summary, "rows": rows}, indent=1), encoding="utf-8")
    print(f"wrote {args.out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
