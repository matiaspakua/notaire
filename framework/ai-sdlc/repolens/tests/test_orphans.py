"""Files and symbols that relate to nothing: the orphan report on every interface, and the index alert."""

import asyncio
import subprocess

import pytest

from repolens import cli, orphans
from repolens.service import KnowledgeGraph

PNG = b"\x89PNG\r\n\x1a\n" + bytes(range(64))
FILES = {
    "src/app.py": "from src.util import helper\n\n\ndef main():\n    return helper()\n",
    "src/util.py": "def helper():\n    return 1\n\n\ndef unused_helper():\n    return 2\n",
    "src/legacy.py": "def forgotten():\n    return 3\n",
    "assets/logo.png": PNG,
    "assets/icon.png": PNG,
    "README.md": "# App\n\nRun `src/app.py`. The icon is assets/icon.png.\n",
    "notes/a.txt": "alpha\n",
    "notes/b.txt": "beta\n",
    "archive/notes.old": "Old notes.\n",
}


def build_repo(root):
    for rel, content in FILES.items():
        path = root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        (path.write_bytes if isinstance(content, bytes) else path.write_text)(content)
    git = ["git", "-C", str(root), "-c", "user.name=t", "-c", "user.email=t@example.com"]
    subprocess.run([*git[:3], "init", "-q"], check=True)
    subprocess.run([*git, "add", "-A"], check=True)
    subprocess.run([*git, "commit", "-qm", "init"], check=True)
    for i in range(2):  # the two notes change together twice: a co-change, their only relation
        for rel in ("notes/a.txt", "notes/b.txt"):
            with open(root / rel, "a", encoding="utf-8") as fh:
                fh.write(f"edit {i}\n")
        subprocess.run([*git, "commit", "-qam", f"notes {i}"], check=True)
    return root


PROFILE = """[index]
db = ".repolens/graph.db"
[[source]]
name = "app"
path = "."
[embed]
backend = "lsa"
[git]
min_count = 2
[ranking]
demote = ["**/*.old"]  # a glob that names no directory: the profile would otherwise link it
"""


@pytest.fixture(scope="module")
def graph(tmp_path_factory):
    root = build_repo(tmp_path_factory.mktemp("orphans"))
    (root / "repolens.toml").write_text(PROFILE, encoding="utf-8")  # untracked: indexed, but no history
    kg = KnowledgeGraph.open(root / "repolens.toml")
    kg.index(log=lambda *_: None)
    yield kg
    kg.close()


def paths(entries):
    return {e["path"] for e in entries}


def test_files_related_to_nothing_and_only_through_history(graph):
    found = orphans.find(graph.engine)
    # repolens.toml too: the tool reads it, but no file of the repository names it
    assert paths(found["isolated"]) == {"src/legacy.py", "assets/logo.png", "repolens.toml"}
    assert paths(found["history_only"]) == {"notes/a.txt", "notes/b.txt"}
    assert found["demoted"] == {"isolated": 1, "history_only": 0}
    assert found["files"] == len(FILES) + 1  # and repolens.toml, which names `.repolens/graph.db`


def test_demoted_paths_are_listed_on_request(graph):
    assert "archive/notes.old" in paths(orphans.find(graph.engine, include_demoted=True)["isolated"])


def test_symbols_with_no_relation_on_request(graph):
    found = orphans.find(graph.engine, symbols=True)
    names = {s["name"] for s in found["symbols"]}
    assert {"unused_helper", "forgotten"} <= names
    assert not names & {"helper", "main"}


def test_the_cli_lists_orphans_and_the_index_run_alerts(graph, capsys, monkeypatch):
    monkeypatch.chdir(graph.cfg.sources[0].path)
    assert cli.main(["orphans"]) == 0
    out = capsys.readouterr().out
    assert "src/legacy.py" in out and "notes/a.txt" in out and "+1 in demoted paths" in out
    assert cli.main(["index"]) == 0
    assert "orphans: 3 files relate to no other file, 2 only through git history (repolens orphans)" in \
        capsys.readouterr().out


def test_http_and_mcp_return_the_same_files(graph):
    from fastapi.testclient import TestClient

    from repolens.api import create_app
    from repolens.mcp_server import build

    client = TestClient(create_app(graph))
    body = client.get("/api/orphans").json()
    assert paths(body["isolated"]) == {"src/legacy.py", "assets/logo.png", "repolens.toml"}
    with_symbols = client.get("/api/orphans", params={"symbols": True}).json()
    assert "unused_helper" in {s["name"] for s in with_symbols["symbols"]}
    assert "/api/orphans" in client.get("/").text  # the explorer's Orphans view

    async def call():
        result = await build(graph).call_tool("orphans", {})
        return (result[0] if isinstance(result, tuple) else result)[0].text

    text = asyncio.run(call())
    assert "src/legacy.py" in text and "assets/logo.png" in text
