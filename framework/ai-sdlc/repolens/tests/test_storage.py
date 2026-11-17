"""Store v2+ behaviours: compact layout, migration, vector reduction, no-op and full re-index paths."""

import os
import sqlite3

import numpy as np

from repolens import embed as embed_mod
from repolens.config import Config, Source
from repolens.embed import Projection, embed_text
from repolens.model import Node, text_hash
from repolens.service import KnowledgeGraph
from repolens.store import SCHEMA_VERSION, Store, parent_id, quantize

from .conftest import make_config, write_repo

LONG = "one-time codes are verified by the service " * 12


def test_quantize_round_trip_is_close():
    rng = np.random.default_rng(0)
    vecs = rng.standard_normal((50, 64)).astype(np.float32)
    vecs /= np.linalg.norm(vecs, axis=1, keepdims=True)
    q, scale = quantize(vecs)
    back = q.astype(np.float32) * scale[:, None]
    assert q.dtype == np.int8 and np.abs(back - vecs).max() < 0.01
    cos = np.sum(back * vecs, axis=1) / np.linalg.norm(back, axis=1)
    assert cos.min() > 0.999


def test_long_text_is_compressed_and_parents_are_relative(kg):
    conn = kg.store.conn
    kind, raw = conn.execute("SELECT typeof(text), text FROM nodes WHERE length(text) > 0 "
                             "ORDER BY length(text) DESC LIMIT 1").fetchone()
    assert kind in ("blob", "text")
    doc = kg.store.get_node("acme:docs/adr/ADR-007-one-time-codes.md")
    assert "ADR-007 One-time codes" in doc.text
    rel = dict(conn.execute("SELECT id, parent FROM nodes WHERE parent IS NOT NULL"))
    svc = "acme:backend/src/main/java/com/acme/service/CodeVerificationService.java"
    assert rel[f"{svc}#CodeVerificationService"] == ""
    assert rel[f"{svc}#CodeVerificationService.verify"] == "CodeVerificationService"
    assert parent_id("acme", "a/b.py", "") == "acme:a/b.py" and parent_id("acme", "a/b.py", "X") == "acme:a/b.py#X"


def test_compression_round_trip(tmp_path):
    store = Store(tmp_path / "c.db")
    from repolens.model import Extraction

    node = Node("p:f.md", "p", "file", "f.md", "f.md", "f.md", "md", 1, 9, "markdown", LONG)
    store.replace_files("p", [("h", 1, 1, Extraction("f.md", "md", [node]))])
    assert store.conn.execute("SELECT typeof(text) FROM nodes").fetchone()[0] == "blob"
    assert store.get_node("p:f.md").text == LONG
    assert store.fts('"verified"', 5)
    store.close()


def test_noop_index_does_nothing_and_touch_only_refreshes_stat(tmp_path):
    repo = write_repo(tmp_path / "r")
    kg = KnowledgeGraph(make_config(repo, tmp_path / "g.db"))
    kg.index(log=lambda *_: None)
    stamp = kg.store.get_meta("last_index")
    again = kg.index(log=lambda *_: None)
    assert again["changed"] is False and again["timings"]["link_s"] < 0.05
    assert kg.store.get_meta("last_index") == stamp
    target = repo / "tools/report.py"
    os.utime(target, ns=(1, 1))
    touched = kg.index(log=lambda *_: None)
    assert touched["sources"]["acme"]["extracted"] == 0 and touched["changed"] is False
    assert kg.store.file_signatures("acme")["tools/report.py"][2] == 1
    kg.close()


def test_full_reextract_keeps_other_projects_and_git_cache(tmp_path):
    a, b = write_repo(tmp_path / "a"), write_repo(tmp_path / "b")
    cfg = Config(db_path=tmp_path / "m.db", sources=[Source("a", a), Source("b", b)])
    cfg.embedder, cfg.workers = "lsa", 1
    kg = KnowledgeGraph(cfg)
    kg.index(log=lambda *_: None)
    before = kg.store.stats()
    assert kg.store.get_meta("git_cache")
    kg.index(only=["a"], full=True, log=lambda *_: None)
    after = kg.store.stats()
    assert after["nodes"] == before["nodes"] and after["edges"] == before["edges"]
    assert kg.engine.search("code verification service", k=5, project="b")
    kg.close()


def test_migrates_a_v1_store_and_keeps_its_vectors(tmp_path):
    db = tmp_path / "old.db"
    conn = sqlite3.connect(db)
    conn.executescript("""
        CREATE TABLE meta (key TEXT PRIMARY KEY, value BLOB);
        CREATE TABLE projects (name TEXT PRIMARY KEY, root TEXT NOT NULL, indexed_at REAL, files INTEGER);
        CREATE TABLE files (project TEXT, path TEXT, hash TEXT, size INTEGER, lang TEXT, meta TEXT);
        CREATE TABLE nodes (id TEXT PRIMARY KEY, project TEXT, kind TEXT, name TEXT, path TEXT, qualname TEXT,
            lang TEXT, start INTEGER, end_ INTEGER, signature TEXT, text TEXT, owner TEXT,
            tok_name TEXT, tok_path TEXT, tok_body TEXT);
        CREATE TABLE edges (src TEXT, dst TEXT, kind TEXT, weight REAL, project TEXT, owner TEXT);
        CREATE TABLE refs (project TEXT, owner TEXT, src TEXT, kind TEXT, target TEXT, extra TEXT);
        CREATE TABLE embeddings (id TEXT PRIMARY KEY, hash TEXT NOT NULL, vec BLOB NOT NULL);
        INSERT INTO meta VALUES ('schema_version', '1'), ('embedder', 'fastembed:test-model');
    """)
    node = Node("p:a.py#f", "p", "function", "f", "a.py", "f", "python", 1, 2, "def f()", "does f")
    conn.execute("INSERT INTO nodes VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                 (node.id, "p", node.kind, "f", "a.py", "f", "python", 1, 2, "def f()", "does f", "a.py", "", "", ""))
    vec = np.linspace(-1, 1, 16, dtype=np.float32)
    conn.execute("INSERT INTO embeddings VALUES (?,?,?)", (node.id, "x", vec.tobytes()))
    conn.commit()
    conn.close()

    store = Store(db)
    tables = {r[0] for r in store.conn.execute("SELECT name FROM sqlite_master WHERE type = 'table'")}
    assert "embeddings" not in tables and "refs" not in tables and "vectors" in tables
    assert store.get_meta("schema_version") == SCHEMA_VERSION and store.get_meta("embedder") == "fastembed:test-model"
    assert store.get_meta("extractor_version") == "migrated"
    hashes, vecs = next(store.vector_chunks())
    assert hashes == [text_hash(embed_text(node))]
    assert np.allclose(vecs[0], vec, atol=0.01)
    store.close()


class _FakeDense:
    """Deterministic dense embedder: a bag of hashed tokens (no model download)."""

    name = "fastembed:fake"
    dims = 48

    def needs_fit(self):
        return False

    def _one(self, text):
        vec = np.zeros(self.dims, dtype=np.float32)
        for tok in text.lower().split():
            vec[hash(tok) % self.dims] += 1.0
        return vec / (np.linalg.norm(vec) or 1.0)

    def embed_stream(self, texts, chunk=4096):
        for start in range(0, len(texts), chunk):
            yield np.vstack([self._one(t) for t in texts[start:start + chunk]])

    def embed_query(self, text):
        return self._one(text)


def test_dense_vectors_are_reduced_with_a_stored_projection(tmp_path, monkeypatch):
    fake = _FakeDense()
    monkeypatch.setattr(embed_mod, "make_embedder", lambda *a, **k: fake)
    monkeypatch.setattr(embed_mod, "MIN_PCA_VECTORS", 10)
    repo = write_repo(tmp_path / "r")
    cfg = make_config(repo, tmp_path / "g.db")
    cfg.embedder, cfg.embed_reduce = "fastembed", 8
    kg = KnowledgeGraph(cfg)
    report = kg.index(log=lambda *_: None)
    assert report["embed"]["dims"] == 8
    lengths = {r[0] for r in kg.store.conn.execute("SELECT length(q) FROM vectors")}
    assert lengths == {8} and kg.store.get_meta("pca_for") == fake.name
    assert kg.engine.emb.shape[1] == 8 and kg.engine.emb.dtype == np.float16
    assert isinstance(kg.engine.embedder, embed_mod.ProjectedEmbedder)
    assert kg.engine.search("verify one-time code service", k=5)
    (repo / "tools/new_module.py").write_text("def brand_new_helper():\n    return 42\n", encoding="utf-8")
    again = kg.index(log=lambda *_: None)
    assert again["embed"]["embedded"] >= 1 and {r[0] for r in kg.store.conn.execute(
        "SELECT length(q) FROM vectors")} == {8}
    kg.close()


def test_projection_round_trip():
    rng = np.random.default_rng(1)
    vecs = rng.standard_normal((200, 32)).astype(np.float32)
    proj = Projection.fit(vecs, 8)
    again = Projection.load(proj.dump())
    out = again.apply(vecs[:5])
    assert out.shape == (5, 8) and np.allclose(np.linalg.norm(out, axis=1), 1.0, atol=1e-5)
    assert np.allclose(out, proj.apply(vecs[:5]))


def test_common_query_terms_are_pruned_but_two_always_stay(kg):
    eng = kg.engine
    eng._vocab = {"servic": 9000, "control": 9000, "verif": 3, "gateway": 2}
    old = eng.tuning["max_df"]
    eng.tuning["max_df"] = 0.15
    try:
        kept = eng._query_terms("service controller verification gateway")
        assert kept == ["verification", "gateway"]
        assert eng._query_terms("service controller") == ["service", "controller"]
        eng.tuning["max_df"] = 0.0
        assert eng._query_terms("service controller verification") == ["service", "controller", "verification"]
    finally:
        eng.tuning["max_df"] = old
        eng._vocab = None


def test_relinking_keeps_unchanged_derived_nodes_and_drops_removed_ones(tmp_path):
    from repolens.linker import link

    repo = write_repo(tmp_path / "r")
    kg = KnowledgeGraph(make_config(repo, tmp_path / "g.db"))
    kg.index(log=lambda *_: None)
    store = kg.store

    def derived():
        return dict(store.conn.execute("SELECT id, rowid FROM nodes WHERE derived = 1"))

    def fts_rows():
        return store.conn.execute("SELECT COUNT(*) FROM fts").fetchone()[0]

    before, fts_before = derived(), fts_rows()
    changes = store.conn.total_changes
    link(store, kg.cfg)
    edges = store.conn.execute("SELECT COUNT(*) FROM edges").fetchone()[0]
    assert store.conn.total_changes - changes <= 2 * edges + 5  # edges rewritten, derived nodes untouched
    assert derived() == before and fts_rows() == fts_before
    route = next(nid for nid in before if ":route:" in nid)
    kept = [store.get_node(nid) for nid in before if nid != route]
    store.replace_derived(kept, [])
    assert route not in derived() and fts_rows() == fts_before - 1
    assert len(derived()) == len(before) - 1
    kg.close()
