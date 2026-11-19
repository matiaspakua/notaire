"""CLI, HTTP API and MCP server over the session fixture graph."""

import asyncio
import json

import pytest

from repolens import cli
from repolens.mcp_server import ALL_TOOLS, CORE_TOOLS, build

GW = "frontend/src/adapters/code-gateway.ts"


@pytest.fixture()
def cli_args(kg, tmp_path):
    cfg = tmp_path / "repolens.toml"
    src = kg.cfg.sources[0]
    cfg.write_text(f'[index]\ndb = "{kg.cfg.db_path}"\n[[source]]\nname = "{src.name}"\npath = "{src.path}"\n'
                   '[embed]\nbackend = "lsa"\n', encoding="utf-8")
    return ["-c", str(cfg)]


def test_cli_search_is_counted_by_the_usage_report(cli_args, capsys):
    from pathlib import Path

    from repolens.report import build

    cfg = Path(cli_args[1]).read_text(encoding="utf-8")
    db = next(line.split("=", 1)[1].strip().strip('"') for line in cfg.splitlines() if line.strip().startswith("db "))
    log = Path(db).parent / "usage.jsonl"
    log.write_text("", encoding="utf-8")
    assert cli.main([*cli_args, "search", "codes verify endpoint", "-k", "3"]) == 0
    capsys.readouterr()
    assert cli.main([*cli_args, "impact", "CodeStorePort"]) == 0
    capsys.readouterr()
    events = [json.loads(line) for line in log.read_text(encoding="utf-8").splitlines()]
    assert [event["tool"] for event in events] == ["search", "impact"]
    assert events[0]["src"] == "cli" and events[0]["results"] >= 1
    report = build(log)
    assert report["events"] == 2
    assert report["server"]["calls"] == 2
    assert report["server"]["clients"]["cli"] == 2


def test_cli_miss_records_a_grep_fallback(cli_args):
    from pathlib import Path

    from repolens.report import build

    cfg = Path(cli_args[1]).read_text(encoding="utf-8")
    db = next(line.split("=", 1)[1].strip().strip('"') for line in cfg.splitlines() if line.strip().startswith("db "))
    log = Path(db).parent / "usage.jsonl"
    assert cli.main([*cli_args, "miss", "exact symbol Folio.getDto"]) == 0
    events = [json.loads(line) for line in log.read_text(encoding="utf-8").splitlines()]
    assert events[-1]["tool"] == "miss" and events[-1]["results"] == 0
    assert events[-1]["input"] == "exact symbol Folio.getDto"
    assert build(log)["server"]["by_tool"]["miss"]["calls"] == 1


def test_cli_search_stats_and_json(cli_args, capsys):
    assert cli.main([*cli_args, "search", "codes verify endpoint", "--files", "-k", "5"]) == 0
    out = capsys.readouterr().out
    assert "CodeController.java" in out
    assert cli.main([*cli_args, "search", "codes verify endpoint", "--json", "-k", "3"]) == 0
    assert len(json.loads(capsys.readouterr().out)) == 3
    assert cli.main([*cli_args, "stats"]) == 0
    assert "project acme" in capsys.readouterr().out


def test_cli_navigation_commands(cli_args, capsys):
    for cmd in (["node", "CodeController.check"], ["impact", "CodeStorePort"], ["outline", GW],
                ["snippet", "codeGateway.verify"], ["path", GW, "CodeJpaEntity"],
                ["neighbors", "CodeVerificationService", "--depth", "2"]):
        assert cli.main([*cli_args, *cmd]) == 0, cmd
        assert capsys.readouterr().out.strip()
    assert cli.main([*cli_args, "node", "NoSuchThingAnywhere123"]) in (0, 1)


def test_cli_index_init_and_export(cli_args, tmp_path, capsys):
    assert cli.main([*cli_args, "index", "--json"]) == 0
    assert json.loads(capsys.readouterr().out)["sources"]["acme"]["extracted"] == 0
    out = tmp_path / "graph.graphml"
    assert cli.main([*cli_args, "export", "--format", "graphml", "-o", str(out)]) == 0
    assert out.read_text().startswith("<?xml")
    target = tmp_path / "new.toml"
    assert cli.main(["init", str(tmp_path), "-o", str(target)]) == 0
    assert "[[source]]" in target.read_text()
    assert cli.main(["init", str(tmp_path), "-o", str(target)]) == 2


def test_cli_reports_configuration_errors(tmp_path, capsys):
    assert cli.main(["--db", str(tmp_path / "empty.db"), "index"]) == 2
    assert "nothing to index" in capsys.readouterr().err


def test_http_api(kg):
    from fastapi.testclient import TestClient

    from repolens.api import create_app

    client = TestClient(create_app(kg))
    assert client.get("/").status_code == 200
    hits = client.get("/api/search", params={"q": "verify code", "k": 3}).json()
    assert len(hits) == 3 and {"id", "path", "score", "why"} <= hits[0].keys()
    node = client.get("/api/node", params={"ref": "CodeController.check"}).json()
    assert "handles" in node["in"]
    graph = client.get("/api/subgraph", params={"q": "codes endpoint", "k": 8}).json()
    assert graph["nodes"] and "hits" in graph
    assert client.get("/api/subgraph", params={"ref": "CodeController"}).json()["edges"]
    assert client.get("/api/impact", params={"ref": "CodeStorePort"}).json()["files"]
    assert client.get("/api/path", params={"a": GW, "b": "CodeJpaEntity"}).json()["found"]
    assert client.get("/api/outline", params={"path": GW}).json()["items"]
    assert "apiPost" in client.get("/api/snippet", params={"ref": "codeGateway"}).json()["code"]
    assert client.get("/api/node", params={"ref": "NoSuchThing_xyz/abc.java"}).status_code in (200, 404)
    assert client.get("/api/subgraph").status_code == 400
    assert client.get("/api/stats").json()["nodes"] > 0


def test_mcp_tools(kg):
    server = build(kg, ALL_TOOLS)

    async def call(name, args):
        result = await server.call_tool(name, args)
        content = result[0] if isinstance(result, tuple) else result
        return content[0].text

    async def scenario():
        names = {t.name for t in await server.list_tools()}
        assert names == set(ALL_TOOLS)
        assert "CodeController.java" in await call("search", {"query": "codes verify endpoint", "k": 5})
        assert "handles" in await call("node", {"ref": "CodeController.check"})
        assert "CodeVerificationServiceTest" in await call("impact", {"ref": "CodeStorePort"})
        assert "http_call" in await call("path", {"source": GW, "target": "CodeController.check"})
        assert "verify" in await call("outline", {"file_path": GW})
        assert "apiPost" in await call("snippet", {"ref": "codeGateway.verify"})
        assert "acme" in await call("stats", {})

    asyncio.run(scenario())


def test_default_search_keeps_four_detailed_hits(cli_args, capsys):
    assert cli.main([*cli_args, "search", "code verification"]) == 0
    out = capsys.readouterr().out
    head = out.split("also relevant:")[0]
    heads = [ln for ln in head.splitlines() if ln.split(". ", 1)[0].isdigit()]
    assert heads
    assert len(heads) <= 4
    assert "also relevant:" in out


def test_search_lists_a_compact_tail_after_the_detailed_results(kg, cli_args, capsys):
    from repolens import render

    hits = kg.engine.search("code verification", k=8, mode="files")
    text = render.hits(hits, "code verification", detailed=2)
    head, _, tail = text.partition("also relevant:")
    assert head.count("[file]") == 2 and "symbols:" in head
    tail_lines = [ln for ln in tail.splitlines() if ln.strip()]
    assert len(tail_lines) == len(hits) - 2 and all("symbols:" not in ln and "[file]" not in ln for ln in tail_lines)
    assert "also relevant:" not in render.hits(hits, "code verification")

    server = build(kg)
    result = asyncio.run(server.call_tool("search", {"query": "code verification", "k": 2}))
    content = result[0] if isinstance(result, tuple) else result
    assert "also relevant:" in content[0].text

    assert cli.main([*cli_args, "search", "code verification", "--files", "-k", "2", "--also", "3"]) == 0
    out = capsys.readouterr().out
    assert out.count("[file]") == 2 and "also relevant:" in out


def test_mcp_core_tool_set(kg):
    server = build(kg)
    names = {t.name for t in asyncio.run(server.list_tools())}
    assert names == set(CORE_TOOLS)
    with pytest.raises(ValueError):
        build(kg, ("search", "bogus"))
