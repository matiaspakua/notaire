"""An empty worktree index is bootstrapped or reported. It is not a silent miss."""

import subprocess

from repolens import cli
from repolens.bootstrap import EMPTY_INDEX


def _git(root, *args):
    subprocess.run(["git", "-C", str(root), *args], check=True, capture_output=True, text=True)


def _repo(path):
    path.mkdir()
    _git(path, "init", "-q", "-b", "main")
    _git(path, "config", "user.email", "t@example.com")
    _git(path, "config", "user.name", "t")
    (path / "app.py").write_text("def greet(name):\n    return name\n", encoding="utf-8")
    _git(path, "add", "app.py")
    _git(path, "commit", "-qm", "init")
    return path


def test_empty_worktree_reuses_the_common_index(tmp_path, monkeypatch, capsys):
    origin = _repo(tmp_path / "origin")
    monkeypatch.chdir(origin)
    assert cli.main(["index", "--embedder", "none", "--quiet"]) == 0
    worktree = tmp_path / "feature"
    _git(origin, "worktree", "add", "--detach", str(worktree), "HEAD")
    monkeypatch.chdir(worktree)
    code = cli.main(["search", "greet", "-k", "3"])
    captured = capsys.readouterr()
    assert code == 0
    assert "app.py" in captured.out
    assert "bootstrapped" in captured.err
    assert "no results" not in captured.out


def test_missing_index_without_a_donor_is_an_actionable_signal(tmp_path, monkeypatch, capsys):
    repo = _repo(tmp_path / "solo")
    spawned = []
    monkeypatch.setattr(cli, "_spawn_index", lambda *args, **kwargs: spawned.append(args))
    monkeypatch.chdir(repo)
    code = cli.main(["search", "greet", "-k", "3"])
    captured = capsys.readouterr()
    assert code == 2
    assert captured.out.strip() == EMPTY_INDEX
    assert "no results" not in captured.out
    assert spawned


def test_a_real_miss_on_a_built_index_stays_a_miss(tmp_path, monkeypatch, capsys):
    repo = _repo(tmp_path / "built")
    monkeypatch.chdir(repo)
    assert cli.main(["index", "--embedder", "none", "--quiet"]) == 0
    code = cli.main(["search", "zzzz_absent", "-k", "3"])
    captured = capsys.readouterr()
    assert code == 1
    assert "no results" in captured.out
    assert "no index" not in captured.out
