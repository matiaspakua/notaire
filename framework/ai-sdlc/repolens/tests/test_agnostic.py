"""Any language, any format: definitions through tags queries, and the paths and identifiers any text names."""

import subprocess

import pytest

from repolens import orphans
from repolens.extractors import code as code_extractor
from repolens.scanner import detect
from repolens.service import KnowledgeGraph

from .conftest import make_config

FILES = {
    # Ruby has a tags query but no hand-written spec; the generic walker found nothing in it
    "lib/greeter.rb": "class GreeterService\n  def greet(name)\n    shout_aloud(name)\n  end\nend\n\n"
                      "def shout_aloud(text)\n  text.upcase\nend\n",
    "app/main.rb": 'require_relative "../lib/greeter"\n\ndef run\n  shout_aloud("hi")\nend\n',
    "scripts/deploy.sh": "deploy() {\n  echo deploying\n}\n",
    "scripts/run.sh": "#!/bin/sh\nsource ./lib/common.sh\n",
    "scripts/lib/common.sh": "log() {\n  echo \"$1\"\n}\n",
    ".claude/skills/review/SKILL.md": "# Review\n\nReview a change.\n",
    "catalog/skills.json": '{"id": "review", "ref": ".claude/skills/review"}\n',
    "docker-compose.yml": "services:\n  app:\n    image: app\n",
    "deploy/notes.yaml": "compose: docker-compose.yml\nhandler: InvoiceLedger\n",
    "lib/billing.py": "class InvoiceLedger:\n    pass\n",
    "web/a/index.ts": 'export const logo = "/icons/logo.png";\n',
    "web/b/index.ts": "export const b = 2;\n",
    "web/c/index.ts": "export const c = 3;\n",
    "docs/index.ts": "export const d = 4;\n",
    "docs/guide.md": "# Guide\n\nStart from index.ts.\n",
    "notes/todo.txt": "Check index.ts somewhere.\n",
    "public/icons/logo.png": b"\x89PNG\r\n\x1a\n" + bytes(32),
    # ESM TypeScript imports name the emitted `.js` file (vscode, NodeNext projects)
    "esm/app.ts": 'import { formatInvoice } from "./format.js";\n\nexport const total = formatInvoice(1);\n',
    "esm/format.ts": "export function formatInvoice(n: number) {\n  return n;\n}\n",
}


@pytest.fixture(scope="module")
def graph(tmp_path_factory):
    root = tmp_path_factory.mktemp("agnostic")
    for rel, content in FILES.items():
        path = root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        (path.write_bytes if isinstance(content, bytes) else path.write_text)(content)
    git = ["git", "-C", str(root), "-c", "user.name=t", "-c", "user.email=t@example.com"]
    subprocess.run([*git[:3], "init", "-q"], check=True)
    subprocess.run([*git, "add", "-A"], check=True)
    subprocess.run([*git, "commit", "-qm", "init"], check=True)
    kg = KnowledgeGraph(make_config(root, tmp_path_factory.mktemp("db") / "graph.db"))
    kg.index(log=lambda *_: None)
    yield kg
    kg.close()


def links(kg, kind="mentions"):
    """(source path, target path or dir/) pairs of one edge kind."""
    rows = kg.store.conn.execute(
        "SELECT a.path, b.path, b.kind FROM edges e JOIN nodes a ON a.rowid = e.src JOIN nodes b ON b.rowid = e.dst "
        "JOIN edge_kinds k ON k.code = e.kind WHERE k.name = ?", (kind,)).fetchall()
    return {(a, b + "/" if k == "dir" else b) for a, b, k in rows}


def test_detection_reaches_languages_without_a_hand_written_spec():
    assert detect("lib/greeter.dart") == ("dart", "code")
    assert detect("lib/server.ex") == ("elixir", "code")
    assert detect("infra/main.tf") == ("terraform", "text")
    assert detect("build.gradle") == ("groovy", "text")
    assert detect("src/x.java") == ("java", "code")
    assert detect("notes.unknownext") == ("unknownext", "text")


def test_a_tags_query_yields_definitions_and_calls(graph):
    names = {(r[0], r[1]) for r in graph.store.conn.execute(
        "SELECT kind, qualname FROM nodes WHERE path = 'lib/greeter.rb' AND kind != 'file'")}
    assert {("class", "GreeterService"), ("method", "GreeterService.greet"), ("function", "shout_aloud")} <= names
    assert ("app/main.rb", "lib/greeter.rb") in links(graph, "calls")


def test_a_grammar_without_a_tags_query_still_yields_functions(graph):
    kinds = {r[0] for r in graph.store.conn.execute("SELECT qualname FROM nodes WHERE path = 'scripts/deploy.sh'")}
    assert "deploy" in kinds


def test_paths_in_any_form_are_linked(graph):
    found = links(graph)
    assert ("catalog/skills.json", ".claude/skills/review/") in found  # a directory
    assert ("scripts/run.sh", "scripts/lib/common.sh") in found  # ./ relative
    assert ("deploy/notes.yaml", "docker-compose.yml") in found  # a bare file name
    assert ("web/a/index.ts", "public/icons/logo.png") in found  # a web-root path in a code string
    assert ("app/main.rb", "lib/greeter.rb") in found  # ../ relative, extension of the mentioning file


def test_an_ambiguous_bare_name_links_the_nearest_or_nothing(graph):
    found = links(graph)
    assert {b for a, b in found if a == "docs/guide.md" and b.endswith("index.ts")} == {"docs/index.ts"}
    assert not {b for a, b in found if a == "notes/todo.txt"}  # four index.ts, none nearer


def test_identifiers_in_configuration_link_the_symbol(graph):
    rows = graph.store.conn.execute(
        "SELECT b.qualname FROM edges e JOIN nodes a ON a.rowid = e.src JOIN nodes b ON b.rowid = e.dst "
        "JOIN edge_kinds k ON k.code = e.kind WHERE k.name = 'mentions' AND a.path = 'deploy/notes.yaml'").fetchall()
    assert ("InvoiceLedger",) in rows


def test_a_named_directory_relates_its_files(graph):
    isolated = {e["path"] for e in orphans.find(graph.engine)["isolated"]}
    assert not isolated & {"catalog/skills.json", ".claude/skills/review/SKILL.md", "public/icons/logo.png"}


def test_an_unavailable_grammar_falls_back_to_text_once(tmp_path, monkeypatch):
    calls = []

    def unavailable(lang):
        calls.append(lang)
        raise RuntimeError("offline")

    monkeypatch.setattr("tree_sitter_language_pack.get_parser", unavailable)
    monkeypatch.setattr(code_extractor, "_PARSERS", {})
    repo = tmp_path / "repo"
    repo.mkdir()
    for name in ("a.rb", "b.rb"):
        (repo / name).write_text("def one\n  1\nend\n", encoding="utf-8")
    subprocess.run(["git", "-C", str(repo), "init", "-q"], check=True)
    kg = KnowledgeGraph(make_config(repo, tmp_path / "graph.db"))
    kg.cfg.workers = 1
    kg.index(log=lambda *_: None)
    rows = kg.store.conn.execute("SELECT lang, signature FROM nodes WHERE kind = 'file'").fetchall()
    assert set(rows) == {("ruby", "text")}
    assert calls == ["ruby"]
    kg.close()


def test_an_esm_import_of_the_js_file_resolves_to_the_ts_source(graph):
    assert ("esm/app.ts", "esm/format.ts") in links(graph, "imports")
    assert ("esm/app.ts", "esm/format.ts") in links(graph, "calls")
