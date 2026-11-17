"""Index pipeline: scan -> extract (parallel, cached by content hash) -> link -> embed -> optimise.

A run with no changed files does nothing after the scan (stat signatures are compared first, so
unchanged files are not even read).
"""

from __future__ import annotations

import time
from concurrent.futures import ProcessPoolExecutor

from .config import Config, Source
from .embed import embed_store
from .extractors import EXTRACTOR_VERSION, Options, extract_job
from .linker import link
from .locks import index_lock
from .scanner import scan
from .store import Store

BATCH = 400


def index(cfg: Config, store: Store, only: list[str] | None = None, full: bool = False,
          embed: bool = True, log=print, wait: bool = True) -> dict:
    """Run one index pass under the store's index lock. With `wait=False` a pass already running elsewhere
    (another agent's server, a git hook) wins and this call returns `{"busy": True}` at once."""
    with index_lock(store.path, wait=wait) as acquired:
        if not acquired:
            return {"busy": True, "changed": False, "sources": {}}
        return _index(cfg, store, only, full, embed, log)


def _index(cfg: Config, store: Store, only: list[str] | None, full: bool, embed: bool, log) -> dict:
    t0 = time.perf_counter()
    report: dict = {"sources": {}}
    if store.get_meta("extractor_version") != EXTRACTOR_VERSION:
        full = True
    sources = [s for s in cfg.sources if not only or s.name in only]
    changed = full
    known = {p["name"] for p in store.projects()}
    for name in known - {s.name for s in cfg.sources}:
        if not only:
            log(f"[{name}] no longer configured: dropping")
            store.drop_project(name)
            changed = True
    for src in sources:
        rep = _index_source(cfg, store, src, full, log)
        report["sources"][src.name] = rep
        changed = changed or bool(rep["extracted"] or rep["removed"]) or src.name not in known
    store.set_meta("extractor_version", EXTRACTOR_VERSION)
    t1 = time.perf_counter()
    if changed or store.get_meta("linked") != "1":
        n_nodes, n_edges = link(store, cfg)
        store.set_meta("linked", "1")
        log(f"linked: {n_nodes} virtual nodes, {n_edges} derived edges in {time.perf_counter() - t1:.1f}s")
    if changed:
        # Publish the graph before embedding: a cold embedding of a large repository takes minutes, and a
        # running server can answer with keyword + graph ranking meanwhile (it reloads on `last_index`).
        store.set_meta("last_index", str(time.time()))
    t2 = time.perf_counter()
    settings = f"{cfg.embedder}|{cfg.embed_model}|{cfg.embed_reduce}"
    if not embed:
        report["embed"] = {"embedder": "skipped"}
    elif not changed and store.get_meta("embedded_with") == settings:
        report["embed"] = {"embedder": store.get_meta("embedder"), "embedded": 0}  # complete and unchanged
    else:
        report["embed"] = embed_store(store, cfg, log)
        store.set_meta("embedded_with", settings)
    t3 = time.perf_counter()
    if changed or report["embed"].get("embedded"):
        extracted = sum(r["extracted"] + r["removed"] for r in report["sources"].values())
        rewritten = report["embed"].get("embedded", 0) > 0.1 * max(1, store.count_vectors())
        store.optimize(merge_fts=full or extracted > 200, compact=rewritten)
        store.set_meta("last_index", str(time.time()))
    t4 = time.perf_counter()
    report["changed"] = changed
    report["timings"] = {
        "extract_s": round(t1 - t0, 2), "link_s": round(t2 - t1, 2), "embed_s": round(t3 - t2, 2),
        "optimize_s": round(t4 - t3, 2), "total_s": round(t4 - t0, 2),
    }
    report["stats"] = {k: v for k, v in store.stats().items() if k in ("files", "nodes", "edges", "embeddings")}
    return report


def _index_source(cfg: Config, store: Store, src: Source, full: bool, log) -> dict:
    if not src.path.is_dir():
        raise FileNotFoundError(f"source {src.name!r}: {src.path} is not a directory")
    old = store.file_signatures(src.name)
    files = scan(src, cfg, None if full else old)
    current = {f.path for f in files}
    removed = [p for p in old if p not in current]
    changed = [f for f in files if full or f.path not in old or old[f.path][0] != f.digest]
    touched = [(f.path, f.size, f.mtime) for f in files
               if not full and f.path in old and old[f.path][0] == f.digest and old[f.path][1:] != (f.size, f.mtime)]
    fresh = full and bool(old)
    if fresh:
        store.clear_extracted(src.name)
    store.remove_files(src.name, removed)
    store.touch_files(src.name, touched)
    opts = Options(src.name, cfg.tags, cfg.route_prefixes)
    jobs = [(opts, str(src.path), f.path, f.lang, f.family, f.digest, f.size, f.mtime) for f in changed]
    log(f"[{src.name}] {len(files)} files: {len(changed)} to extract, {len(removed)} removed")
    batch: list = []
    errors = 0

    def flush():
        nonlocal batch
        if batch:
            store.replace_files(src.name, [(d, s, m, ex) for _p, d, s, m, ex in batch], fresh=fresh)
            batch = []

    for done, result in enumerate(_run(jobs, cfg.workers), 1):
        batch.append(result)
        errors += "error" in result[4].meta
        if len(batch) >= BATCH:
            flush()
            log(f"  extracted {done}/{len(jobs)}")
    flush()
    if changed or removed or store.project_roots().get(src.name) != str(src.path):
        store.upsert_project(src.name, str(src.path), len(files))
    return {"files": len(files), "extracted": len(changed), "removed": len(removed), "errors": errors}


def _run(jobs: list, workers: int):
    if workers <= 1 or len(jobs) < 64:
        for job in jobs:
            result = _safe_job(job)
            if result is not None:
                yield result
        return
    with ProcessPoolExecutor(max_workers=workers) as pool:
        for result in pool.map(_safe_job, jobs, chunksize=24):
            if result is not None:
                yield result


def _safe_job(job):
    try:
        return extract_job(job)
    except OSError:
        return None
