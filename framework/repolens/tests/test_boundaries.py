"""RepoLens is its own system: its package, runners and launcher import no factory kit and name no adopter."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
# Assembled so this file does not itself contain the names it looks for.
ADOPTER = re.compile("|".join(["notai" + "re", "backend" + "-api", "workspace" + "/sdlc"]), re.IGNORECASE)
IMPORT_KIT = re.compile(r"^\s*(?:import|from)\s+aisdlc\b")
PARENT_ESCAPE = re.compile(r"\.parents\[\s*([3-9]|\d{2,})\s*\]")
CODE = ("repolens", "bench", "bin")
SKIP = ("bench/results", "bench/tasks", "bench/callgraph")


def _files() -> list[Path]:
    found = [ROOT / "pyproject.toml"]
    for folder in CODE:
        for path in sorted((ROOT / folder).rglob("*")):
            relative = path.relative_to(ROOT).as_posix()
            if not path.is_file() or "__pycache__" in path.parts or relative.startswith(SKIP):
                continue
            if path.suffix in {".py", ".sh", ".toml"} or path.parent.name == "bin":
                found.append(path)
    return found


def test_the_package_runners_and_launcher_stay_free_of_the_kit_and_of_adopters():
    hits = []
    for path in _files():
        for number, line in enumerate(path.read_text(encoding="utf-8", errors="replace").splitlines(), 1):
            if ADOPTER.search(line) or IMPORT_KIT.search(line) or PARENT_ESCAPE.search(line):
                hits.append(f"{path.relative_to(ROOT)}:{number}: {line.strip()}")
    assert hits == []


def test_repolens_does_not_live_inside_the_factory_kit():
    kit = ROOT.parent
    assert not (kit / "aisdlc").is_dir() and not (kit / "bin" / "aisdlc").exists(), "RepoLens sits beside the kit"


def test_benchmark_tasks_resolve_to_the_checkout_that_holds_this_module():
    """A task file that strips this module from its corpus must clone the repository this module lives in: after
    the module moved, the relative paths pointed one folder too high and the benchmark could not run."""
    import yaml

    for task_file in sorted((ROOT / "bench" / "tasks").glob("*.yaml")):
        corpus = yaml.safe_load(task_file.read_text(encoding="utf-8"))["corpus"]
        strip = corpus.get("strip")
        if not strip or "repo" not in corpus or not ROOT.as_posix().endswith("/" + strip):
            continue  # a prebuilt corpus, or this module copied out of the repository it measures
        expected = Path(ROOT.as_posix()[: -len(strip) - 1])
        assert (task_file.parent / corpus["repo"]).resolve() == expected, task_file.name
        assert (task_file.parent / corpus["profile"]).resolve().parent == expected, task_file.name
