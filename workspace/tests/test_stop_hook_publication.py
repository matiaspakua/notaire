#!/usr/bin/env python3
"""Guards the Stop hook for continuous publication (#1681; Notaire audit E4; cross-repo assessment 2026-11).

The hook blocks a stop once when an Issue branch holds commits that are on no remote, and
never otherwise: not twice in a row, not on main, not on a non-Issue branch, not once pushed.
"""

from __future__ import annotations

import json
import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
HOOK = REPO_ROOT / ".claude" / "hooks" / "stop-unpublished-work.sh"
SETTINGS = REPO_ROOT / ".claude" / "settings.json"
# A git hook (pre-push runs these suites) exports GIT_DIR and friends; inherited, they would
# point these temp repositories at the real one.
ENV = {k: v for k, v in os.environ.items() if not k.startswith("GIT_")}


def git(repo: str, *args: str) -> str:
    return subprocess.run(["git", "-C", repo, *args], check=True, capture_output=True, text=True, env=ENV).stdout


class StopHookTest(unittest.TestCase):
    def setUp(self):
        self.repo = tempfile.mkdtemp(prefix="stop-hook-")
        self.addCleanup(shutil.rmtree, self.repo, ignore_errors=True)
        git(self.repo, "init", "-q", "-b", "main")
        git(self.repo, "config", "user.email", "t@example.com")
        git(self.repo, "config", "user.name", "t")
        git(self.repo, "commit", "-q", "--allow-empty", "-m", "chore: root")

    def run_hook(self, payload: dict) -> subprocess.CompletedProcess:
        return subprocess.run(
            ["bash", str(HOOK)], input=json.dumps({"cwd": self.repo, **payload}),
            capture_output=True, text=True, env={**ENV, "CLAUDE_PROJECT_DIR": self.repo},
        )

    def test_blocks_once_when_issue_branch_commits_are_only_local(self):
        git(self.repo, "checkout", "-q", "-b", "fix/1620_lost-work")
        git(self.repo, "commit", "-q", "--allow-empty", "-m", "fix: finished but never pushed")
        result = self.run_hook({"stop_hook_active": False})
        self.assertEqual(result.returncode, 2)
        self.assertIn("publish-progress.sh 1620", result.stderr)

    def test_lets_the_retry_through(self):
        git(self.repo, "checkout", "-q", "-b", "fix/1620_lost-work")
        git(self.repo, "commit", "-q", "--allow-empty", "-m", "fix: finished but never pushed")
        self.assertEqual(self.run_hook({"stop_hook_active": True}).returncode, 0)

    def test_ignores_main_and_non_issue_branches(self):
        git(self.repo, "commit", "-q", "--allow-empty", "-m", "chore: local main commit")
        self.assertEqual(self.run_hook({}).returncode, 0)
        git(self.repo, "checkout", "-q", "-b", "spike/try-something")
        git(self.repo, "commit", "-q", "--allow-empty", "-m", "chore: spike")
        self.assertEqual(self.run_hook({}).returncode, 0)

    def test_passes_once_the_commits_are_on_a_remote(self):
        remote = tempfile.mkdtemp(prefix="stop-hook-remote-")
        self.addCleanup(shutil.rmtree, remote, ignore_errors=True)
        git(remote, "init", "-q", "--bare")
        git(self.repo, "remote", "add", "origin", remote)
        git(self.repo, "checkout", "-q", "-b", "feat/42_published")
        git(self.repo, "commit", "-q", "--allow-empty", "-m", "feat: published")
        git(self.repo, "push", "-q", "-u", "origin", "feat/42_published")
        self.assertEqual(self.run_hook({}).returncode, 0)

    def test_bad_payload_never_traps_the_session(self):
        result = subprocess.run(["bash", str(HOOK)], input="not json", capture_output=True, text=True,
                                env={**ENV, "CLAUDE_PROJECT_DIR": self.repo})
        self.assertEqual(result.returncode, 0)


class StopHookWiredTest(unittest.TestCase):
    def test_settings_register_the_stop_hook(self):
        settings = json.loads(SETTINGS.read_text(encoding="utf-8"))
        commands = [h["command"] for entry in settings["hooks"].get("Stop", []) for h in entry["hooks"]]
        self.assertTrue(any("stop-unpublished-work.sh" in c for c in commands), commands)


if __name__ == "__main__":
    unittest.main()
