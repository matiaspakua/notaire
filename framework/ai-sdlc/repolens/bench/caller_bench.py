#!/usr/bin/env python3
"""Caller benchmark: repolens `calls` edges against a call graph resolved by the Java compiler.

    python3 bench/caller_bench.py [--config repolens.toml] [--label NAME] [--module backend-api]

Ground truth: bench/callgraph/CallGraph.java runs javac (Compiler Tree API, Lombok included) over the
module's main and test sources and resolves every method invocation and method reference to the method it
statically binds to. A pair is (caller file, callee file, callee method name): overloads merge (repolens
links by name), constructors and calls inside the callee's own file are left out (an agent asking "who
calls X" needs the other files). Reported: micro precision, recall and F1 over all pairs, the same for
callees whose method name is declared in several classes (the ambiguous case agents asked about), and
per-callee recall of `impact` (what an agent sees). Results land in bench/results/callers-<label>.json.
"""

from __future__ import annotations

import argparse
import json
import statistics
import subprocess
import sys
import time
from collections import defaultdict
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
sys.path.insert(0, str(ROOT))

from repolens.paths import home  # noqa: E402

JAVA_ROOTS = ("src/main/java", "src/test/java")


def repo_root() -> Path:
    out = subprocess.run(["git", "-C", str(ROOT), "rev-parse", "--show-toplevel"], capture_output=True, text=True)
    return Path(out.stdout.strip())


def classpath(module: Path, cache: Path) -> str:
    """The module's test classpath from Maven (offline first), cached by pom.xml mtime."""
    stamp = cache / f"cp-{module.name}-{int((module / 'pom.xml').stat().st_mtime)}.txt"
    if not stamp.exists():
        cache.mkdir(parents=True, exist_ok=True)
        for offline in (["-o"], []):
            done = subprocess.run(["mvn", "-q", *offline, "dependency:build-classpath",
                                   f"-Dmdep.outputFile={stamp}", "-Dmdep.includeScope=test"], cwd=module,
                                  capture_output=True, text=True)
            if done.returncode == 0 and stamp.exists():
                break
        else:
            raise SystemExit(f"mvn dependency:build-classpath failed in {module}")
    return stamp.read_text(encoding="utf-8").strip()


def ground_truth(repo: Path, module: str, cache: Path) -> tuple[list[tuple[str, str, int, str, str]], float]:
    """(caller file, callee file, line, callee owner, callee method) for calls into the module's own sources."""
    mod = repo / module
    out = cache / f"calls-{module.replace('/', '_')}.tsv"
    clock = time.perf_counter()
    roots = [str(mod / r) for r in JAVA_ROOTS if (mod / r).is_dir()]
    subprocess.run(["java", str(HERE / "callgraph" / "CallGraph.java"), str(out), classpath(mod, cache), *roots],
                   cwd=repo, check=True, capture_output=True)
    seconds = time.perf_counter() - clock
    rows = []
    for line in out.read_text(encoding="utf-8").splitlines():
        caller, lineno, owner, method, declared, *_ = line.split("\t") + [""]
        if not declared or method == "<init>":
            continue
        rows.append((_rel(caller, repo), _rel(declared, repo), int(lineno), owner, method))
    return rows, seconds


def _rel(path: str, repo: Path) -> str:
    p = Path(path)
    return (p.relative_to(repo) if p.is_absolute() else p).as_posix()


def predicted(store, module: str) -> set[tuple[str, str, str]]:
    """repolens `calls` edges as (caller file, callee file, callee method name), Java files of the module."""
    out = set()
    prefix = module.rstrip("/") + "/"
    for src, dst, kind, _w in store.edges():
        if kind != "calls":
            continue
        src_path, dst_path = _path(src), _path(dst)
        if not (src_path.startswith(prefix) and dst_path.startswith(prefix) and dst_path.endswith(".java")):
            continue
        parts = [q.split("~", 1)[0] for q in dst.split("#", 1)[-1].split(".")]
        if len(parts) < 2 or parts[-1] == parts[-2]:
            continue  # a type or a constructor (`new X()`): the ground truth leaves constructors out too
        out.add((src_path, dst_path, parts[-1]))
    return out


def _path(node_id: str) -> str:
    return node_id.split(":", 1)[-1].split("#", 1)[0]


def score(truth: set, pred: set) -> dict:
    tp = len(truth & pred)
    precision = tp / len(pred) if pred else 0.0
    recall = tp / len(truth) if truth else 0.0
    f1 = 2 * precision * recall / (precision + recall) if precision + recall else 0.0
    return {"pairs_truth": len(truth), "pairs_predicted": len(pred), "true_positive": tp,
            "precision": round(precision, 4), "recall": round(recall, 4), "f1": round(f1, 4)}


def impact_recall(engine, truth_by_callee: dict, sample: list) -> dict:
    """For each sampled callee: share of its caller files (other files) that `impact` (depth 1) names."""
    recalls, precisions, missing = [], [], 0
    for callee_file, method in sample:
        ref = _method_ref(engine, callee_file, method)
        if ref is None:
            missing += 1
            continue
        result = engine.impact(ref, depth=1, limit=400)
        named = {a["path"] for a in (result or {}).get("affected", []) if a["path"] != callee_file}
        expected = truth_by_callee[(callee_file, method)]
        recalls.append(len(named & expected) / len(expected))
        precisions.append(len(named & expected) / len(named) if named else 0.0)
    return {"callees": len(sample), "not_found": missing,
            "recall_mean": round(statistics.mean(recalls), 4) if recalls else None,
            "precision_mean": round(statistics.mean(precisions), 4) if precisions else None,
            "recall_zero_share": round(sum(r == 0 for r in recalls) / len(recalls), 4) if recalls else None}


def _method_ref(engine, path: str, method: str) -> str | None:
    rows = engine.store.conn.execute(
        "SELECT id FROM nodes WHERE path = ? AND name = ? AND kind = 'method' ORDER BY length(id) LIMIT 1",
        (path, method)).fetchall()
    return rows[0][0] if rows else None


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--config", type=Path)
    ap.add_argument("--label", default="current")
    ap.add_argument("--module", default="backend-api")
    ap.add_argument("--sample", type=int, default=150, help="ambiguous callees sampled for the impact check")
    args = ap.parse_args()
    from repolens.service import KnowledgeGraph

    repo = repo_root()
    cache = home() / "bench" / "callgraph"
    rows, javac_s = ground_truth(repo, args.module, cache)
    truth = {(c, d, m) for c, d, _l, _o, m in rows if c != d}
    kg = KnowledgeGraph.open(args.config or repo / "repolens.toml")
    clock = time.perf_counter()
    report = kg.index(log=lambda *_: None)
    index_s = time.perf_counter() - clock
    pred = {p for p in predicted(kg.store, args.module) if p[0] != p[1]}
    declared = defaultdict(set)
    for _c, _d, _l, owner, m in rows:
        declared[m].add(owner)
    ambiguous = {m for m, owners in declared.items() if len(owners) > 1}
    truth_amb = {p for p in truth if p[2] in ambiguous}
    pred_amb = {p for p in pred if p[2] in ambiguous}
    by_callee: dict = defaultdict(set)
    for c, d, m in truth:
        by_callee[(d, m)].add(c)
    sample = sorted(k for k in by_callee if k[1] in ambiguous)
    step = max(1, len(sample) // args.sample)
    result = {
        "label": args.label, "stamp": time.strftime("%Y-%m-%d %H:%M"), "module": args.module,
        "javac_seconds": round(javac_s, 1), "javac_calls_in_module": len(rows),
        "index": {k: report.get(k) for k in ("timings", "stats")}, "index_seconds": round(index_s, 1),
        "all": score(truth, pred), "ambiguous_names": score(truth_amb, pred_amb),
        "ambiguous_method_names": len(ambiguous),
        "impact_depth1": impact_recall(kg.engine, by_callee, sample[::step][: args.sample]),
    }
    kg.close()
    out = HERE / "results" / f"callers-{args.label}.json"
    out.write_text(json.dumps(result, indent=1), encoding="utf-8")
    print(json.dumps({k: result[k] for k in ("all", "ambiguous_names", "impact_depth1")}, indent=1))
    print(f"-> {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
