"""Several repositories: the machine registry and federated search (CLI and MCP)."""

import asyncio
import json
import subprocess

import pytest

from repolens import cli, repos
from repolens.engine import Hit

from .conftest import write_repo

PROFILE = '[index]\ndb = ".repolens/graph.db"\n[[source]]\nname = "{name}"\npath = "."\n[embed]\nbackend = "lsa"\n'


def make(root, name, extra=None):
    write_repo(root)
    (root / "repolens.toml").write_text(PROFILE.format(name=name), encoding="utf-8")
    for rel, text in (extra or {}).items():
        (root / rel).parent.mkdir(parents=True, exist_ok=True)
        (root / rel).write_text(text, encoding="utf-8")
    git = ["git", "-C", str(root), "-c", "user.name=t", "-c", "user.email=t@example.com"]
    subprocess.run([*git, "add", "-A"], check=True)
    subprocess.run([*git, "commit", "-qm", "extra"], check=True)
    return root


@pytest.fixture
def two(tmp_path, monkeypatch):
    billing = make(tmp_path / "billing", "billing", {
        "billing/InvoiceLedger.py": ("def reconcile_invoice_ledger(ledger):\n"
                                     "    \"\"\"Reconcile the invoice ledger.\"\"\"\n    return ledger\n")})
    shop = make(tmp_path / "shop", "shop")
    for root in (billing, shop):
        monkeypatch.chdir(root)
        assert cli.main(["index", "--quiet"]) == 0
    return billing, shop


def test_every_index_run_registers_its_store(two, capsys):
    billing, shop = two
    assert cli.main(["repos", "--json"]) == 0
    listed = {r["name"]: r for r in json.loads(capsys.readouterr().out)}
    assert {"billing", "shop"} <= listed.keys()
    assert listed["billing"]["root"] == str(billing.resolve())
    assert listed["shop"]["head"] and listed["shop"]["db"].endswith(".repolens/graph.db")


def test_a_federated_search_merges_and_labels_repositories(two, capsys):
    _billing, shop = two
    assert cli.main(["search", "reconcile invoice ledger", "--files", "-k", "5"]) in (0, 1)
    assert "InvoiceLedger.py" not in capsys.readouterr().out  # this repository alone does not have it
    assert cli.main(["search", "reconcile invoice ledger", "--files", "-k", "5", "--repos", "all"]) == 0
    out = capsys.readouterr().out
    assert "[billing] billing/InvoiceLedger.py" in out
    assert cli.main(["search", "codes verify endpoint", "--files", "-k", "4", "--repos", "billing,shop"]) == 0
    out = capsys.readouterr().out
    assert "[billing]" in out and "[shop]" in out


def test_the_registry_drops_stores_that_no_longer_exist(two):
    billing, _shop = two
    (billing / ".repolens" / "graph.db").unlink()
    assert "billing" not in {r["name"] for r in repos.listed()}


def test_mcp_search_accepts_repositories(two):
    from repolens.mcp_server import build
    from repolens.service import KnowledgeGraph

    _billing, shop = two
    kg = KnowledgeGraph.open(shop / "repolens.toml")
    server = build(kg)

    async def call():
        result = await server.call_tool("search", {"query": "reconcile invoice ledger", "k": 3, "repos": "all"})
        return (result[0] if isinstance(result, tuple) else result)[0].text

    assert "[billing] billing/InvoiceLedger.py" in asyncio.run(call())
    kg.close()


def _hit(path):
    return Hit(id=path, kind="file", name=path, path=path, start=1, end=1, score=1.0)


def test_a_repository_that_matches_the_question_better_ranks_first():
    owner = ("billing", [_hit("a"), _hit("b")], {"score": 0.62, "space": "m:1"})
    other = ("shop", [_hit("x"), _hit("y")], {"score": 0.48, "space": "m:1"})
    assert [h.path for _n, h in repos.fuse([other, owner], 4)] == ["a", "b", "x", "y"]


def test_equally_relevant_repositories_interleave():
    one = ("billing", [_hit("a"), _hit("b")], {"score": 0.5, "space": "m:1"})
    two = ("shop", [_hit("x"), _hit("y")], {"score": 0.5, "space": "m:1"})
    assert [h.path for _n, h in repos.fuse([one, two], 4)] == ["a", "x", "b", "y"]


def test_relevance_from_different_embedding_spaces_is_not_compared():
    one = ("billing", [_hit("a"), _hit("b")], {"score": 0.9, "space": "lsa:1"})
    two = ("shop", [_hit("x"), _hit("y")], {"score": 0.1, "space": "lsa:2"})
    assert [h.path for _n, h in repos.fuse([two, one], 4)] == ["x", "a", "y", "b"]


def test_a_store_reports_its_relevance_and_embedding_space(kg):
    rel = kg.engine.relevance("verify a one-time code")
    assert 0.0 < rel["score"] <= 1.0 and rel["space"].startswith("lsa:")
    assert kg.engine.relevance("verify a one-time code") == rel


def test_the_service_answers_a_federated_request_with_hits_and_relevance(two, capsys):
    assert cli.main(["search", "reconcile invoice ledger", "--files", "--json", "--relevance"]) in (0, 1)
    reply = json.loads(capsys.readouterr().out)
    assert set(reply) == {"hits", "relevance"} and reply["relevance"]["space"].startswith("lsa:")


@pytest.mark.parametrize(("shop", "searched"), [(0.2, ["billing"]), (0.599, ["billing", "shop"])])
def test_a_repository_too_far_behind_to_place_a_hit_is_not_searched(monkeypatch, shop, searched):
    from types import SimpleNamespace

    entries = [{"name": "shop", "db": "/s"}, {"name": "billing", "db": "/b"}]
    relevance = {"billing": {"score": 0.6, "space": "m"}, "shop": {"score": shop, "space": "m"}}
    asked = []

    def ask(_kg, _current, entry, _query, k, _mode, _scope):
        if k:
            asked.append(entry["name"])
        return [_hit(f"{entry['name']}{i}") for i in range(k)], relevance[entry["name"]]

    monkeypatch.setattr(repos, "targets", lambda *_: entries)
    monkeypatch.setattr(repos, "_ask", ask)
    pairs = repos.search(SimpleNamespace(cfg=SimpleNamespace(db_path="/s")), "q", 3, "all")
    assert asked == searched
    assert [h.path for _n, h in pairs][0] == "billing0"
