"""Shared vector cache: content-addressed vectors of one embedding model, reused by every store on the machine.

A vector depends only on the text it embeds (kind, name, signature, doc, path) and on the model and projection,
never on the branch, worktree or repository. Each worktree keeps its own store (an index never mixes branches),
and before embedding a store copies from this cache every vector whose text hash it needs; after embedding it
adds its new vectors. A new worktree, a branch switch or a long-lived branch therefore embeds only text that
no store on the machine has embedded before.

The cache also holds the model's PCA basis: stores that share it produce identical vectors. A store that
already fitted a different basis keeps working on its own (it neither reads nor writes the cache).

Location: `$REPOLENS_VECTOR_CACHE` (a folder), `off` to disable, default `$REPOLENS_HOME/vectors`.
"""

from __future__ import annotations

import os
import re
import sqlite3
from pathlib import Path

from .paths import home

SCHEMA = """
CREATE TABLE IF NOT EXISTS meta (key TEXT PRIMARY KEY, value BLOB);
CREATE TABLE IF NOT EXISTS vectors (hash INTEGER PRIMARY KEY, scale REAL NOT NULL, q BLOB NOT NULL);
"""


def cache_path(model: str) -> Path | None:
    setting = os.environ.get("REPOLENS_VECTOR_CACHE", "")
    if setting.lower() == "off":
        return None
    folder = Path(setting).expanduser() if setting else home() / "vectors"
    return folder / (re.sub(r"[^A-Za-z0-9._-]+", "_", model).strip("_") + ".db")


class VectorCache:
    def __init__(self, path: Path, model: str = ""):
        self.path = path
        path.parent.mkdir(parents=True, exist_ok=True)
        self.conn = sqlite3.connect(path, timeout=60)
        self.conn.execute("PRAGMA journal_mode = WAL")
        self.conn.executescript(SCHEMA)
        if model:  # recorded so an exported copy knows which cache it belongs to
            with self.conn:
                self.conn.execute("INSERT OR IGNORE INTO meta VALUES ('model', ?)", (model,))

    @classmethod
    def open(cls, model: str) -> VectorCache | None:
        path = cache_path(model)
        if path is None:
            return None
        try:
            return cls(path, model)
        except sqlite3.Error:
            return None

    def close(self) -> None:
        self.conn.close()

    def projection(self) -> bytes | None:
        row = self.conn.execute("SELECT value FROM meta WHERE key = 'pca'").fetchone()
        return row[0] if row else None

    def adopt_projection(self, blob: bytes) -> bytes:
        """Record `blob` as the cache's basis unless it has one; returns the basis in effect."""
        with self.conn:
            self.conn.execute("INSERT OR IGNORE INTO meta VALUES ('pca', ?)", (blob,))
        return self.projection()

    def count(self) -> int:
        return self.conn.execute("SELECT COUNT(*) FROM vectors").fetchone()[0]


def pull(store, cache: VectorCache, skip_kinds) -> int:
    """Copy into the store every cached vector one of its nodes needs and it does not have yet."""
    skip = list(skip_kinds)
    marks = ",".join("?" * len(skip))
    conn = store.conn
    conn.execute("ATTACH DATABASE ? AS vcache", (str(cache.path),))
    try:
        with conn:
            before = conn.total_changes
            conn.execute(
                "INSERT OR IGNORE INTO main.vectors (hash, scale, q) SELECT c.hash, c.scale, c.q FROM vcache.vectors c "
                f"WHERE c.hash IN (SELECT vhash FROM main.nodes WHERE vhash IS NOT NULL AND kind NOT IN ({marks})) "
                "AND c.hash NOT IN (SELECT hash FROM main.vectors)", skip)
            return conn.total_changes - before
    finally:
        conn.execute("DETACH DATABASE vcache")


def push(store, cache: VectorCache) -> int:
    """Add the store's vectors the cache does not have."""
    conn = store.conn
    conn.execute("ATTACH DATABASE ? AS vcache", (str(cache.path),))
    try:
        with conn:
            before = conn.total_changes
            conn.execute("INSERT OR IGNORE INTO vcache.vectors (hash, scale, q) "
                         "SELECT hash, scale, q FROM main.vectors")
            return conn.total_changes - before
    finally:
        conn.execute("DETACH DATABASE vcache")


# -- moving vectors between machines (`repolens cache export/import/info`) ------------------------
class CacheError(Exception):
    pass


def model_name(model: str) -> str:
    """The cache key of a fastembed model, as `embed_store` names it."""
    return model if ":" in model else f"fastembed:{model}"


def info(model: str) -> dict | None:
    path = cache_path(model_name(model))
    if path is None or not path.exists():
        return None
    conn = sqlite3.connect(f"file:{path}?mode=ro", uri=True)
    try:
        count = conn.execute("SELECT COUNT(*) FROM vectors").fetchone()[0]
        basis = conn.execute("SELECT value FROM meta WHERE key = 'pca'").fetchone()
    finally:
        conn.close()
    return {"path": str(path), "vectors": count, "projection": basis is not None,
            "bytes": path.stat().st_size}


def export(model: str, target: Path) -> dict:
    """Write the model's cache to one compact SQLite file (for another machine, a CI artifact...)."""
    source = cache_path(model_name(model))
    if source is None or not source.exists():
        raise CacheError(f"no vector cache for {model_name(model)}")
    if target.exists():
        raise CacheError(f"{target} exists; not overwriting")
    cache = VectorCache(source, model_name(model))  # records the model in caches made before 0.5
    try:
        cache.conn.execute("VACUUM INTO ?", (str(target),))
    finally:
        cache.close()
    return {"path": str(target), "bytes": target.stat().st_size}


def import_file(source: Path) -> dict:
    """Merge an exported cache into this machine's cache. Both must share the PCA basis (or this one has
    none yet and adopts it): vectors in different projections are not comparable."""
    con = sqlite3.connect(f"file:{source}?mode=ro", uri=True)
    try:
        name = con.execute("SELECT value FROM meta WHERE key = 'model'").fetchone()
        basis = con.execute("SELECT value FROM meta WHERE key = 'pca'").fetchone()
    finally:
        con.close()
    if name is None:
        raise CacheError(f"{source} is not a repolens vector cache (no model recorded)")
    cache = VectorCache.open(name[0] if isinstance(name[0], str) else name[0].decode())
    if cache is None:
        raise CacheError("the vector cache is disabled (REPOLENS_VECTOR_CACHE=off)")
    try:
        if basis is not None and cache.adopt_projection(basis[0]) != basis[0]:
            raise CacheError(f"{source} uses a different projection than {cache.path}; import it into an empty "
                             "cache (REPOLENS_VECTOR_CACHE) instead")
        before = cache.count()
        cache.conn.execute("ATTACH DATABASE ? AS incoming", (str(source),))
        with cache.conn:
            cache.conn.execute("INSERT OR IGNORE INTO main.vectors SELECT hash, scale, q FROM incoming.vectors")
        cache.conn.execute("DETACH DATABASE incoming")
        return {"path": str(cache.path), "added": cache.count() - before, "vectors": cache.count()}
    finally:
        cache.close()
