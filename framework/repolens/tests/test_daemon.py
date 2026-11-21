"""The resident per-worktree service behind the CLI (repolens/daemon.py)."""

import subprocess
import time

import pytest

from repolens import cli, daemon

from .conftest import write_repo

QUERY = ["search", "codes verify endpoint", "--files", "-k", "3"]


def wait_for(predicate, seconds=40.0):
    deadline = time.monotonic() + seconds
    while time.monotonic() < deadline:
        if predicate():
            return True
        time.sleep(0.1)
    return False


@pytest.fixture
def repo(tmp_path, monkeypatch):
    root = write_repo(tmp_path / "acme")
    (root / "repolens.toml").write_text('[index]\ndb = ".repolens/graph.db"\n[[source]]\nname = "acme"\n'
                                        'path = "."\n[embed]\nbackend = "lsa"\n', encoding="utf-8")
    monkeypatch.chdir(root)
    monkeypatch.setenv("REPOLENS_DAEMON", "1")
    monkeypatch.setenv("REPOLENS_DAEMON_IDLE", "60")
    assert cli.main(["index", "--quiet"]) == 0
    state = root / ".repolens"
    yield root, state
    daemon.stop(state)
    wait_for(lambda: not daemon.socket_path(state).exists(), 15)


def alive(state):
    info = daemon.status(state)
    return bool(info and info["alive"])


def test_the_second_query_is_served_by_the_service(repo, capsys):
    _root, state = repo
    assert cli.main(QUERY) == 0
    in_process = capsys.readouterr().out
    assert "CodeController.java" in in_process
    assert wait_for(lambda: alive(state)), (state / daemon.LOG).read_text()
    reply = daemon.ask(state, QUERY)
    assert reply is not None and reply["code"] == 0
    assert reply["stdout"] == in_process
    assert cli.main(QUERY) == 0 and capsys.readouterr().out == in_process


def test_the_service_follows_head(repo, monkeypatch):
    root, state = repo
    # The suite disables periodic refresh. A stale index must still refresh in the background.
    monkeypatch.setenv("REPOLENS_REFRESH_SECONDS", "0")
    daemon.start(state, QUERY)
    assert wait_for(lambda: alive(state)), (state / daemon.LOG).read_text()
    (root / "tools" / "added.py").write_text("def brandNewHelper():\n    return 1\n", encoding="utf-8")
    git = ["git", "-C", str(root), "-c", "user.name=t", "-c", "user.email=t@example.com"]
    subprocess.run([*git, "add", "-A"], check=True)
    subprocess.run([*git, "commit", "-qm", "add helper"], check=True)
    reply = daemon.ask(state, ["search", "brandNewHelper", "-k", "3"])
    assert reply is not None and "background" in reply["stderr"]

    def found():
        later = daemon.ask(state, ["search", "brandNewHelper", "-k", "3"])
        return later is not None and "tools/added.py" in later["stdout"]

    assert wait_for(found), (state / daemon.LOG).read_text(encoding="utf-8")[-800:]


def test_an_idle_service_exits(repo, monkeypatch):
    _root, state = repo
    monkeypatch.setenv("REPOLENS_DAEMON_IDLE", "1")
    daemon.start(state, QUERY)
    assert wait_for(lambda: (state / daemon.INFO).exists())
    assert wait_for(lambda: not daemon.socket_path(state).exists() and not (state / daemon.INFO).exists())


def test_an_outdated_service_is_replaced(repo, monkeypatch):
    _root, state = repo
    daemon.start(state, QUERY)
    assert wait_for(lambda: alive(state)), (state / daemon.LOG).read_text()
    monkeypatch.setattr(daemon, "code_version", lambda: "older")
    monkeypatch.setattr(daemon, "start", lambda *_: None)
    assert daemon.ask(state, QUERY) is None
    assert wait_for(lambda: not daemon.socket_path(state).exists())


def test_disabled_runs_in_process_and_starts_nothing(repo, monkeypatch, capsys):
    _root, state = repo
    monkeypatch.setenv("REPOLENS_DAEMON", "0")
    assert cli.main(QUERY) == 0 and "CodeController.java" in capsys.readouterr().out
    time.sleep(1.0)
    assert not daemon.socket_path(state).exists() and daemon.ask(state, QUERY) is None
    assert cli.main(["daemon", "status"]) == 1
