"""Portable vectors (`repolens cache export/import`) and `[embed] skip`."""

import shutil
import sqlite3

import pytest

from repolens import cli
from repolens import embed as embed_mod
from repolens.service import KnowledgeGraph

from .conftest import make_config, write_repo
from .test_storage import _FakeDense


@pytest.fixture
def dense(monkeypatch):
    fake = _FakeDense()
    monkeypatch.setattr(embed_mod, "make_embedder", lambda *a, **k: fake)
    monkeypatch.setattr(embed_mod, "MIN_PCA_VECTORS", 10)
    return fake


def configured(repo, db):
    cfg = make_config(repo, db)
    cfg.embedder, cfg.embed_reduce, cfg.embed_model = "fastembed", 8, "fake"
    return cfg


def test_a_new_machine_imports_the_cache_and_embeds_nothing(tmp_path, monkeypatch, dense):
    first = write_repo(tmp_path / "one")
    built = KnowledgeGraph(configured(first, tmp_path / "one.db")).index(log=lambda *_: None)
    assert built["embed"]["embedded"] > 10
    exported = tmp_path / "vectors.db"
    assert cli.main(["cache", "export", str(exported), "--model", "fake"]) == 0
    monkeypatch.setenv("REPOLENS_VECTOR_CACHE", str(tmp_path / "machine-two"))
    assert cli.main(["cache", "import", str(exported)]) == 0
    second = tmp_path / "two"
    shutil.copytree(first, second)
    report = KnowledgeGraph(configured(second, tmp_path / "two.db")).index(log=lambda *_: None)
    assert report["embed"]["embedded"] == 0
    assert report["embed"]["from_cache"] == built["stats"]["embeddings"]


def test_import_refuses_a_cache_with_another_basis(tmp_path, monkeypatch, dense, capsys):
    repo = write_repo(tmp_path / "one")
    KnowledgeGraph(configured(repo, tmp_path / "one.db")).index(log=lambda *_: None)
    exported = tmp_path / "vectors.db"
    assert cli.main(["cache", "export", str(exported), "--model", "fake"]) == 0
    con = sqlite3.connect(exported)
    con.execute("UPDATE meta SET value = ? WHERE key = 'pca'", (b"another basis",))
    con.commit()
    con.close()
    assert cli.main(["cache", "import", str(exported)]) == 2
    assert "different projection" in capsys.readouterr().err


def test_skipped_paths_have_no_vector_but_keyword_search_finds_them(tmp_path, dense):
    repo = write_repo(tmp_path / "one")
    cfg = configured(repo, tmp_path / "one.db")
    cfg.embed_skip = ["docs/**"]
    kg = KnowledgeGraph(cfg)
    kg.index(log=lambda *_: None)
    embedded = {r[0] for r in kg.store.conn.execute(
        "SELECT DISTINCT path FROM nodes WHERE vhash IN (SELECT hash FROM vectors)")}
    assert embedded and not any(p.startswith("docs/") for p in embedded)
    assert any(h.path.startswith("docs/") for h in kg.engine.search("rotation out of scope", k=10, semantic=False))
    kg.close()


def test_cache_info_without_a_cache(tmp_path, capsys):
    assert cli.main(["cache", "info", "--model", "nothing-here"]) == 1
    assert "no vector cache" in capsys.readouterr().out
