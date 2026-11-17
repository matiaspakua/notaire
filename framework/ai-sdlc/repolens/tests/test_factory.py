"""repolens inside an agent factory: zero config, many agents and worktrees, branch switches, usage evidence,
and harness wiring."""

import asyncio
import json
import shutil
import subprocess
import time

from repolens import cli
from repolens import embed as embed_mod
from repolens import setup as setup_mod
from repolens.config import load_config
from repolens.locks import index_lock
from repolens.report import build, gate_line
from repolens.service import KnowledgeGraph
from repolens.usage import hook_main

from .conftest import make_config, write_repo
from .test_storage import _FakeDense

GIT = ["-c", "user.name=t", "-c", "user.email=t@example.com"]


def git(root, *args):
    return subprocess.run(["git", "-C", str(root), *GIT, *args], check=True, capture_output=True, text=True).stdout


def wait_for(predicate, seconds=20.0):
    deadline = time.monotonic() + seconds
    while time.monotonic() < deadline:
        if predicate():
            return True
        time.sleep(0.05)
    return False


def test_zero_config_indexes_the_repository_and_finds_the_nearest_profile(tmp_path, monkeypatch):
    repo = write_repo(tmp_path / "acme")
    monkeypatch.chdir(repo / "backend")
    cfg = load_config()
    assert [s.path for s in cfg.sources] == [repo.resolve()]
    assert cfg.db_path == repo.resolve() / ".repolens" / "graph.db"
    (repo / "repolens.toml").write_text('[index]\ndb = "custom.db"\n[[source]]\nname = "x"\npath = "frontend"\n')
    cfg = load_config()
    assert cfg.db_path == repo.resolve() / "custom.db" and cfg.sources[0].path == (repo / "frontend").resolve()


def test_a_second_indexer_waits_or_steps_aside(tmp_path):
    repo = write_repo(tmp_path / "acme")
    kg = KnowledgeGraph(make_config(repo, tmp_path / "g.db"))
    with index_lock(tmp_path / "g.db") as held:
        assert held
        with index_lock(tmp_path / "g.db", wait=False) as second:
            assert second is False
        assert kg.index(log=lambda *_: None, wait=False) == {"busy": True, "changed": False, "sources": {}}
    assert kg.index(log=lambda *_: None, wait=False)["sources"]["acme"]["extracted"] > 0
    kg.close()


def test_a_new_worktree_reuses_every_vector_from_the_shared_cache(tmp_path, monkeypatch):
    fake = _FakeDense()
    monkeypatch.setattr(embed_mod, "make_embedder", lambda *a, **k: fake)
    monkeypatch.setattr(embed_mod, "MIN_PCA_VECTORS", 10)
    first = write_repo(tmp_path / "main")
    cfg = make_config(first, tmp_path / "main.db")
    cfg.embedder, cfg.embed_reduce = "fastembed", 8
    one = KnowledgeGraph(cfg).index(log=lambda *_: None)
    assert one["embed"]["embedded"] > 10 and one["embed"]["to_cache"] == one["stats"]["embeddings"]
    second = tmp_path / "worktree"
    shutil.copytree(first, second)
    cfg2 = make_config(second, tmp_path / "worktree.db")
    cfg2.embedder, cfg2.embed_reduce = "fastembed", 8
    kg2 = KnowledgeGraph(cfg2)
    two = kg2.index(log=lambda *_: None)
    assert two["embed"]["embedded"] == 0 and two["embed"]["from_cache"] == one["stats"]["embeddings"]
    assert kg2.store.get_meta("pca") == KnowledgeGraph(cfg).store.get_meta("pca")
    assert kg2.engine.emb is not None and kg2.engine.search("verify code", k=3)
    kg2.close()


def test_background_refresh_follows_the_agents_edits(tmp_path, monkeypatch):
    repo = write_repo(tmp_path / "acme")
    kg = KnowledgeGraph(make_config(repo, tmp_path / "g.db"))
    kg.index(log=lambda *_: None)
    assert not [h for h in kg.engine.search("brandNewHelper", k=3) if "brandNewHelper" in h.qualname]
    (repo / "tools" / "fresh.py").write_text("def brandNewHelper():\n    return 7\n", encoding="utf-8")
    monkeypatch.setenv("REPOLENS_REFRESH_SECONDS", "0")
    done = []
    assert kg.maybe_refresh(on_done=done.append)
    assert wait_for(lambda: done) and done[0]["changed"] and not done[0].get("error")
    assert any("brandNewHelper" in h.qualname for h in kg.engine.search("brandNewHelper", k=3))
    kg.close()


def test_switching_branches_reindexes_only_the_difference(tmp_path):
    repo = write_repo(tmp_path / "acme")
    kg = KnowledgeGraph(make_config(repo, tmp_path / "g.db"))
    kg.index(log=lambda *_: None)
    git(repo, "checkout", "-q", "-b", "feature")
    (repo / "tools" / "feature_only.py").write_text("def featureOnly():\n    return 1\n", encoding="utf-8")
    git(repo, "rm", "-q", "data/people.csv")
    git(repo, "add", "-A")
    git(repo, "commit", "-qm", "feature work")
    on_feature = kg.index(log=lambda *_: None)
    assert on_feature["sources"]["acme"]["extracted"] == 1 and on_feature["sources"]["acme"]["removed"] == 1
    assert kg.engine.outline("tools/feature_only.py") and kg.engine.outline("data/people.csv") is None
    git(repo, "checkout", "-q", "-")
    kg.index(log=lambda *_: None)
    assert kg.engine.outline("tools/feature_only.py") is None and kg.engine.outline("data/people.csv")
    kg.close()


def test_mcp_builds_on_first_start_and_records_every_call(tmp_path, monkeypatch):
    from repolens.mcp_server import BUILDING, build

    repo = write_repo(tmp_path / "acme")
    db = repo / ".repolens" / "graph.db"
    kg = KnowledgeGraph(make_config(repo, db))
    server = build(kg)

    async def search():
        result = await server.call_tool("search", {"query": "codes verify endpoint", "k": 3})
        return (result[0] if isinstance(result, tuple) else result)[0].text

    assert wait_for(lambda: not kg.refreshing, 60)
    text = asyncio.run(search())
    assert text != BUILDING and "CodeController.java" in text
    events = [json.loads(line) for line in (db.parent / "usage.jsonl").read_text().splitlines()]
    assert {"refresh", "search"} <= {e["tool"] for e in events}
    call = next(e for e in events if e["tool"] == "search")
    assert call["results"] > 0 and call["tokens"] > 0 and any("CodeController" in f for f in call["files"])
    kg.close()


def test_hook_and_report_measure_hit_rate_fallbacks_and_adoption(tmp_path, monkeypatch):
    (tmp_path / ".git").mkdir()
    (tmp_path / ".repolens").mkdir()
    absolute = str(tmp_path / "src" / "a.py")

    def call(session, tool, params, response=""):
        payload = {"session_id": session, "cwd": str(tmp_path), "tool_name": tool, "tool_input": params,
                   "tool_response": response}
        assert hook_main(stdin=_stdin(payload)) == 0

    call("s1", "mcp__repolens__search", {"query": "a"}, "1. [file] src/a.py:1-9  (0.9)\n2. [file] src/b.py:1")
    call("s1", "Read", {"file_path": absolute}, "x" * 400)
    call("s1", "Grep", {"pattern": "todo"}, "src/c.py")
    call("s1", "Read", {"file_path": "src/c.py"}, "y" * 40)
    call("s2", "Grep", {"pattern": "a"}, "z" * 4000)
    call("s2", "Read", {"file_path": "src/a.py"}, "x" * 400)
    call("s2", "Bash", {"command": "ls"}, "ignored")
    rep = build(tmp_path / ".repolens" / "usage.jsonl")
    s = rep["sessions"]
    assert s["sessions"] == 2 and s["adoption"] == 0.5 and s["first_tool_repolens"] == 0.5
    assert s["hit_rate"] == 0.5 and s["reads_after_repolens"] == 2 and s["fallback_rate"] == 1.0
    assert s["without_repolens"]["navigation_tokens"] > s["with_repolens"]["navigation_tokens"]
    assert rep["recommendations"] and "adoption 50%" in gate_line(rep)
    monkeypatch.chdir(tmp_path)
    assert cli.main(["report", "--gate"]) == 0


def test_setup_registers_every_harness_once_and_keeps_existing_entries(tmp_path, monkeypatch):
    repo = tmp_path / "repo"
    repo.mkdir()
    git(repo, "init", "-q")
    (repo / "repolens.toml").write_text('[harness]\nopencode = "tools/opencode.json"\n')
    (repo / ".mcp.json").write_text(json.dumps({"mcpServers": {"other": {"command": "x"}}}))
    (repo / ".claude").mkdir()
    (repo / ".claude" / "settings.json").write_text(json.dumps({"hooks": {"Stop": [{"hooks": []}]}}))
    (repo / ".codex").mkdir()
    (repo / ".codex" / "config.toml").write_text('model = "m"\n')
    monkeypatch.setattr(setup_mod, "MODULE_DIR", repo / "framework" / "repolens")
    first = setup_mod.setup(list(setup_mod.TARGETS), start=repo)
    assert all(first[t] for t in setup_mod.TARGETS), first
    assert setup_mod.setup(list(setup_mod.TARGETS), start=repo) == {t: [] for t in setup_mod.TARGETS}
    mcp = json.loads((repo / ".mcp.json").read_text())["mcpServers"]
    assert set(mcp) == {"other", "repolens"} and mcp["repolens"]["command"] == "bash"
    assert "git rev-parse --show-toplevel)/framework/repolens/bin/repolens" in mcp["repolens"]["args"][1]
    settings = json.loads((repo / ".claude" / "settings.json").read_text())
    assert settings["enabledMcpjsonServers"] == ["repolens"] and "Stop" in settings["hooks"]
    assert settings["hooks"]["PostToolUse"][0]["matcher"] == setup_mod.HOOK_MATCHER
    codex = (repo / ".codex" / "config.toml").read_text()
    assert codex.startswith('model = "m"') and codex.count("[mcp_servers.repolens]") == 1
    assert json.loads((repo / "tools" / "opencode.json").read_text())["mcp"]["repolens"]["enabled"] is True
    assert "refresh --background" in (repo / ".git" / "hooks" / "post-checkout").read_text()
    assert all(setup_mod._wiring(repo).values())


def test_refresh_never_creates_a_store(tmp_path, monkeypatch):
    repo = write_repo(tmp_path / "acme")
    monkeypatch.chdir(repo)
    assert cli.main(["refresh", "--background"]) == 0
    assert not (repo / ".repolens").exists()


def _stdin(payload):
    import io

    return io.StringIO(json.dumps(payload))


def _events(log, events):
    log.parent.mkdir(parents=True, exist_ok=True)
    now = time.time()
    with open(log, "w", encoding="utf-8") as fh:
        for i, event in enumerate(events):
            fh.write(json.dumps({"ts": now - 100 + i, **event}) + "\n")


def test_report_renders_every_format_and_raises_each_recommendation(tmp_path):
    from repolens.report import markdown, render, write

    log = tmp_path / ".repolens" / "usage.jsonl"
    server = [{"src": "server", "client": "claude-code", "tool": "search", "input": f"q{i}", "ms": 2500,
               "bytes": 4000, "tokens": 1000, "results": 0, "files": []} for i in range(5)]
    server.append({"src": "server", "tool": "refresh", "ms": 900, "error": "boom"})
    sessions = []
    for s in range(4):
        sessions += [{"src": "harness", "session": f"s{s}", "tool": "grep", "bytes": 4000}]
        if s == 0:
            for _ in range(10):
                sessions += [{"src": "harness", "session": "s0", "tool": "repolens:search", "files": ["a.py"],
                              "bytes": 400},
                             {"src": "harness", "session": "s0", "tool": "grep", "bytes": 100},
                             {"src": "harness", "session": "s0", "tool": "read", "files": ["z.py"], "bytes": 80}]
    _events(log, server + sessions)
    rep = build(log, "1d")
    levels = {r["finding"].split(" ")[0]: r["level"] for r in rep["recommendations"]}
    text = " ".join(r["finding"] for r in rep["recommendations"])
    assert "only 25%" in text and "hit rate 0%" in text and "followed by grep" in text
    assert "returned nothing" in text and "p95 latency" in text and "refreshes failed" in text
    assert "high" in levels.values()
    assert "agent sessions: 4" in render(rep) and markdown(rep).startswith("# repolens usage report")
    write(rep, tmp_path / "r.json", tmp_path / "r.md")
    assert json.loads((tmp_path / "r.json").read_text())["events"] == len(server) + len(sessions)
    assert build(log, "2026-01-01")["events"] == rep["events"] and build(tmp_path / "none.jsonl")["recommendations"]


def test_cli_setup_doctor_and_report_on_a_bare_repository(tmp_path, monkeypatch, capsys):
    repo = tmp_path / "repo"
    repo.mkdir()
    git(repo, "init", "-q")
    monkeypatch.chdir(repo)
    monkeypatch.setattr(setup_mod, "MODULE_DIR", repo / "tools" / "repolens")
    assert cli.main(["setup", "--dry-run"]) == 0
    assert "would update" in capsys.readouterr().out and not (repo / ".mcp.json").exists()
    assert cli.main(["setup", "--only", "claude", "--only", "gitignore"]) == 0
    assert (repo / ".mcp.json").exists()
    code = cli.main(["doctor"])
    out = capsys.readouterr().out
    assert code in (0, 1) and "claude: registered" in out and "codex: not registered" in out
    assert "no store yet" in out
    assert cli.main(["report"]) == 0 and "no usage recorded yet" in capsys.readouterr().out
    assert cli.main(["report", "--json"]) == 0 and json.loads(capsys.readouterr().out)["events"] == 0


def test_doctor_reads_a_live_store_and_the_usage_log(tmp_path, monkeypatch):
    repo = write_repo(tmp_path / "acme")
    (repo / "repolens.toml").write_text('[index]\ndb = ".repolens/graph.db"\n[[source]]\nname = "acme"\n'
                                        'path = "."\n[embed]\nbackend = "lsa"\n', encoding="utf-8")
    monkeypatch.chdir(repo)
    assert cli.main(["index", "--quiet"]) == 0
    (repo / ".repolens" / "usage.jsonl").write_text("")
    code, lines = setup_mod.doctor(repo)
    text = "\n".join(lines)
    assert "store " in text and "files" in text and "usage log" in text and "config " in text
    assert cli.main(["refresh"]) == 0
