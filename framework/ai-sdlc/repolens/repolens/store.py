"""SQLite property-graph store (schema v3).

Layout chosen for size and incremental speed:
  nodes     one row per node; `derived` = 1 for linker-made virtual nodes (extracted nodes belong to the
            file in `path`); `parent` = the containing node within the same file, stored relative to the file
            id ('' = the file itself), so containment needs no edge rows; `vhash` =
            content hash of the node's embedding text; long `text` is stored zlib-compressed
  edges     derived edges only, integer keyed (node rowids) and rebuilt by every link pass
  files     content hash + stat signature per file, extractor metadata, and the file's unresolved
            references as one zlib-compressed JSON blob (only the linker reads them, all at once)
  fts       contentless FTS5 (BM25) keyed by node rowid, updated incrementally
  vectors   content-addressed int8 embeddings (hash -> scale, codes), PCA-reduced by a projection
            stored in `meta`; identical texts share a vector
"""

from __future__ import annotations

import json
import sqlite3
import time
import zlib
from collections.abc import Iterable, Iterator
from pathlib import Path

import numpy as np

from .model import Edge, Extraction, Node, Ref, search_tokens, text_hash

SCHEMA_VERSION = "4"
COMPRESS_ABOVE = 160
_NODE_COLS = "id, project, kind, name, path, qualname, lang, start, end_, signature, text"

_SCHEMA = """
CREATE TABLE IF NOT EXISTS meta (key TEXT PRIMARY KEY, value BLOB);
CREATE TABLE IF NOT EXISTS projects (
    name TEXT PRIMARY KEY, root TEXT NOT NULL, indexed_at REAL, files INTEGER DEFAULT 0
);
CREATE TABLE IF NOT EXISTS files (
    project TEXT NOT NULL, path TEXT NOT NULL, hash TEXT NOT NULL, size INTEGER, mtime INTEGER,
    lang TEXT, meta TEXT, refs BLOB, PRIMARY KEY (project, path)
);
CREATE TABLE IF NOT EXISTS nodes (
    id TEXT NOT NULL UNIQUE, project TEXT NOT NULL, kind TEXT NOT NULL, name TEXT NOT NULL,
    path TEXT, qualname TEXT, lang TEXT, start INTEGER, end_ INTEGER, signature TEXT, text,
    derived INTEGER NOT NULL DEFAULT 0, parent TEXT, vhash INTEGER
);
CREATE INDEX IF NOT EXISTS nodes_file ON nodes (project, path) WHERE derived = 0;
CREATE INDEX IF NOT EXISTS nodes_name ON nodes (name);
CREATE TABLE IF NOT EXISTS edge_kinds (code INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE);
CREATE TABLE IF NOT EXISTS edges (src INTEGER NOT NULL, dst INTEGER NOT NULL, kind INTEGER NOT NULL,
    weight REAL NOT NULL);
CREATE TABLE IF NOT EXISTS vectors (hash INTEGER PRIMARY KEY, scale REAL NOT NULL, q BLOB NOT NULL);
CREATE VIRTUAL TABLE IF NOT EXISTS fts USING fts5 (
    tok_name, tok_path, tok_body, content='', contentless_delete=1,
    tokenize='porter unicode61 remove_diacritics 2'
);
CREATE VIRTUAL TABLE IF NOT EXISTS fts_vocab USING fts5vocab(fts, 'row');
"""


def quantize(vecs: np.ndarray) -> tuple[np.ndarray, np.ndarray]:
    """Symmetric per-vector int8 quantisation: v ~= q * scale."""
    scale = np.abs(vecs).max(axis=1) / 127.0
    scale[scale == 0] = 1.0
    return np.round(vecs / scale[:, None]).astype(np.int8), scale.astype(np.float32)


class Store:
    def __init__(self, path: Path | str, readonly: bool = False):
        self.path = Path(path)
        if not readonly:
            self.path.parent.mkdir(parents=True, exist_ok=True)
        uri = f"file:{self.path}?mode=ro" if readonly else f"file:{self.path}"
        self.conn = sqlite3.connect(uri, uri=True, check_same_thread=False)
        if not readonly:
            self.conn.execute("PRAGMA journal_mode = WAL")
            self.conn.execute("PRAGMA synchronous = NORMAL")
            self._migrate()
            self.conn.executescript(_SCHEMA)
            self.set_meta("schema_version", SCHEMA_VERSION)
        self._kinds: dict[str, int] | None = None

    def close(self) -> None:
        self.conn.close()

    # -- schema migration --------------------------------------------------------------------
    def _migrate(self) -> None:
        """Older schema -> current: keep the (expensive) vectors, drop the rest; files re-extract in seconds."""
        tables = {r[0] for r in self.conn.execute("SELECT name FROM sqlite_master WHERE type = 'table'")}
        if "nodes" not in tables:
            return
        version = self.get_meta("schema_version")
        if version == SCHEMA_VERSION:
            return
        model = self.get_meta("embedder")
        legacy: dict[int, np.ndarray] = {}
        if "embeddings" in tables:  # v1: float32 vectors keyed by node id
            from .embed import embed_text

            rows = self.conn.execute(
                f"SELECT {', '.join('n.' + c.strip() for c in _NODE_COLS.split(','))}, e.vec "
                "FROM nodes n JOIN embeddings e ON e.id = n.id")
            for row in rows:
                legacy[text_hash(embed_text(Node(*row[:11])))] = np.frombuffer(row[11], dtype=np.float32)
        with self.conn:
            for table in ("nodes", "edges", "edge_kinds", "refs", "files", "embeddings", "fts_vocab", "fts"):
                self.conn.execute(f"DROP TABLE IF EXISTS {table}")
        self.conn.executescript(_SCHEMA)
        if legacy:
            self._put_vectors(list(legacy), np.vstack(list(legacy.values())))
        if model:
            self.set_meta("embedder", model)
        self.set_meta("extractor_version", "migrated")
        self.vacuum()

    # -- meta / projects -------------------------------------------------------------------
    def set_meta(self, key: str, value: bytes | str) -> None:
        with self.conn:
            self.conn.execute("INSERT OR REPLACE INTO meta VALUES (?, ?)", (key, value))

    def get_meta(self, key: str, default=None):
        row = self.conn.execute("SELECT value FROM meta WHERE key = ?", (key,)).fetchone()
        return row[0] if row else default

    def upsert_project(self, name: str, root: str, files: int) -> None:
        with self.conn:
            self.conn.execute(
                "INSERT OR REPLACE INTO projects VALUES (?, ?, ?, ?)", (name, root, time.time(), files))

    def projects(self) -> list[dict]:
        rows = self.conn.execute("SELECT name, root, indexed_at, files FROM projects ORDER BY name")
        return [{"name": r[0], "root": r[1], "indexed_at": r[2], "files": r[3]} for r in rows]

    def project_roots(self) -> dict[str, str]:
        return {p["name"]: p["root"] for p in self.projects()}

    def drop_project(self, name: str) -> None:
        with self.conn:
            self._delete_fts("SELECT rowid FROM nodes WHERE project = ?", (name,))
            self.conn.execute("DELETE FROM nodes WHERE project = ?", (name,))
            self.conn.execute("DELETE FROM files WHERE project = ?", (name,))
            self.conn.execute("DELETE FROM projects WHERE name = ?", (name,))

    # -- files -----------------------------------------------------------------------------
    def file_signatures(self, project: str) -> dict[str, tuple[str, int, int]]:
        rows = self.conn.execute("SELECT path, hash, size, mtime FROM files WHERE project = ?", (project,))
        return {r[0]: (r[1], r[2], r[3]) for r in rows}

    def file_hashes(self, project: str) -> dict[str, str]:
        return {p: s[0] for p, s in self.file_signatures(project).items()}

    def file_meta(self) -> Iterator[tuple[str, str, str, dict]]:
        for project, path, lang, meta in self.conn.execute("SELECT project, path, lang, meta FROM files"):
            yield project, path, lang, json.loads(meta or "{}")

    def remove_files(self, project: str, paths: Iterable[str]) -> None:
        with self.conn:
            for path in paths:
                self._delete_file_nodes(project, path)
                self.conn.execute("DELETE FROM files WHERE project = ? AND path = ?", (project, path))

    def replace_files(self, project: str, batch: list[tuple[str, int, int, Extraction]], fresh: bool = False) -> None:
        """Atomically replace everything extracted from each (hash, size, mtime, extraction) file.
        Tokens and content hashes come precomputed from the extraction workers (`ex.aux`).
        `fresh`: the project was cleared beforehand, so there is nothing to delete."""
        with self.conn:
            for digest, size, mtime, ex in batch:
                if not fresh:
                    self._delete_file_nodes(project, ex.path)
                aux = ex.aux if len(ex.aux) == len(ex.nodes) else [node_aux(n) for n in ex.nodes]
                fid = ex.nodes[0].id if ex.nodes else ""
                parent = {e.dst: _relative(e.src, fid) for e in ex.edges if e.kind == "contains"}
                refs = list(ex.refs) + [Ref(e.src, "edge", e.dst, e.kind) for e in ex.edges if e.kind != "contains"]
                rows = [(*_node_values(n), 0, parent.get(n.id), a[3]) for n, a in zip(ex.nodes, aux, strict=True)]
                self.conn.executemany(
                    f"INSERT OR IGNORE INTO nodes ({_NODE_COLS}, derived, parent, vhash) "
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", rows)
                rowid = dict(self.conn.execute(
                    "SELECT id, rowid FROM nodes WHERE project = ? AND path = ? AND derived = 0", (project, ex.path)))
                self.conn.executemany(
                    "INSERT INTO fts (rowid, tok_name, tok_path, tok_body) VALUES (?,?,?,?)",
                    [(rowid[n.id], *a[:3]) for n, a in zip(ex.nodes, aux, strict=True) if n.id in rowid])
                self.conn.execute(
                    "INSERT OR REPLACE INTO files VALUES (?,?,?,?,?,?,?,?)",
                    (project, ex.path, digest, size, mtime, ex.lang, json.dumps(ex.meta) if ex.meta else None,
                     _pack_refs(ex.nodes[0].id if ex.nodes else "", refs)))

    def clear_extracted(self, project: str) -> None:
        """Drop everything a project's files produced (before a full re-extract: one bulk delete instead
        of one per file). Derived rows are rebuilt by the next link pass anyway."""
        with self.conn:
            if {p["name"] for p in self.projects()} <= {project}:
                self.conn.execute("INSERT INTO fts(fts) VALUES ('delete-all')")
                self.conn.execute("DELETE FROM nodes")
                self.conn.execute("DELETE FROM edges")
            else:
                self._delete_fts("SELECT rowid FROM nodes WHERE project = ?", (project,))
                self.conn.execute("DELETE FROM nodes WHERE project = ?", (project,))
            self.conn.execute("UPDATE files SET hash = '' WHERE project = ?", (project,))

    def touch_files(self, project: str, rows: list[tuple[str, int, int]]) -> None:
        """Refresh stat signatures of files whose content hash did not change."""
        with self.conn:
            self.conn.executemany("UPDATE files SET size = ?, mtime = ? WHERE project = ? AND path = ?",
                                  [(size, mtime, project, path) for path, size, mtime in rows])

    def _delete_file_nodes(self, project: str, path: str) -> None:
        where = "project = ? AND path = ? AND derived = 0"
        self._delete_fts(f"SELECT rowid FROM nodes WHERE {where}", (project, path))
        self.conn.execute(f"DELETE FROM nodes WHERE {where}", (project, path))

    def _delete_fts(self, select_rowids: str, params: tuple) -> None:
        self.conn.execute(f"DELETE FROM fts WHERE rowid IN ({select_rowids})", params)

    def refs(self) -> Iterator[tuple[str, str, str, str, str]]:
        for project, blob in self.conn.execute("SELECT project, refs FROM files WHERE refs IS NOT NULL"):
            yield from ((project, *ref) for ref in _unpack_refs(blob))

    # -- derived graph ---------------------------------------------------------------------
    def replace_derived(self, nodes: Iterable[Node], edges: Iterable[Edge]) -> None:
        """Replace the linker's output. Derived nodes are diffed (unchanged ones keep their row and FTS
        entry: rewriting ~10k of them on every run fragments the FTS index and slows every query);
        derived edges are rewritten."""
        from .embed import embed_text

        with self.conn:
            old = {r[0]: (r[1], r[2:]) for r in self.conn.execute(
                f"SELECT id, rowid, {_NODE_COLS}, vhash FROM nodes WHERE derived = 1")}
            kept, fresh = set(), []
            for n in nodes:
                values = (*_node_values(n), text_hash(embed_text(n)))
                previous = old.get(n.id)
                if previous is not None and previous[1] == values:
                    kept.add(n.id)
                elif n.id not in kept:
                    fresh.append((n, values))
            stale = [(rowid,) for nid, (rowid, _) in old.items() if nid not in kept]
            self.conn.executemany("DELETE FROM fts WHERE rowid = ?", stale)
            self.conn.executemany("DELETE FROM nodes WHERE rowid = ?", stale)
            fts_rows = []
            for n, values in fresh:
                cursor = self.conn.execute(
                    f"INSERT OR IGNORE INTO nodes ({_NODE_COLS}, derived, parent, vhash) "
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,1,NULL,?)", values)
                if cursor.rowcount:
                    fts_rows.append((cursor.lastrowid, *node_aux(n)[:3]))
            self.conn.executemany("INSERT INTO fts (rowid, tok_name, tok_path, tok_body) VALUES (?,?,?,?)", fts_rows)
            self.conn.execute("DELETE FROM edges")
            rowid = dict(self.conn.execute("SELECT id, rowid FROM nodes"))
            kinds = self.kind_codes()
            rows = []
            for e in edges:
                s, d = rowid.get(e.src), rowid.get(e.dst)
                if s is not None and d is not None:
                    if e.kind not in kinds:
                        kinds[e.kind] = len(kinds)
                        self.conn.execute("INSERT INTO edge_kinds VALUES (?, ?)", (kinds[e.kind], e.kind))
                    rows.append((s, d, kinds[e.kind], e.weight))
                    if len(rows) >= 100_000:  # bounded batches: a large graph has millions of edges
                        self.conn.executemany("INSERT INTO edges VALUES (?,?,?,?)", rows)
                        rows.clear()
            self.conn.executemany("INSERT INTO edges VALUES (?,?,?,?)", rows)

    def kind_codes(self) -> dict[str, int]:
        if self._kinds is None:
            self._kinds = {name: code for code, name in self.conn.execute("SELECT code, name FROM edge_kinds")}
        return self._kinds

    def optimize(self, merge_fts: bool = True, compact: bool = False) -> None:
        """After an index run: merge FTS segments (large changes only; FTS5 auto-merges small ones),
        checkpoint the WAL and reclaim free pages (always when `compact`: after rewriting many rows the
        pages are half empty without being free)."""
        if merge_fts:
            with self.conn:
                self.conn.execute("INSERT INTO fts(fts) VALUES ('optimize')")
        self.conn.execute("PRAGMA wal_checkpoint(TRUNCATE)")
        free, total = (self.conn.execute(f"PRAGMA {p}").fetchone()[0] for p in ("freelist_count", "page_count"))
        if compact or (total and free / total > 0.2):
            self.vacuum()

    def vacuum(self) -> None:
        self.conn.execute("VACUUM")
        self.conn.execute("PRAGMA wal_checkpoint(TRUNCATE)")

    # -- reads -----------------------------------------------------------------------------
    def nodes(self, extracted_only: bool = False, with_text: bool = True) -> Iterator[Node]:
        """All nodes; `with_text=False` leaves signature and text empty (the linker needs neither)."""
        cols = _NODE_COLS if with_text else _NODE_COLS.replace("signature, text", "'', ''")
        sql = f"SELECT {cols} FROM nodes"
        if extracted_only:
            sql += " WHERE derived = 0"
        for r in self.conn.execute(sql):
            yield _node(r)

    def edges(self) -> Iterator[tuple[str, str, str, float]]:
        """All edges as (src id, dst id, kind, weight), containment included."""
        for parent, project, path, nid in self.conn.execute(
                "SELECT parent, project, path, id FROM nodes WHERE parent IS NOT NULL"):
            yield parent_id(project, path, parent), nid, "contains", 1.0
        yield from self.conn.execute(
            "SELECT a.id, b.id, k.name, e.weight FROM edges e JOIN nodes a ON a.rowid = e.src "
            "JOIN nodes b ON b.rowid = e.dst JOIN edge_kinds k ON k.code = e.kind")

    def get_node(self, node_id: str) -> Node | None:
        row = self.conn.execute(f"SELECT {_NODE_COLS} FROM nodes WHERE id = ?", (node_id,)).fetchone()
        return _node(row) if row else None

    def fts(self, match: str, limit: int) -> list[tuple[int, float]]:
        """BM25 hits as (node rowid, score); higher is better."""
        sql = "SELECT rowid, bm25(fts, 6.0, 2.0, 1.0) AS score FROM fts WHERE fts MATCH ? ORDER BY score LIMIT ?"
        try:
            return [(r[0], -r[1]) for r in self.conn.execute(sql, (match, limit))]
        except sqlite3.OperationalError:
            return []

    # -- vectors ---------------------------------------------------------------------------
    def missing_vectors(self, skip_kinds: Iterable[str]) -> Iterator[Node]:
        """Nodes whose embedding text has no vector yet (one representative per distinct text), streamed."""
        skip = list(skip_kinds)
        marks = ",".join("?" * len(skip))
        rows = self.conn.execute(
            f"SELECT {_NODE_COLS} FROM nodes WHERE kind NOT IN ({marks}) AND vhash NOT IN "
            "(SELECT hash FROM vectors) GROUP BY vhash", skip)
        yield from (_node(r) for r in rows.fetchall())

    def put_vectors(self, hashes: list[int], vecs: np.ndarray) -> None:
        with self.conn:
            self._put_vectors(hashes, vecs)

    def _put_vectors(self, hashes: list[int], vecs: np.ndarray) -> None:
        q, scale = quantize(np.asarray(vecs, dtype=np.float32))
        self.conn.executemany("INSERT OR REPLACE INTO vectors VALUES (?,?,?)",
                              [(h, float(s), row.tobytes()) for h, s, row in zip(hashes, scale, q, strict=True)])

    def prune_vectors(self) -> None:
        with self.conn:
            self.conn.execute("DELETE FROM vectors WHERE hash NOT IN (SELECT vhash FROM nodes WHERE vhash IS NOT NULL)")

    def clear_vectors(self) -> None:
        with self.conn:
            self.conn.execute("DELETE FROM vectors")
            self.conn.execute("DELETE FROM meta WHERE key IN ('pca', 'pca_for')")

    def vector_chunks(self, size: int = 20000) -> Iterator[tuple[list[int], np.ndarray]]:
        """Every stored vector, dequantised, `size` at a time in hash order (keyset paging, so callers may
        rewrite the chunk they were given)."""
        last = -(2 ** 63)
        while True:
            rows = self.conn.execute("SELECT hash, scale, q FROM vectors WHERE hash > ? ORDER BY hash LIMIT ?",
                                     (last, size)).fetchall()
            if not rows:
                return
            yield [r[0] for r in rows], _dequantize(rows)
            last = rows[-1][0]

    def sample_vectors(self, limit: int) -> np.ndarray:
        """About `limit` stored vectors, dequantised. Hashes are uniform, so a stride over them is random."""
        step = max(1, self.count_vectors() // limit)
        rows = self.conn.execute("SELECT hash, scale, q FROM vectors WHERE hash % ? = 0", (step,)).fetchall()
        return _dequantize(rows) if rows else np.zeros((0, 0), dtype=np.float32)

    def count_vectors(self) -> int:
        return self.conn.execute("SELECT COUNT(*) FROM vectors").fetchone()[0]

    # -- stats -----------------------------------------------------------------------------
    def stats(self) -> dict:
        q = self.conn.execute
        kinds = dict(q("SELECT k.name, COUNT(*) FROM edges e JOIN edge_kinds k ON k.code = e.kind GROUP BY k.name"))
        kinds["contains"] = q("SELECT COUNT(*) FROM nodes WHERE parent IS NOT NULL").fetchone()[0]
        size = sum(p.stat().st_size for p in self.path.parent.glob(self.path.name + "*"))
        return {
            "projects": self.projects(),
            "files": q("SELECT COUNT(*) FROM files").fetchone()[0],
            "nodes": q("SELECT COUNT(*) FROM nodes").fetchone()[0],
            "edges": sum(kinds.values()),
            "embeddings": self.count_vectors(),
            "nodes_by_kind": dict(q("SELECT kind, COUNT(*) FROM nodes GROUP BY kind ORDER BY 2 DESC")),
            "edges_by_kind": dict(sorted(kinds.items(), key=lambda kv: -kv[1])),
            "files_by_lang": dict(q("SELECT lang, COUNT(*) FROM files GROUP BY lang ORDER BY 2 DESC")),
            "db_bytes": size,
        }


def node_aux(n: Node) -> tuple[str, str, str, int]:
    """What the store needs per node besides its fields: FTS token columns and the embedding-text hash."""
    from .embed import embed_text

    return (search_tokens(n.name, n.qualname), search_tokens(n.path), search_tokens(n.signature, n.text),
            text_hash(embed_text(n)))


def _node_values(n: Node) -> tuple:
    text = n.text
    if text and len(text) > COMPRESS_ABOVE:
        text = zlib.compress(text.encode("utf-8"), 6)
    return (n.id, n.project, n.kind, n.name, n.path, n.qualname, n.lang, n.start, n.end, n.signature, text)


def _dequantize(rows: list[tuple]) -> np.ndarray:
    dim = len(rows[0][2])
    q = np.frombuffer(b"".join(r[2] for r in rows), dtype=np.int8).reshape(len(rows), dim)
    return q.astype(np.float32) * np.array([r[1] for r in rows], dtype=np.float32)[:, None]


def _relative(node_id: str, file_id: str) -> str:
    if node_id == file_id:
        return ""
    return node_id[len(file_id) + 1:] if node_id.startswith(file_id + "#") else "\0" + node_id


def parent_id(project: str, path: str, rel: str) -> str:
    """Absolute id of a parent stored relative to its file ('' = the file, '\0' prefix = absolute)."""
    if rel.startswith("\0"):
        return rel[1:]
    return f"{project}:{path}" + (f"#{rel}" if rel else "")


def _text(value) -> str:
    return zlib.decompress(value).decode("utf-8") if isinstance(value, bytes) else (value or "")


def _node(row: tuple) -> Node:
    return Node(*row[:10], _text(row[10]))


def _pack_refs(file_id: str, refs: list[Ref]) -> bytes | None:
    """Refs as compressed JSON; source ids are stored relative to the file id (they all start with it)."""
    if not refs:
        return None
    n = len(file_id)
    rows = [[r.src[n:] if r.src.startswith(file_id) else "\0" + r.src, r.kind, r.target, r.extra] for r in refs]
    return zlib.compress(json.dumps([file_id, rows], separators=(",", ":")).encode("utf-8"), 6)


def _unpack_refs(blob: bytes) -> Iterator[tuple[str, str, str, str]]:
    file_id, rows = json.loads(zlib.decompress(blob))
    for src, kind, target, extra in rows:
        yield (src[1:] if src.startswith("\0") else file_id + src), kind, target, extra
