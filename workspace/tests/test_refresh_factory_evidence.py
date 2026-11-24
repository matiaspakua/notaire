#!/usr/bin/env python3
"""
Guards issue #2469: one command refreshes the factory evidence (RepoLens benches, Pages snapshots, factory
metrics), paid steps are opt-in, and `--check` names stale engines and results.

Run with: python3 workspace/tests/test_refresh_factory_evidence.py
"""
import importlib.util
import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
SCRIPT = REPO_ROOT / "workspace" / "ci" / "refresh-factory-evidence.py"

for _var in [v for v in os.environ if v.startswith("GIT_")]:
    del os.environ[_var]


def load():
    spec = importlib.util.spec_from_file_location("refresh_factory_evidence", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def git(repo, *args):
    return subprocess.run(["git", "-C", str(repo), "-c", "user.email=t@e", "-c", "user.name=t", *args],
                          check=True, capture_output=True, text=True).stdout.strip()


class StepsTest(unittest.TestCase):
    def test_default_steps_are_free_and_ordered(self):
        names = [name for name, _ in load().steps()]
        self.assertEqual(["repolens index", "retrieval bench", "holdout bench", "bench report",
                          "pages bench snapshot", "junie usage", "factory metrics snapshot"], names)

    def test_agent_and_history_are_opt_in(self):
        names = [name for name, _ in load().steps(agent=True, history=True)]
        self.assertIn("agent bench", names)
        self.assertIn("ai sdlc history", names)
        self.assertIn("delivery metrics", names)  # #2475: every Pages snapshot from one command
        self.assertIn("roadmap snapshot", names)
        self.assertLess(names.index("agent bench"), names.index("bench report"))

    def test_dry_run_prints_the_steps_and_runs_nothing(self):
        result = subprocess.run(["python3", str(SCRIPT), "--dry-run"], cwd=REPO_ROOT, capture_output=True, text=True)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("retrieval bench", result.stdout)
        self.assertNotIn("agent bench", result.stdout)


class EvidenceRootTest(unittest.TestCase):
    """The ledger and costs are git-ignored and live in the main worktree, not in the worktree that refreshes."""

    def test_evidence_root_is_the_main_worktree(self):
        main = Path(tempfile.mkdtemp(prefix="main-"))
        git(main, "init", "-q", "-b", "main")
        git(main, "commit", "-q", "--allow-empty", "-m", "root")
        linked = main.parent / (main.name + "-wt")
        git(main, "worktree", "add", "-q", "-b", "work", str(linked))
        self.assertEqual(main.resolve(), load().evidence_root(linked).resolve())

    def test_kit_steps_read_the_evidence_root(self):
        plan = dict(load().steps(evidence=Path("/evidence")))
        self.assertIn("/evidence", plan["junie usage"])
        self.assertIn("/evidence", plan["factory metrics snapshot"])


class StaleResultsTest(unittest.TestCase):
    def results(self, stamps):
        folder = Path(tempfile.mkdtemp(prefix="bench-results-"))
        for name, version in stamps.items():
            body = {"summary": {}} if version is None else {"repolens_version": version, "summary": {}}
            (folder / name).write_text(json.dumps(body), encoding="utf-8")
        return folder

    def test_result_from_another_version_is_stale(self):
        folder = self.results({"retrieval.json": "0.6.2", "retrieval-notaire-holdout.json": "0.7.0"})
        stale = load().stale_results(folder, "0.7.0")
        self.assertEqual(1, len(stale))
        self.assertIn("retrieval.json", stale[0])
        self.assertIn("0.6.2", stale[0])

    def test_unstamped_result_is_stale(self):
        stale = load().stale_results(self.results({"retrieval.json": None}), "0.7.0")
        self.assertEqual(1, len(stale))


class EnginesBehindTest(unittest.TestCase):
    def repo_with_main_ahead(self, path):
        repo = Path(tempfile.mkdtemp(prefix="engines-"))
        git(repo, "init", "-q", "-b", "main")
        (repo / path).mkdir(parents=True)
        (repo / path / "a.py").write_text("x = 1\n", encoding="utf-8")
        git(repo, "add", "-A")
        git(repo, "commit", "-q", "-m", "root")
        git(repo, "branch", "work")
        (repo / path / "a.py").write_text("x = 2\n", encoding="utf-8")
        git(repo, "commit", "-q", "-am", "engine moves on main")
        git(repo, "update-ref", "refs/remotes/origin/main", "main")
        git(repo, "checkout", "-q", "work")
        return repo

    def test_engine_behind_origin_main_is_stale(self):
        repo = self.repo_with_main_ahead("framework/repolens")
        behind = load().engines_behind(repo)
        self.assertEqual(1, len(behind))
        self.assertIn("RepoLens", behind[0])
        self.assertIn("origin/main", behind[0])

    def test_current_engines_and_results_pass(self):
        repo = self.repo_with_main_ahead("framework/repolens")
        git(repo, "checkout", "-q", "main")
        module = load()
        self.assertEqual([], module.engines_behind(repo))
        folder = StaleResultsTest.results(self, {"retrieval.json": "0.7.0"})
        self.assertEqual([], module.stale_results(folder, "0.7.0"))


class SessionStartTest(unittest.TestCase):
    def test_session_start_reports_evidence_freshness_and_never_blocks(self):
        hook = REPO_ROOT / ".claude" / "hooks" / "session-start-openspec-status.sh"
        result = subprocess.run(["bash", str(hook)], capture_output=True, text=True,
                                env={**os.environ, "CLAUDE_PROJECT_DIR": str(REPO_ROOT)})
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("evidence:", result.stdout)


if __name__ == "__main__":
    unittest.main()
