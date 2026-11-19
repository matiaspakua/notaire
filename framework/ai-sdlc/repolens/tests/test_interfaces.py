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


def test_impact_lists_each_file_once_with_its_symbols():
    from repolens import render

    def item(kind, name, path, line, via, demoted=False):
        return {"kind": kind, "name": name, "path": path, "line": line, "depth": 1, "via": via, "demoted": demoted}

    result = {"root": "p:src/A.java#A", "files": ["docs/adr.md", "docs/old/adr.md", "src/test/ATest.java",
                                                  "src/test/BTest.java"],
              "tests": ["src/test/ATest.java", "src/test/BTest.java"], "affected": [
                  item("route", "GET /a", "", 0, "handles"),
                  item("method", "ATest.one", "src/test/ATest.java", 5, "calls"),
                  item("method", "ATest.Nested.two", "src/test/ATest.java", 9, "calls"),
                  item("file", "ATest.java", "src/test/ATest.java", 1, "imports"),
                  item("section", "docs/adr.md § Decisions", "docs/adr.md", 12, "mentions"),
                  item("section", "docs/old/adr.md § Decisions", "docs/old/adr.md", 12, "mentions", demoted=True)]}
    text = render.impact(result)
    assert text.count("src/test/ATest.java") == 1
    assert "  d1 src/test/ATest.java [test]: calls one:5, Nested.two:9; imports" in text
    assert "  d1 handles: GET /a" in text
    assert "  d1 docs/adr.md: mentions § Decisions:12" in text
    assert "docs/old/adr.md" not in text and "+1 in demoted paths" in text
    assert text.endswith("tests not listed above: src/test/BTest.java")


def test_impact_marks_what_lies_in_demoted_paths(repo, tmp_path):
    from repolens.service import KnowledgeGraph

    from .conftest import make_config

    cfg = make_config(repo, tmp_path / "graph.db")
    cfg.demote = ["docs/**"]
    graph = KnowledgeGraph(cfg)
    graph.index(log=lambda *_: None)
    affected = graph.engine.impact("CodeController")["affected"]
    assert {a["path"] for a in affected if a["demoted"]} == {"docs/adr/ADR-007-one-time-codes.md"}
    graph.close()


def test_navigation_misses_name_the_closest_symbols(kg, cli_args, capsys):
    assert cli.main([*cli_args, "impact", "CodeService"]) == 1
    assert "not found: 'CodeService'" in capsys.readouterr().out
    server = build(kg)

    async def call():
        result = await server.call_tool("impact", {"ref": "CodeService"})
        return (result[0] if isinstance(result, tuple) else result)[0].text

    text = asyncio.run(call())
    assert text.startswith("not found: 'CodeService'") and "CodeVerificationService" in text


def test_node_lists_related_symbols_by_file_without_repeating_paths():
    from repolens import render

    def item(kind, name, path, line, demoted=False):
        return {"id": f"p:{path}#{name}", "kind": kind, "name": name, "path": path, "line": line, "end": 0,
                "demoted": demoted}

    home = "src/A.java"
    info = {**item("class", "A", home, 3), "end": 40, "signature": "class A", "text": "",
            "out": {"contains": [item("method", "A.one", home, 5), item("method", "A.two", home, 9)],
                    "calls": [item("route", "GET /b", "", 0)]},
            "in": {"contains": [item("file", home, home, 1)],
                   "references": [item("class", "B", "src/B.java", 2), item("method", "B.use", "src/B.java", 7),
                                  item("file", "src/C.java", "src/C.java", 1)],
                   "mentions": [item("section", "docs/a.md § Usage", "docs/a.md", 4),
                                item("section", "docs/old/a.md § Notes", "docs/old/a.md", 4, demoted=True)]}}
    text = render.node(info)
    assert "-> contains: one:5, two:9" in text and "-> calls: route:GET /b" in text
    assert "<- contains" not in text
    assert "<- references: src/B.java (B:2, use:7), src/C.java" in text
    assert "<- mentions: docs/a.md (§ Usage:4), +1 in demoted paths" in text


SCHEMA_BUDGET = 4000  # bytes of tools/list + instructions per session: 4,701 with output schemas, 3,803 without


def test_tool_schemas_stay_within_the_byte_budget(kg):
    server = build(kg)
    tools = asyncio.run(server.list_tools())
    assert all(t.outputSchema is None for t in tools)
    listing = json.dumps([t.model_dump(exclude_none=True) for t in tools])
    assert len(listing) + len(server.instructions) <= SCHEMA_BUDGET


def test_a_tool_answer_is_sent_once(kg):
    result = asyncio.run(build(kg).call_tool("node", {"ref": "CodeController"}))
    assert not isinstance(result, tuple)  # no structured copy of the same text
    assert "CodeController" in result[0].text
