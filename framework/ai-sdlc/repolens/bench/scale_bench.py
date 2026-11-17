#!/usr/bin/env python3
"""Scale benchmark: index two large public repositories as one multi-project store and measure every stage.

    python3 bench/scale_bench.py --label s0 [--embed none|fastembed] [--steps index,noop,touch,query]

Corpus: shallow clones of spring-framework and vscode in bench/.scale (~33k files, ~9M lines), cloned on
first use. Each step runs in a fresh process, so peak memory (`ru_maxrss` of the process and of its
extraction workers) is that step's own. Quality uses bench/tasks/scale.yaml (never used for tuning).
"""

from __future__ import annotations

import argparse
import json
import statistics
import subprocess
import sys
import time
from pathlib import Path

import yaml

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
SCALE = HERE / ".scale"
REPOS = {"spring-framework": "https://github.com/spring-projects/spring-framework.git",
         "vscode": "https://github.com/microsoft/vscode.git"}
TASKS = HERE / "tasks" / "scale.yaml"
TOUCH = ("spring-framework", "spring-core/src/main/java/org/springframework/util/StringUtils.java")
LATENCY_EXTRA = ["DispatcherServlet", "TextModel", "createDecorator", "registerSingleton", "IEditorService",
                 "how is a bean definition registered", "where are extensions activated", "json schema validation",
                 "transaction synchronization", "terminal process launch", "webview message passing"]

CHILD = r"""
import json, resource, sys, time
sys.path.insert(0, {root!r})
from pathlib import Path
from repolens.service import KnowledgeGraph

def rss():
    scale = 1e6 if sys.platform == "darwin" else 1e3
    me = resource.getrusage(resource.RUSAGE_SELF).ru_maxrss / scale
    kids = resource.getrusage(resource.RUSAGE_CHILDREN).ru_maxrss / scale
    return round(me), round(kids)

kg = KnowledgeGraph.open(Path({config!r}))
step = {step!r}
out = {{}}
if step in ("index", "noop", "touch", "full"):
    t0 = time.perf_counter()
    rep = kg.index(full=step == "full", log=lambda *a: print(*a, file=sys.stderr, flush=True))
    out = {{"wall_s": round(time.perf_counter() - t0, 2), **rep["timings"], "stats": rep["stats"],
            "embed": rep["embed"]}}
else:
    t0 = time.perf_counter()
    eng = kg.engine
    out["load_s"] = round(time.perf_counter() - t0, 2)
    out["rss_after_load_mb"] = rss()[0]
    t0 = time.perf_counter()
    eng.warm()
    out["warm_s"] = round(time.perf_counter() - t0, 2)
    tasks, queries = json.loads(sys.argv[1]), json.loads(sys.argv[2])
    results = {{}}
    for t in tasks:
        hits = eng.search(t["prompt"], k=20, mode="files")
        results[t["id"]] = [h.project + ":" + h.path for h in hits]
    samples, stages = [], {{}}
    for _ in range(3):
        for q in queries:
            eng.timings.clear()
            c = time.perf_counter()
            eng.search(q, k=10, mode="files")
            samples.append((time.perf_counter() - c) * 1000)
            for k, v in eng.timings.items():
                stages.setdefault(k, []).append(v * 1000)
    out["results"] = results
    out["samples"] = samples
    out["stages"] = stages
    out["nodes"] = len(eng.ids)
    out["edges"] = int(eng.out_ptr[-1])
out["rss_mb"], out["children_rss_mb"] = rss()
print("@@" + json.dumps(out))
"""


def ensure_clones() -> None:
    """Clone the repositories. The folder is its own (empty) git root, so an agent working in it treats it
    as the project instead of the enclosing repository; stores and profiles are ignored by search tools."""
    SCALE.mkdir(parents=True, exist_ok=True)
    for name, url in REPOS.items():
        if not (SCALE / name / ".git").exists():
            subprocess.run(["git", "clone", "--quiet", "--depth", "1", url, str(SCALE / name)], check=True)
    if not (SCALE / ".git").exists():
        subprocess.run(["git", "init", "--quiet", str(SCALE)], check=True)
    (SCALE / ".gitignore").write_text("/*.db*\n/*.toml\n/.gitignore\n", encoding="utf-8")


def write_config(embed: str) -> Path:
    cfg = SCALE / f"scale-{embed}.toml"
    sources = "".join(f'\n[[source]]\nname = "{n}"\npath = "{n}"\n' for n in REPOS)
    cfg.write_text(f'[index]\ndb = "scale-{embed}.db"\n\n[embed]\nbackend = "{embed}"\n'
                   f'model = "BAAI/bge-small-en-v1.5"\n{sources}', encoding="utf-8")
    return cfg


def run_step(config: Path, step: str, tasks: list | None = None, queries: list | None = None) -> dict:
    code = CHILD.format(root=str(ROOT), config=str(config), step=step)
    args = [sys.executable, "-c", code, json.dumps(tasks or []), json.dumps(queries or [])]
    proc = subprocess.run(args, capture_output=True, text=True, env={"HF_HUB_OFFLINE": "1", "PATH": "/usr/bin:/bin"})
    line = next((ln for ln in proc.stdout.splitlines() if ln.startswith("@@")), None)
    if line is None:
        raise RuntimeError(f"step {step} failed:\n{proc.stderr[-3000:]}")
    return json.loads(line[2:])


def db_mb(config: Path) -> float:
    db = SCALE / config.name.replace(".toml", ".db")
    return round(sum(p.stat().st_size for p in db.parent.glob(db.name + "*")) / 1e6, 1)


def score(paths: list[str], expected: list[str], k: int) -> tuple[float, float]:
    import re

    rel = [p.split(":", 1)[1] for p in paths]
    found = [any(re.search(pat, p) for p in rel[:k]) for pat in expected]
    first = next((r for r, p in enumerate(rel, 1) if any(re.search(pat, p) for pat in expected)), None)
    return sum(found) / len(expected), (1.0 / first if first else 0.0)


def quality(res: dict, tasks: list) -> dict:
    per, r10, r20, mrr = {}, [], [], []
    for t in tasks:
        a, m = score(res["results"][t["id"]], t["expected"], 10)
        b, _ = score(res["results"][t["id"]], t["expected"], 20)
        r10.append(a)
        r20.append(b)
        mrr.append(m)
        per[t["id"]] = {"r10": round(a, 2), "r20": round(b, 2), "mrr": round(m, 2),
                        "top3": res["results"][t["id"]][:3]}
    return {"recall@10": round(statistics.mean(r10), 3), "recall@20": round(statistics.mean(r20), 3),
            "mrr": round(statistics.mean(mrr), 3), "per_task": per}


def touch() -> Path:
    target = SCALE / TOUCH[0] / TOUCH[1]
    target.write_text(target.read_text(encoding="utf-8") + "\n// scale-bench touch\n", encoding="utf-8")
    return target


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--label", required=True)
    ap.add_argument("--embed", default="none", choices=["none", "fastembed", "lsa"])
    ap.add_argument("--steps", default="index,noop,touch,query")
    ap.add_argument("--fresh", action="store_true", help="delete the store first")
    args = ap.parse_args()
    ensure_clones()
    config = write_config(args.embed)
    if args.fresh:
        for p in SCALE.glob(f"scale-{args.embed}.db*"):
            p.unlink()
    spec = yaml.safe_load(TASKS.read_text(encoding="utf-8"))
    tasks = spec["tasks"]
    result: dict = {"label": args.label, "embed": args.embed, "stamp": time.strftime("%Y-%m-%d %H:%M")}
    for step in args.steps.split(","):
        print(f"-- {step}", flush=True)
        if step == "touch":
            touch()
            try:
                result["touch"] = run_step(config, "touch")
            finally:
                subprocess.run(["git", "-C", str(SCALE / TOUCH[0]), "checkout", "--", TOUCH[1]], check=True)
                run_step(config, "noop")
        elif step == "query":
            queries = [t["prompt"] for t in tasks] + LATENCY_EXTRA
            res = run_step(config, "query", tasks, queries)
            samples = sorted(res.pop("samples"))
            stages = res.pop("stages")
            res["latency"] = {"p50_ms": round(statistics.median(samples), 1),
                              "p95_ms": round(samples[int(0.95 * len(samples)) - 1], 1),
                              "stages_p50_ms": {k: round(statistics.median(v), 2) for k, v in stages.items()}}
            res["quality"] = quality(res, tasks)
            res.pop("results")
            result["query"] = res
        else:
            result[step] = run_step(config, step)
        print(json.dumps(result[step], indent=None)[:600], flush=True)
    result["db_mb"] = db_mb(config)
    out = HERE / "results" / "perf" / f"scale-{args.label}.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(result, indent=1), encoding="utf-8")
    print(f"db {result['db_mb']} MB -> {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
