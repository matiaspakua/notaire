"""Repositories indexed on this machine, and one question across several of them.

Every index run records its store in `$REPOLENS_HOME/repos.json`: name, root, store, commit and the
repository it belongs to (worktrees of one repository share that identity). A federated search asks each
selected repository's own index (its resident service when it can, else the store in-process) for its hits and
its relevance to the question (the mean cosine of its ten nearest vectors), then merges the answers by
reciprocal-rank fusion, each repository weighted by exp((relevance - best) / TAU), and labels each hit with its
repository. Plain interleaving cost Notaire's held-out recall@10 0.786 -> 0.628 with one unrelated store
registered; the owning repository scored the higher relevance in 32 of 32 benchmark tasks
(bench/federation_bench.py). Stores in different embedding spaces (another model or width, or an LSA store) do
not compare, and are interleaved unweighted. Each repository keeps its own index, profile and lifecycle; nothing is
copied between stores.

`all` means one store per repository: this worktree's for the current repository, the most recently indexed
for the others. A comma-separated list selects repositories by name.
"""

from __future__ import annotations

import contextlib
import json
import math
import os
import subprocess
import time
from pathlib import Path

from .paths import home

RRF_K = 60
TAU = 0.05  # the largest temperature with no recall loss on owner tasks (bench/results/federation-0.5.0.json)


def registry_path() -> Path:
    return home() / "repos.json"


@contextlib.contextmanager
def _locked():
    path = registry_path()
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path.with_suffix(".lock"), "a") as lock:
        try:
            import fcntl

            fcntl.flock(lock, fcntl.LOCK_EX)
        except ImportError:  # no advisory locks: last writer wins
            pass
        yield path


def _read(path: Path) -> dict:
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
        return data if isinstance(data, dict) and isinstance(data.get("repos"), dict) else {"repos": {}}
    except (OSError, ValueError):
        return {"repos": {}}


def _identity(root: Path) -> str:
    try:
        out = subprocess.run(["git", "-C", str(root), "rev-parse", "--path-format=absolute", "--git-common-dir"],
                             capture_output=True, text=True, timeout=10).stdout.strip()
    except (OSError, subprocess.SubprocessError):
        out = ""
    return out or str(root)


def record(cfg, head: str = "", files: int = 0) -> None:
    """Register (or refresh) the store an index run just wrote."""
    if not cfg.sources:
        return
    paths = [str(Path(src.path).resolve()) for src in cfg.sources]
    root = Path(os.path.commonpath(paths)) if len(paths) > 1 else Path(paths[0])
    name = cfg.sources[0].name if len(cfg.sources) == 1 else root.name
    entry = {"name": name, "root": str(root), "db": str(Path(cfg.db_path).resolve()), "head": head,
             "files": files, "indexed_at": time.time(), "repo": _identity(root)}
    with _locked() as path:
        data = _read(path)
        data["repos"][entry["db"]] = entry
        tmp = path.with_suffix(".tmp")
        tmp.write_text(json.dumps(data, indent=1), encoding="utf-8")
        os.replace(tmp, path)


def listed() -> list[dict]:
    """Registered stores that still exist, most recently indexed first."""
    entries = [e for e in _read(registry_path())["repos"].values() if Path(e.get("db", "")).exists()]
    return sorted(entries, key=lambda e: -e.get("indexed_at", 0))


def forget(name: str) -> int:
    with _locked() as path:
        data = _read(path)
        doomed = [db for db, e in data["repos"].items() if e.get("name") == name]
        for db in doomed:
            del data["repos"][db]
        path.write_text(json.dumps(data, indent=1), encoding="utf-8")
    return len(doomed)


def targets(selector: str, current_db: Path) -> list[dict]:
    """The stores a selector names, one per repository, the current store first."""
    current = str(Path(current_db).resolve())
    entries = listed()
    wanted = None if selector.strip() == "all" else {s.strip() for s in selector.split(",") if s.strip()}
    chosen: dict[str, dict] = {}
    for e in entries:  # newest first
        if wanted is not None and e["name"] not in wanted:
            continue
        key = e.get("repo") or e["db"]
        if key not in chosen or e["db"] == current:
            chosen[key] = e
    return sorted(chosen.values(), key=lambda e: e["db"] != current)


# -- federated search (imports the engine lazily) ----------------------------------------------
_OPEN: dict[str, object] = {}


def search(kg, query: str, k: int, selector: str, mode: str = "files", scope: str = "all") -> list[tuple]:
    """(repository name, Hit) pairs, best first, over the repositories a selector names."""
    current = Path(kg.cfg.db_path).resolve()
    entries = targets(selector, current)
    rels = [_ask(kg, current, e, query, 0, mode, scope)[1] for e in entries]
    weight = weights(rels)
    answers: dict[int, tuple] = {}
    for i in sorted(range(len(entries)), key=lambda i: -weight[i]):  # stable: the current repository first
        if _out_of_reach(answers, weight, k, weight[i]):
            break
        hits, _rel = _ask(kg, current, entries[i], query, k, mode, scope)
        answers[i] = (entries[i]["name"], hits, rels[i])
    return fuse([answers[i] for i in sorted(answers)], k)


def weights(rels: list[dict | None]) -> list[float]:
    """exp((relevance - best) / TAU) per answer when all were measured in one embedding space, else 1."""
    if not rels or not all(rels) or len({r["space"] for r in rels}) != 1:
        return [1.0] * len(rels)
    best = max(r["score"] for r in rels)
    return [math.exp((r["score"] - best) / TAU) for r in rels]


def fuse(answers: list[tuple[str, list, dict | None]], k: int) -> list[tuple]:
    """Reciprocal-rank fusion of (name, hits, relevance) answers weighted by `weights`; ties keep the given
    order (the current repository first)."""
    scored = []
    for order, ((name, hits, _rel), weight) in enumerate(zip(answers, weights([a[2] for a in answers]), strict=True)):
        for rank, hit in enumerate(hits):
            scored.append((weight / (RRF_K + rank), -order, name, hit))
    scored.sort(key=lambda s: (s[0], s[1]), reverse=True)
    return [(name, hit) for _s, _o, name, hit in scored[:k]]


def _out_of_reach(answers: dict[int, tuple], weight: list[float], k: int, candidate: float) -> bool:
    """True when even the first hit of a repository with this weight would rank below the k-th merged hit."""
    scores = sorted((weight[i] / (RRF_K + rank) for i, (_n, hits, _r) in answers.items() for rank in range(len(hits))),
                    reverse=True)
    return len(scores) >= k > 0 and candidate / RRF_K < scores[k - 1]


def _ask(kg, current: Path, entry: dict, query: str, k: int, mode: str, scope: str) -> tuple[list, dict | None]:
    """One repository's hits (none when k is 0) and relevance."""
    if Path(entry["db"]) == current:
        eng = kg.engine
        return (eng.search(query, k=k, mode=mode, scope=scope) if k else []), eng.relevance(query)
    return _remote(entry, query, k, mode, scope)


def labelled(name: str, hit):
    """A copy of the hit whose path names its repository: `[billing] src/x.py`."""
    import copy

    shown = copy.copy(hit)
    shown.path = f"[{name}] {hit.path}" if hit.path else f"[{name}]"
    return shown


def _remote(entry: dict, query: str, k: int, mode: str, scope: str) -> tuple[list, dict | None]:
    from . import daemon
    from .engine import Hit

    argv = ["search", query, "-k", str(k), "--json", "--relevance", "--scope", scope] + \
        (["--files"] if mode == "files" else [])
    reply = daemon.ask(Path(entry["db"]).parent, argv, cwd=Path(entry["root"]))
    if reply is not None and reply.get("code") in (0, 1):
        try:
            answer = json.loads(reply.get("stdout") or "{}")
            return [Hit(**h) for h in answer.get("hits", [])], answer.get("relevance")
        except (AttributeError, TypeError, ValueError):
            pass
    graph = _OPEN.get(entry["db"])
    if graph is None:
        from .service import KnowledgeGraph

        graph = _OPEN[entry["db"]] = KnowledgeGraph.open(None, Path(entry["db"]))
    eng = graph.engine
    return (eng.search(query, k=k, mode=mode, scope=scope) if k else []), eng.relevance(query)
