"""Index pipeline paths the session fixture does not take: process pool, plain folders, no embedder, CLI paths."""

import json

from repolens import cli
from repolens.config import Config, Source
from repolens.embed import make_embedder
from repolens.scanner import list_files, scan
from repolens.service import KnowledgeGraph


def _plain_folder(root, n=70):
    for i in range(n):
        (root / "pkg").mkdir(parents=True, exist_ok=True)
        (root / "pkg" / f"mod_{i}.py").write_text(
            f"from pkg.mod_{(i + 1) % n} import f{(i + 1) % n}\n\n\ndef f{i}():\n    return f{(i + 1) % n}()\n",
            encoding="utf-8")
    (root / "node_modules" / "x").mkdir(parents=True)
    (root / "node_modules" / "x" / "index.js").write_text("module.exports = 1\n", encoding="utf-8")
    (root / "big.txt").write_text("x" * 5000, encoding="utf-8")
    return root


def test_parallel_extraction_without_git_and_without_embeddings(tmp_path):
    root = _plain_folder(tmp_path / "plain")
    cfg = Config(db_path=tmp_path / "g.db", sources=[Source("plain", root)])
    cfg.embedder, cfg.workers, cfg.max_file_bytes = "none", 2, 1000
    files = list_files(cfg.sources[0], cfg)
    assert "node_modules/x/index.js" not in files and "pkg/mod_0.py" in files
    assert {f.family for f in scan(cfg.sources[0], cfg) if f.path == "big.txt"} == {"binary"}
    kg = KnowledgeGraph(cfg)
    report = kg.index(log=lambda *_: None)
    assert report["sources"]["plain"]["extracted"] == 71 and report["embed"]["embedder"] == "none"
    edges = {(s.split(":", 1)[1], d.split(":", 1)[1], k) for s, d, k, _ in kg.store.edges()}
    assert ("pkg/mod_0.py", "pkg/mod_1.py", "imports") in edges
    assert ("pkg/mod_0.py#f0", "pkg/mod_1.py#f1", "calls") in edges
    hits = kg.engine.search("f5", k=3)
    assert hits and kg.engine.stats()["embedder"] == "none"
    kg.close()


def test_make_embedder_none_and_lsa():
    assert make_embedder("none", "m", 8) is None
    assert make_embedder("lsa", "m", 8).name == "lsa:8"


def test_cli_index_ad_hoc_paths_then_reuse_store(tmp_path, capsys):
    root = _plain_folder(tmp_path / "adhoc", n=3)
    db = tmp_path / "adhoc.db"
    assert cli.main(["--db", str(db), "index", str(root), "--name", "adhoc", "--embedder", "lsa", "--json"]) == 0
    assert json.loads(capsys.readouterr().out)["sources"]["adhoc"]["files"] == 4
    assert cli.main(["--db", str(db), "index", "--no-embed"]) == 0
    assert "adhoc: 4 files" in capsys.readouterr().out
    assert cli.main(["--db", str(db), "search", "f1", "--no-graph", "-k", "2"]) == 0
    assert cli.main(["--db", str(db), "search", "qqqqzzzz", "--kind", "route"]) == 1


def test_dropping_a_source_removes_its_project(tmp_path):
    a, b = _plain_folder(tmp_path / "a", n=2), _plain_folder(tmp_path / "b", n=2)
    cfg = Config(db_path=tmp_path / "m.db", sources=[Source("a", a), Source("b", b)])
    cfg.embedder, cfg.workers = "none", 1
    kg = KnowledgeGraph(cfg)
    kg.index(log=lambda *_: None)
    kg.cfg.sources = [Source("a", a)]
    kg.index(log=lambda *_: None)
    assert [p["name"] for p in kg.store.projects()] == ["a"]
    kg.close()


def test_graph_is_published_before_embedding_starts(tmp_path, monkeypatch):
    from repolens import indexer
    from repolens.engine import Engine
    from repolens.store import Store

    root = _plain_folder(tmp_path / "plain", n=5)
    cfg = Config(db_path=tmp_path / "g.db", sources=[Source("plain", root)])
    cfg.embedder, cfg.workers = "none", 1
    seen = {}

    def embed_store(store, cfg, log):
        reader = Store(cfg.db_path, readonly=True)
        seen["published"] = reader.get_meta("last_index")
        seen["hits"] = [h.path for h in Engine(reader, cfg).search("f3", k=3)]
        reader.close()
        return {"embedder": "none", "embedded": 0}

    monkeypatch.setattr(indexer, "embed_store", embed_store)
    KnowledgeGraph(cfg).index(log=lambda *_: None)
    assert seen["published"] and "pkg/mod_3.py" in seen["hits"]
