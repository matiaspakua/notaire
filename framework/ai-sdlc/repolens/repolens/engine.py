"""Query engine: in-memory CSR graph + hybrid anchors (BM25, dense vectors, exact names) + Personalized PageRank.

Two-step retrieval as in the Code Knowledge Graph lesson: find anchor nodes by similarity, then let a
typed, hub-damped PageRank walk surface the structurally related nodes the query never names.
"""

from __future__ import annotations

import fnmatch
import heapq
import re
import sqlite3
import sys
import threading
import time
from array import array
from collections import deque
from dataclasses import asdict, dataclass, field
from pathlib import Path

import numpy as np
from scipy.sparse import csr_matrix

from .config import Config
from .embed import query_embedder
from .model import SYMBOL_KINDS, search_tokens
from .store import Store

HIDDEN_KINDS = frozenset({"dir", "project", "external"})
DOC_LANGS = frozenset({"md", "mdx", "markdown", "txt", "rst", "adoc", "pdf", "text", "html"})
SCOPES = ("all", "code", "docs", "tests")
# Tuned on bench/tasks/notaire.yaml and validated on the held-out set (README, "Benchmark").
# Override any value per profile under [ranking.tuning].
DEFAULT_TUNING = {
    "anchors": 7, "doc_anchors": 1, "anchor_power": 2.0,   # seeds: best code nodes + best doc nodes
    "rrf_k": 60, "w_fts": 1.0, "w_dense": 1.5,             # reciprocal-rank fusion of BM25 and dense lists
    "w_text": 0.2, "w_graph": 0.8, "restart": 0.15,        # final blend and PageRank restart
    "file_rest": 0.15,                                     # extra credit for a file's 2nd-4th best nodes
    "max_df": 0.15,                                        # drop query terms present in >15% of nodes
    "ppr_eps": 1e-5,                                       # sparse PageRank: mass below this is not propagated
    "ppr_sparse_from": 150_000,                            # graphs with fewer nodes use exact PageRank
    "test_factor": 0.5,                                    # weight of test files unless the query asks for tests
}
DEPENDENCY_EDGES = frozenset({
    "calls", "references", "inherits", "imports", "http_call", "handles", "tests", "uses_table",
    "defines_table", "mentions", "links",
})
STOPWORDS = frozenset("""
a an and are as at be by can do does for from how i in is it its me of on or show that the their this to
what when where which who why with find list give get all any each code file files class classes function
functions method methods implemented implement implementation used use uses using defined define located
should would could my our we you there here into about over between el la los las de del que en y un una
para por con se es como donde cual cuales
""".split())
_TEST_WORDS = re.compile(r"\b(tests?|testing|tested|specs?|coverage|covers?|covered|mocks?|fixtures?|e2e)\b", re.I)
_TEST_IDENT = re.compile(r"[a-z0-9](Tests?|IT|Spec)\b|\b(test_\w+|\w+_test)\b|\.(test|spec)\.[jt]sx?\b")
_IDENT_IN_QUERY = re.compile(r"[\w$.-]*[a-z][A-Z][\w$.]*|\w+_\w+|[\w-]+(?:/[\w.-]+)+|[\w-]+\.[a-z]{1,5}\b")


@dataclass
class Hit:
    id: str
    kind: str
    name: str
    path: str
    start: int
    end: int
    score: float
    project: str = ""
    qualname: str = ""
    signature: str = ""
    why: str = ""
    symbols: list[dict] = field(default_factory=list)

    def to_dict(self) -> dict:
        return asdict(self)


class Engine:
    def __init__(self, store: Store, cfg: Config):
        self.store = store
        self.cfg = cfg
        self._lock = threading.RLock()
        self._embedder = None
        self._embedder_loaded = False
        self.loaded_at = None
        self.tuning = {**DEFAULT_TUNING, **cfg.tuning}
        self.timings: dict[str, float] = {}
        self._vocab: dict[str, int] | None = None
        self._tokenizer: sqlite3.Connection | None = None
        self.load()

    # -- loading ---------------------------------------------------------------------------
    def load(self) -> None:
        """Load what ranking touches for every node (kind, project, path, lines, edges, vectors) as compact
        arrays, streamed from SQLite. Ids, names and qualified names are read per node only when a result
        is rendered (`_Column`): they are the bulk of the strings and a query shows a few dozen of them."""
        with self._lock:
            conn = self.store.conn
            intern = sys.intern
            rowids, self.project, self.kind, self.path, self.lang = [], [], [], [], []
            self.start, self.end = array("i"), array("i")
            vhashes = []
            cursor = conn.execute("SELECT rowid, project, kind, path, start, end_, lang, vhash FROM nodes")
            for rowid, project, kind, path, start, end, lang, vhash in cursor:
                rowids.append(rowid)
                self.project.append(intern(project))
                self.kind.append(intern(kind))
                self.path.append(intern(path or ""))
                self.start.append(start or 0)
                self.end.append(end or 0)
                self.lang.append(intern(lang or ""))
                vhashes.append(vhash)
            self.rowids = np.array(rowids, dtype=np.int64)
            del rowids
            self.by_rowid = np.full(int(self.rowids.max()) + 1 if len(self.rowids) else 1, -1, dtype=np.int64)
            self.by_rowid[self.rowids] = np.arange(len(self.rowids))
            self.ids = _Column(self.store, self.rowids, "id")
            self.name = _Column(self.store, self.rowids, "name")
            self.qual = _Column(self.store, self.rowids, "qualname")
            self.files = {f"{p}:{path}": i for i, (p, k, path) in enumerate(zip(self.project, self.kind, self.path,
                                                                                    strict=True)) if k == "file"}
            self.idx = _IdIndex(self.store, self.by_rowid, self.files)
            self.hidden = np.array([k in HIDDEN_KINDS for k in self.kind], dtype=bool)
            self.boost = self._boosts()
            tests = {p: _looks_test(p) for p in set(self.path) if p}
            self.is_test = np.fromiter((tests.get(p, False) for p in self.path), dtype=bool, count=len(self.path))
            self._load_edges(conn, self._containment(conn))
            self._load_embeddings(vhashes)
            self.roots = self.store.project_roots()
            self.loaded_at = self.store.get_meta("last_index")
            self._vocab = None
            # The index may have refitted its embedding space (LSA, a new PCA basis): rebuild the query side
            # from the store on next use (the ONNX model itself is shared and stays loaded).
            self._embedder, self._embedder_loaded = None, False

    def _containment(self, conn) -> np.ndarray:
        """(parent, child) index pairs. `parent` is stored relative to the file id ('' = the file itself,
        a NUL prefix marks an absolute id); SQLite resolves it through the unique id index."""
        rows = conn.execute(
            "SELECT p.rowid, c.rowid FROM nodes c JOIN nodes p ON p.id = CASE "
            "WHEN substr(c.parent, 1, 1) = char(0) THEN substr(c.parent, 2) "
            "WHEN c.parent = '' THEN c.project || ':' || c.path "
            "ELSE c.project || ':' || c.path || '#' || c.parent END WHERE c.parent IS NOT NULL")
        pairs = np.array(rows.fetchall(), dtype=np.int64).reshape(-1, 2)
        return self.by_rowid[pairs] if len(pairs) else pairs

    def sig(self, i: int) -> str:
        row = self.store.conn.execute("SELECT signature FROM nodes WHERE rowid = ?", (int(self.rowids[i]),)).fetchone()
        return (row[0] or "") if row else ""

    def _boosts(self) -> np.ndarray:
        boost = np.ones(len(self.ids))
        if self.cfg.demote:
            pats = self.cfg.demote
            for i, p in enumerate(self.path):
                if p and any(fnmatch.fnmatch(p, pat) or fnmatch.fnmatch("/" + p, pat) for pat in pats):
                    boost[i] = self.cfg.demote_factor
        return boost

    def in_scope(self, i: int, scope: str) -> bool:
        if scope == "all":
            return True
        if scope == "tests":
            return _looks_test(self.path[i])
        is_doc = self.lang[i] in DOC_LANGS or self.kind[i] in ("section", "tag")
        return is_doc if scope == "docs" else not is_doc

    def _load_edges(self, conn, contains: np.ndarray) -> None:
        n = len(self.ids)
        names = dict(conn.execute("SELECT code, name FROM edge_kinds"))
        kind_codes = {"contains": 0}
        remap = {code: kind_codes.setdefault(name, len(kind_codes)) for code, name in sorted(names.items())}
        count = conn.execute("SELECT COUNT(*) FROM edges").fetchone()[0]
        src_e = np.empty(count, dtype=np.int64)
        dst_e = np.empty(count, dtype=np.int64)
        kind_e = np.empty(count, dtype=np.int16)
        weight_e = np.empty(count, dtype=np.float64)
        cursor = conn.execute("SELECT src, dst, kind, weight FROM edges")
        filled = 0
        while filled < count:
            chunk = cursor.fetchmany(65536)
            if not chunk:
                break
            block = np.array(chunk, dtype=np.float64)
            stop = filled + len(block)
            src_e[filled:stop] = block[:, 0]
            dst_e[filled:stop] = block[:, 1]
            kind_e[filled:stop] = block[:, 2]
            weight_e[filled:stop] = block[:, 3]
            filled = stop
        src_e, dst_e, kind_e, weight_e = src_e[:filled], dst_e[:filled], kind_e[:filled], weight_e[:filled]
        table = np.zeros(max(remap, default=0) + 1, dtype=np.int16)
        for code, mapped in remap.items():
            table[code] = mapped
        kind_e = table[kind_e]
        in_range = (src_e < len(self.by_rowid)) & (dst_e < len(self.by_rowid))
        src_e = np.where(in_range, self.by_rowid[np.minimum(src_e, len(self.by_rowid) - 1)], -1)
        dst_e = np.where(in_range, self.by_rowid[np.minimum(dst_e, len(self.by_rowid) - 1)], -1)
        ok = (src_e >= 0) & (dst_e >= 0)
        cont = contains
        s = np.concatenate([cont[:, 0], src_e[ok]])
        d = np.concatenate([cont[:, 1], dst_e[ok]])
        k = np.concatenate([np.zeros(len(cont), dtype=np.int16), kind_e[ok]])
        w = np.concatenate([np.ones(len(cont)), weight_e[ok]])
        self.kind_names = sorted(kind_codes, key=kind_codes.get)
        self.out_ptr, self.out_nbr, self.out_kind = _csr(s, d, k, n)
        self.in_ptr, self.in_nbr, self.in_kind = _csr(d, s, k, n)
        base = np.array([self.cfg.edge_weights.get(name, 0.5) for name in self.kind_names] or [0.5])
        eff = base[k] * np.minimum(w, 3.0) if len(k) else np.zeros(0)
        deg = np.bincount(np.concatenate([s, d]), minlength=n).astype(np.float64)
        damp = 1.0 / np.log2(2.0 + deg)
        fw = eff * damp[d]
        bw = eff * self.cfg.reverse_weight * damp[s]
        rows = np.concatenate([s, d])
        cols = np.concatenate([d, s])
        vals = np.concatenate([fw, bw])
        mat = csr_matrix((vals, (rows, cols)), shape=(n, n))
        out_sum = np.asarray(mat.sum(axis=1)).ravel()
        self.dangling = out_sum == 0
        out_sum[out_sum == 0] = 1.0
        inv = csr_matrix((1.0 / out_sum, (np.arange(n), np.arange(n))), shape=(n, n))
        self.transition = (inv @ mat).tocsr()

    def _load_embeddings(self, vhashes: list) -> None:
        """int8 vectors from the store, decoded chunk by chunk straight into a resident float16 matrix
        (one row per node; half the memory of float32 for ~1 ms more per query)."""
        wanted: dict[int, list[int]] = {}
        rows: list[int] = []
        for i, h in enumerate(vhashes):
            if h is not None and not self.hidden[i]:
                wanted.setdefault(h, []).append(len(rows))
                rows.append(i)
        self.emb, self.emb_rows = None, None
        if not rows:
            return
        first = self.store.conn.execute("SELECT length(q) FROM vectors LIMIT 1").fetchone()
        if first is None:
            return
        dim = first[0]
        emb = np.zeros((len(rows), dim), dtype=np.float16)
        present = np.zeros(len(rows), dtype=bool)
        cursor = self.store.conn.execute("SELECT hash, scale, q FROM vectors WHERE length(q) = ?", (dim,))
        while True:
            chunk = cursor.fetchmany(8192)
            if not chunk:
                break
            keep = [r for r in chunk if r[0] in wanted]
            if not keep:
                continue
            block = np.frombuffer(b"".join(r[2] for r in keep), dtype=np.int8).reshape(len(keep), dim)
            block = block.astype(np.float32) * np.array([r[1] for r in keep], dtype=np.float32)[:, None]
            norms = np.linalg.norm(block, axis=1, keepdims=True)
            norms[norms == 0] = 1.0
            block = (block / norms).astype(np.float16)
            for k, r in enumerate(keep):
                for target in wanted[r[0]]:
                    emb[target] = block[k]
                    present[target] = True
        if not present.any():
            return
        self.emb = emb if present.all() else emb[present]
        self.emb_rows = np.array(rows, dtype=np.int64)[present]

    def similarities(self, q: np.ndarray) -> np.ndarray:
        q = q.astype(np.float32)
        out = np.empty(len(self.emb), dtype=np.float32)
        step = 8192
        for start in range(0, len(self.emb), step):
            out[start:start + step] = self.emb[start:start + step].astype(np.float32) @ q
        return out

    def refresh(self) -> bool:
        """Reload when another process re-indexed the store."""
        if self.store.get_meta("last_index") != self.loaded_at:
            self.load()
            return True
        return False

    @property
    def embedder(self):
        if not self._embedder_loaded:
            self._embedder_loaded = True
            try:
                self._embedder = query_embedder(self.store, self.cfg) if self.emb is not None else None
            except Exception:  # noqa: BLE001 - fall back to lexical + graph only
                self._embedder = None
        return self._embedder

    def warm(self) -> None:
        if self.embedder is not None:
            self.embedder.embed_query("warm up")

    # -- search ----------------------------------------------------------------------------
    def search(self, query: str, k: int = 10, kinds: list[str] | None = None, project: str | None = None,
               mode: str = "nodes", n_anchors: int | None = None, graph: bool = True, semantic: bool = True,
               scope: str = "all") -> list[Hit]:
        with self._lock:
            t = self.tuning
            boost = self.boost
            if t["test_factor"] != 1.0 and not (_TEST_WORDS.search(query) or _TEST_IDENT.search(query)):
                boost = np.where(self.is_test, boost * t["test_factor"], boost)
            fused = self._anchors(query, semantic=semantic)
            if project:
                fused = {i: s for i, s in fused.items() if self.project[i] == project}
            if not fused:
                return []
            fused = {i: s * boost[i] for i, s in fused.items()}
            top = sorted(fused, key=fused.get, reverse=True)
            anchors = self._balanced(top, int(n_anchors or t["anchors"]), int(t["doc_anchors"]))
            fmax = fused[top[0]]
            scores: dict[int, float] = {i: t["w_text"] * fused[i] / fmax for i in top[:300]}
            if graph and len(self.ids):
                clock = time.perf_counter()
                seeds = {i: (fused[i] / fmax) ** t["anchor_power"] for i in anchors}
                nodes, mass = self.ppr(seeds, restart=t["restart"])
                self.timings["ppr"] = time.perf_counter() - clock
                if len(nodes):
                    top = np.argpartition(-mass, min(600, len(mass) - 1))[:600]
                    pmax = float(mass[top].max()) or 1.0
                    for i, m in zip(nodes[top].tolist(), mass[top].tolist(), strict=True):
                        scores[i] = scores.get(i, 0.0) + t["w_graph"] * boost[i] * m / pmax
            allowed = set(kinds) if kinds else None
            ranked = [i for i in sorted(scores, key=scores.get, reverse=True)
                      if not self.hidden[i] and (allowed is None or self.kind[i] in allowed)
                      and (not project or self.project[i] == project) and self.in_scope(i, scope)]
            if mode == "files":
                return self._group_files(ranked, scores, set(anchors), k)
            return [self._hit(i, scores[i], set(anchors), scores) for i in ranked[:k]]

    def _balanced(self, ranked: list[int], n_code: int, n_docs: int) -> list[int]:
        """Seed the walk from the best code nodes and the best doc nodes: natural-language questions
        otherwise pick only prose, and the graph is what relates the two."""
        code = [i for i in ranked if not self.in_scope(i, "docs")][:n_code]
        docs = [i for i in ranked if self.in_scope(i, "docs")][:n_docs]
        return code + docs

    def _anchors(self, query: str, semantic: bool = True) -> dict[int, float]:
        fused: dict[int, float] = {}
        rrf_k = self.tuning["rrf_k"]
        clock = time.perf_counter()
        tokens = self._query_terms(query)
        if tokens:
            match = " OR ".join(f'"{t}"' for t in tokens)
            for rank, (rowid, _score) in enumerate(self.store.fts(match, 150)):
                i = int(self.by_rowid[rowid]) if rowid < len(self.by_rowid) else -1
                if i >= 0:
                    fused[i] = fused.get(i, 0.0) + self.tuning["w_fts"] / (rrf_k + rank)
        self.timings["fts"] = time.perf_counter() - clock
        clock = time.perf_counter()
        if semantic and self.embedder is not None and self.emb is not None:
            q = self.embedder.embed_query(query)
            self.timings["embed_query"] = time.perf_counter() - clock
            sims = self.similarities(q)
            top = np.argpartition(-sims, min(150, len(sims) - 1))[:150]
            for rank, r in enumerate(top[np.argsort(-sims[top])]):
                if sims[r] <= 0.0:
                    break
                i = int(self.emb_rows[r])
                fused[i] = fused.get(i, 0.0) + self.tuning["w_dense"] / (rrf_k + rank)
        self.timings["dense"] = time.perf_counter() - clock
        idents = set(_IDENT_IN_QUERY.findall(query))
        if query.strip() and not re.search(r"\s", query.strip()):
            idents.add(query.strip())
        for ident in idents:
            for i in self._exact(ident):
                fused[i] = fused.get(i, 0.0) + 2.0 / rrf_k
        return {i: s for i, s in fused.items() if not self.hidden[i]}

    def _query_terms(self, query: str) -> list[str]:
        """Query tokens without stopwords and without terms so common that BM25 ignores them anyway
        (their long posting lists are what makes an OR query slow); the rarest two always stay."""
        tokens = list(dict.fromkeys(t for t in search_tokens(query).split() if t not in STOPWORDS and len(t) > 1))
        limit = self.tuning["max_df"] * max(1, len(self.ids))
        if not tokens or limit <= 0:
            return tokens[:24]
        df = dict(zip(tokens, self._doc_freqs(tokens), strict=True))
        rare = sorted(tokens, key=df.get)
        keep = {t for t in rare if df[t] <= limit} | set(rare[:2])
        return [t for t in tokens if t in keep][:24]

    def _doc_freqs(self, tokens: list[str]) -> list[int]:
        """Exact document frequency of each token: stemmed by SQLite's own tokenizer (a scratch FTS5 table
        with the index's tokenizer), then looked up in the index vocabulary. The scratch table lives on its
        own in-memory connection: writing it through the store's connection would open a transaction that
        pins this long-lived reader to an old snapshot (refreshed indexes would never become visible)."""
        if self._vocab is None:
            try:
                self._vocab = dict(self.store.conn.execute("SELECT term, doc FROM fts_vocab"))
            except sqlite3.Error:  # vocabulary unavailable: no pruning
                self._vocab = {}
        if not self._vocab:
            return [0] * len(tokens)
        if self._tokenizer is None:
            self._tokenizer = sqlite3.connect(":memory:", check_same_thread=False, isolation_level=None)
            self._tokenizer.execute("CREATE VIRTUAL TABLE qtok USING fts5(x, tokenize='porter unicode61 "
                                    "remove_diacritics 2')")
            self._tokenizer.execute("CREATE VIRTUAL TABLE qtok_v USING fts5vocab(qtok, 'instance')")
        conn = self._tokenizer
        conn.execute("DELETE FROM qtok")
        conn.executemany("INSERT INTO qtok (rowid, x) VALUES (?, ?)", list(enumerate(tokens)))
        stems = dict(conn.execute("SELECT doc, term FROM qtok_v ORDER BY doc, offset DESC"))
        return [self._vocab.get(stems.get(i, t), 0) for i, t in enumerate(tokens)]

    def _exact(self, ident: str) -> list[int]:
        ident = ident.strip("./")
        rows = self.store.conn.execute(
            "SELECT id FROM nodes WHERE name = ? OR qualname = ? LIMIT 8", (ident, ident)).fetchall()
        if not rows and ("/" in ident or "." in ident):
            rows = self.store.conn.execute(
                "SELECT id FROM nodes WHERE kind = 'file' AND path LIKE ? LIMIT 4", (f"%{ident}",)).fetchall()
        return [self.idx[r[0]] for r in rows if r[0] in self.idx]

    def ppr(self, seeds: dict[int, float], iters: int = 20, restart: float | None = None,
            eps: float | None = None) -> tuple[np.ndarray, np.ndarray]:
        """Personalized PageRank as (node indices, mass). Small graphs iterate exactly over the whole graph;
        large ones propagate only entries of at least `eps` (a batched forward push), so the cost follows
        the walk's reach instead of the graph size (crossover measured at ~150k nodes)."""
        alpha = self.tuning["restart"] if restart is None else restart
        eps = self.tuning["ppr_eps"] if eps is None else eps
        n = len(self.ids)
        seed_idx = np.fromiter(seeds, dtype=np.int64, count=len(seeds))
        seed_w = np.fromiter(seeds.values(), dtype=np.float64, count=len(seeds))
        seed_w /= seed_w.sum() or 1.0
        if n < self.tuning["ppr_sparse_from"]:
            return self._ppr_exact(seed_idx, seed_w, iters, alpha)
        nodes, mass = seed_idx, seed_w
        for _ in range(iters):
            row = csr_matrix((mass, nodes, np.array([0, len(nodes)])), shape=(1, n))
            out = row @ self.transition
            back = alpha + (1 - alpha) * mass[self.dangling[nodes]].sum()
            idx, inverse = np.unique(np.concatenate([out.indices, seed_idx]), return_inverse=True)
            total = np.bincount(inverse, weights=np.concatenate([out.data * (1 - alpha), back * seed_w]))
            keep = total >= eps
            nodes, mass = idx[keep], total[keep]
        return nodes, mass

    def _ppr_exact(self, seed_idx: np.ndarray, seed_w: np.ndarray, iters: int,
                   alpha: float) -> tuple[np.ndarray, np.ndarray]:
        p = np.zeros(len(self.ids))
        p[seed_idx] = seed_w
        r = p.copy()
        backward = self.transition.T
        for _ in range(iters):
            nxt = (1 - alpha) * (backward @ r) + (alpha + (1 - alpha) * r[self.dangling].sum()) * p
            done = np.abs(nxt - r).sum() < 1e-7
            r = nxt
            if done:
                break
        nodes = np.flatnonzero(r)
        return nodes, r[nodes]

    def _hit(self, i: int, score: float, anchors: set[int], scores: dict[int, float]) -> Hit:
        return Hit(
            id=self.ids[i], kind=self.kind[i], name=self.name[i], path=self.path[i], start=self.start[i],
            end=self.end[i], score=round(score, 4), project=self.project[i], qualname=self.qual[i],
            signature=self.sig(i) if self.kind[i] != "file" else "", why=self._why(i, anchors, scores),
        )

    def _why(self, i: int, anchors: set[int], scores: dict[int, float]) -> str:
        if i in anchors:
            return "match"
        best, label = 0.0, ""
        for nbrs, kinds, ptr, arrow in ((self.out_nbr, self.out_kind, self.out_ptr, "->"),
                                        (self.in_nbr, self.in_kind, self.in_ptr, "<-")):
            for pos in range(ptr[i], min(ptr[i + 1], ptr[i] + 400)):
                j = int(nbrs[pos])
                s = scores.get(j, 0.0) + (1.0 if j in anchors else 0.0)
                if s > best and j != i:
                    best, label = s, f"{self.kind_names[kinds[pos]]} {arrow} {self._label(j)}"
        return label

    def _label(self, i: int) -> str:
        if self.kind[i] in ("route", "table", "tag", "external", "project"):
            return self.name[i]
        if self.kind[i] in SYMBOL_KINDS:
            return self.qual[i]
        if self.kind[i] == "section":
            return f"{self.path[i]} § {self.name[i]}"
        return self.path[i] or self.name[i]

    def _group_files(self, ranked: list[int], scores: dict[int, float], anchors: set[int], k: int) -> list[Hit]:
        groups: dict[str, list[int]] = {}
        order: list[str] = []
        for i in ranked:
            key = f"{self.project[i]}:{self.path[i]}" if self.path[i] else self.ids[i]
            if key not in groups:
                groups[key] = []
                order.append(key)
            groups[key].append(i)
        file_scores = {}
        for key, members in groups.items():
            vals = sorted((scores[i] for i in members), reverse=True)
            file_scores[key] = vals[0] + self.tuning["file_rest"] * sum(vals[1:4])
        out = []
        for key in sorted(order, key=file_scores.get, reverse=True)[:k]:
            members = groups[key]
            head = next((i for i in members if self.kind[i] == "file"), members[0])
            fid = self.idx.get(key, head)
            hit = self._hit(fid if self.path[fid] else head, file_scores[key], anchors, scores)
            hit.why = self._why(members[0], anchors, scores)
            hit.symbols = [
                {"name": (self.name[i] if self.kind[i] in ("section", "chunk") else self.qual[i] or self.name[i])[:80],
                 "kind": self.kind[i], "line": self.start[i]}
                for i in members if self.kind[i] != "file"
            ][:3]
            out.append(hit)
        return out

    # -- graph navigation ------------------------------------------------------------------
    def resolve(self, ref: str) -> int | None:
        ref = ref.strip()
        if ref in self.idx:
            return self.idx[ref]
        for project in self.roots:
            if f"{project}:{ref}" in self.idx:
                return self.idx[f"{project}:{ref}"]
        m = re.match(r"^(.*?):(\d+)$", ref)
        if m and not ref.startswith(tuple(p + ":" for p in self.roots)):
            path, line = m.group(1), int(m.group(2))
            fi = self._file(path)
            if fi is not None:
                return self._innermost(fi, line)
        fi = self._file(ref)
        if fi is not None:
            return fi
        rows = self.store.conn.execute(
            "SELECT id, kind FROM nodes WHERE name = ? OR qualname = ? LIMIT 50", (ref, ref)).fetchall()
        prio = {"class": 0, "interface": 1, "enum": 2, "type": 3, "function": 4, "method": 5, "route": 6}
        rows = [r for r in rows if r[0] in self.idx]
        if rows:
            rows.sort(key=lambda r: (prio.get(r[1], 9), len(r[0])))
            return self.idx[rows[0][0]]
        hits = self.search(ref, k=1, graph=False)
        return self.idx.get(hits[0].id) if hits else None

    def _file(self, path: str) -> int | None:
        path = path.strip().lstrip("./")
        for project in self.roots:
            i = self.idx.get(f"{project}:{path}")
            if i is not None and self.kind[i] == "file":
                return i
        if "/" in path or "." in path:
            row = self.store.conn.execute(
                "SELECT id FROM nodes WHERE kind = 'file' AND (path = ? OR path LIKE ?) ORDER BY length(path) LIMIT 1",
                (path, f"%/{path}")).fetchone()
            if row and row[0] in self.idx:
                return self.idx[row[0]]
        return None

    def _innermost(self, fi: int, line: int) -> int:
        best, best_span = fi, 10 ** 9
        stack = [fi]
        while stack:
            cur = stack.pop()
            for pos in range(self.out_ptr[cur], self.out_ptr[cur + 1]):
                if self.kind_names[self.out_kind[pos]] != "contains":
                    continue
                j = int(self.out_nbr[pos])
                if self.start[j] <= line <= self.end[j]:
                    span = self.end[j] - self.start[j]
                    if span < best_span:
                        best, best_span = j, span
                    stack.append(j)
        return best

    def node(self, ref: str, limit: int = 25) -> dict | None:
        i = self.resolve(ref)
        if i is None:
            return None
        info = self.store.get_node(self.ids[i])
        out: dict[str, list] = {}
        inc: dict[str, list] = {}
        for nbrs, kinds, ptr, bucket in ((self.out_nbr, self.out_kind, self.out_ptr, out),
                                         (self.in_nbr, self.in_kind, self.in_ptr, inc)):
            for pos in range(ptr[i], ptr[i + 1]):
                kind = self.kind_names[kinds[pos]]
                lst = bucket.setdefault(kind, [])
                if len(lst) < limit:
                    lst.append(self.brief(int(nbrs[pos])))
        return {**self.brief(i), "signature": info.signature, "text": info.text[:800], "out": out, "in": inc}

    def brief(self, i: int) -> dict:
        return {"id": self.ids[i], "kind": self.kind[i], "name": self._label(i), "path": self.path[i],
                "line": self.start[i], "end": self.end[i]}

    def neighbors(self, ref: str, kinds: list[str] | None = None, direction: str = "both", depth: int = 1,
                  limit: int = 60) -> dict | None:
        start = self.resolve(ref)
        if start is None:
            return None
        allowed = set(kinds) if kinds else None
        seen = {start: 0}
        edges = []
        frontier = [start]
        for d in range(1, depth + 1):
            nxt = []
            for i in frontier:
                for j, kind, arrow in self._adjacent(i, direction):
                    if allowed and kind not in allowed:
                        continue
                    if kind == "dir_contains" and not (allowed and "dir_contains" in allowed):
                        continue
                    edges.append((self.ids[i], kind, arrow, self.ids[j]))
                    if j not in seen:
                        seen[j] = d
                        nxt.append(j)
                    if len(seen) > limit:
                        break
            frontier = nxt
        nodes = [{**self.brief(j), "depth": dd} for j, dd in seen.items()]
        return {"root": self.ids[start], "nodes": nodes, "edges": edges[: limit * 3]}

    def _adjacent(self, i: int, direction: str):
        if direction in ("out", "both"):
            for pos in range(self.out_ptr[i], self.out_ptr[i + 1]):
                yield int(self.out_nbr[pos]), self.kind_names[self.out_kind[pos]], "->"
        if direction in ("in", "both"):
            for pos in range(self.in_ptr[i], self.in_ptr[i + 1]):
                yield int(self.in_nbr[pos]), self.kind_names[self.in_kind[pos]], "<-"

    def impact(self, ref: str, depth: int = 3, limit: int = 80) -> dict | None:
        """Who depends on this node (reverse dependency closure), incl. its own members and the tests."""
        start = self.resolve(ref)
        if start is None:
            return None
        seeds = {start}
        stack = [start]
        while stack:
            cur = stack.pop()
            for j, kind, _ in self._adjacent(cur, "out"):
                if kind == "contains" and j not in seeds:
                    seeds.add(j)
                    stack.append(j)
        seen = dict.fromkeys(seeds, (0, "", ""))
        queue = deque((i, 0) for i in seeds)
        while queue and len(seen) < limit + len(seeds):
            cur, d = queue.popleft()
            if d >= depth:
                continue
            for j, kind, _ in self._adjacent(cur, "in"):
                if kind not in DEPENDENCY_EDGES or kind in ("mentions", "links") and d > 0 or j in seen:
                    continue
                seen[j] = (d + 1, kind, self.ids[cur])
                queue.append((j, d + 1))
                for parent, pk, _ in self._adjacent(j, "in"):
                    if pk == "contains" and self.kind[parent] == "file" and parent not in seen:
                        seen[parent] = (d + 1, "contains", self.ids[j])
        affected = [
            {**self.brief(i), "depth": d, "via": kind, "from": src}
            for i, (d, kind, src) in sorted(seen.items(), key=lambda kv: kv[1][0]) if i not in seeds
        ]
        tests = sorted({a["path"] for a in affected if _looks_test(a["path"])})
        files = sorted({a["path"] for a in affected if a["path"]})
        return {"root": self.ids[start], "affected": affected[:limit], "files": files, "tests": tests}

    def path_between(self, a: str, b: str, max_expand: int = 200_000) -> dict | None:
        """Cheapest chain of relations: strong structural edges and low-degree nodes are preferred."""
        ia, ib = self.resolve(a), self.resolve(b)
        if ia is None or ib is None:
            return None
        skip = {"dir_contains", "tagged", "depends_on", "changed_for"}
        deg = np.diff(self.out_ptr) + np.diff(self.in_ptr)
        kind_cost = [1.0 / max(self.cfg.edge_weights.get(k, 0.5), 0.05) for k in self.kind_names]
        best = {ia: 0.0}
        prev: dict[int, tuple[int, str, str]] = {}
        heap = [(0.0, ia)]
        expanded = 0
        while heap and expanded < max_expand:
            cost, i = heapq.heappop(heap)
            if i == ib:
                break
            if cost > best.get(i, float("inf")):
                continue
            expanded += 1
            for nbrs, kinds, ptr, arrow in ((self.out_nbr, self.out_kind, self.out_ptr, "->"),
                                            (self.in_nbr, self.in_kind, self.in_ptr, "<-")):
                for pos in range(ptr[i], ptr[i + 1]):
                    kind = self.kind_names[kinds[pos]]
                    if kind in skip:
                        continue
                    j = int(nbrs[pos])
                    step = kind_cost[kinds[pos]] + 0.35 * float(np.log2(2 + deg[j]))
                    if cost + step < best.get(j, float("inf")):
                        best[j] = cost + step
                        prev[j] = (i, kind, arrow)
                        heapq.heappush(heap, (cost + step, j))
        if ib not in prev:
            return {"found": False, "from": self.ids[ia], "to": self.ids[ib]}
        chain = []
        cur = ib
        while cur != ia:
            p, kind, arrow = prev[cur]
            chain.append({"from": self.brief(p), "edge": kind, "dir": arrow, "to": self.brief(cur)})
            cur = p
        return {"found": True, "hops": list(reversed(chain))}

    def outline(self, path: str) -> dict | None:
        fi = self._file(path)
        if fi is None:
            return None
        items = []
        stack = [(fi, 0)]
        while stack:
            cur, depth = stack.pop()
            kids = [int(self.out_nbr[p]) for p in range(self.out_ptr[cur], self.out_ptr[cur + 1])
                    if self.kind_names[self.out_kind[p]] == "contains"]
            kids.sort(key=lambda j: self.start[j], reverse=True)
            for j in kids:
                items.append((self.start[j], depth, j))
                stack.append((j, depth + 1))
        items.sort()
        return {"file": self.brief(fi), "items": [
            {"line": self.start[j], "end": self.end[j], "depth": d, "kind": self.kind[j], "name": self.name[j],
             "signature": self.sig(j)} for _, d, j in items]}

    def snippet(self, ref: str, max_lines: int = 80, context: int = 0) -> dict | None:
        i = self.resolve(ref)
        if i is None or not self.path[i]:
            return None
        root = self.roots.get(self.project[i])
        if root is None:
            return None
        full = Path(root) / self.path[i]
        try:
            lines = full.read_text(encoding="utf-8", errors="replace").splitlines()
        except OSError:
            return None
        start = max(1, (self.start[i] or 1) - context)
        end = min(len(lines), max(self.end[i] or start, start) + context, start + max_lines - 1)
        return {**self.brief(i), "from": start, "to": end, "truncated": end < (self.end[i] or end),
                "code": "\n".join(lines[start - 1:end])}

    def subgraph(self, ids: list[str], expand: int = 0, limit: int = 250) -> dict:
        keep = {self.idx[i] for i in ids if i in self.idx}
        if expand:
            for i in list(keep):
                for j, kind, _ in self._adjacent(i, "both"):
                    if len(keep) >= limit:
                        break
                    if kind not in ("dir_contains",) and not self.hidden[j]:
                        keep.add(j)
        edges = []
        for i in keep:
            for pos in range(self.out_ptr[i], self.out_ptr[i + 1]):
                j = int(self.out_nbr[pos])
                if j in keep:
                    edges.append({"source": self.ids[i], "target": self.ids[j],
                                  "kind": self.kind_names[self.out_kind[pos]]})
        return {"nodes": [self.brief(i) for i in keep], "edges": edges}

    def stats(self) -> dict:
        return {**self.store.stats(), "embedder": self.store.get_meta("embedder"),
                "loaded_nodes": len(self.ids), "loaded_edges": int(self.out_ptr[-1]) if len(self.ids) else 0}


class _Column:
    """One text column of `nodes`, read by node index on demand and cached."""

    LIMIT = 200_000

    def __init__(self, store: Store, rowids: np.ndarray, column: str):
        self.store, self.rowids, self.column = store, rowids, column
        self.cache: dict[int, str] = {}

    def __len__(self) -> int:
        return len(self.rowids)

    def __getitem__(self, i: int) -> str:
        value = self.cache.get(i)
        if value is None:
            row = self.store.conn.execute(
                f"SELECT {self.column} FROM nodes WHERE rowid = ?", (int(self.rowids[i]),)).fetchone()
            value = (row[0] or "") if row else ""
            if len(self.cache) >= self.LIMIT:
                self.cache.clear()
            self.cache[i] = value
        return value

    def __iter__(self):
        """Every value in index order (one scan; used by exports)."""
        values = [""] * len(self.rowids)
        index = {int(r): i for i, r in enumerate(self.rowids)}
        for rowid, value in self.store.conn.execute(f"SELECT rowid, {self.column} FROM nodes"):
            i = index.get(rowid)
            if i is not None:
                values[i] = value or ""
        return iter(values)


class _IdIndex:
    """Node id -> node index through the unique id index in SQLite; file ids are answered from memory."""

    def __init__(self, store: Store, by_rowid: np.ndarray, files: dict[str, int]):
        self.store, self.by_rowid, self.files = store, by_rowid, files

    def get(self, node_id: str, default=None):
        i = self.files.get(node_id)
        if i is not None:
            return i
        row = self.store.conn.execute("SELECT rowid FROM nodes WHERE id = ?", (node_id,)).fetchone()
        if row is None or row[0] >= len(self.by_rowid) or self.by_rowid[row[0]] < 0:
            return default
        return int(self.by_rowid[row[0]])

    def __contains__(self, node_id: str) -> bool:
        return self.get(node_id) is not None

    def __getitem__(self, node_id: str) -> int:
        i = self.get(node_id)
        if i is None:
            raise KeyError(node_id)
        return i


def _looks_test(path: str) -> bool:
    from .linker import is_test_path

    return is_test_path(path)


def _csr(src: np.ndarray, dst: np.ndarray, kind: np.ndarray, n: int):
    order = np.argsort(src, kind="stable")
    ptr = np.zeros(n + 1, dtype=np.int64)
    np.add.at(ptr, src + 1, 1)
    return np.cumsum(ptr), dst[order], kind[order]
