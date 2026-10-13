#!/usr/bin/env python3
"""Guards #1647 (CU76): agent harness config works on any machine and surfaces lost work."""

from __future__ import annotations

import json
import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
OPENCODE = REPO_ROOT / "opencode.json"
HOOK = REPO_ROOT / ".claude" / "hooks" / "session-start-openspec-status.sh"
WORKFLOW_RULE = REPO_ROOT / ".claude" / "rules" / "ai-agent-workflow.md"


def git(repo: str, *args: str) -> str:
    env = {k: v for k, v in os.environ.items() if not k.startswith("GIT_")}
    return subprocess.run(["git", "-C", repo, *args], check=True, capture_output=True, text=True, env=env).stdout


class OpencodePortabilityTest(unittest.TestCase):
    def setUp(self):
        self.config = json.loads(OPENCODE.read_text(encoding="utf-8"))

    def test_no_absolute_user_paths(self):
        self.assertNotIn("/Users/", OPENCODE.read_text(encoding="utf-8"))
        self.assertNotIn("/home/", OPENCODE.read_text(encoding="utf-8"))

    def test_loads_only_the_always_loaded_set(self):
        instructions = self.config["instructions"]
        self.assertEqual(set(instructions), {"AGENTS.md", "CONSTITUTION-AGENT-CARD.md"})


class UnpushedWorkWarningTest(unittest.TestCase):
    def test_session_start_lists_branches_never_pushed(self):
        repo = tempfile.mkdtemp(prefix="hook-")
        self.addCleanup(shutil.rmtree, repo, ignore_errors=True)
        git(repo, "init", "-q", "-b", "main")
        git(repo, "config", "user.email", "t@example.com")
        git(repo, "config", "user.name", "t")
        git(repo, "commit", "-q", "--allow-empty", "-m", "chore: root")
        git(repo, "checkout", "-q", "-b", "fix/1620_lost-work")
        git(repo, "commit", "-q", "--allow-empty", "-m", "fix: finished but never pushed")
        result = subprocess.run(
            ["bash", str(HOOK)], capture_output=True, text=True,
            env={**{k: v for k, v in os.environ.items() if not k.startswith("GIT_")}, "CLAUDE_PROJECT_DIR": repo},
        )
        self.assertEqual(result.returncode, 0)
        self.assertIn("never pushed", result.stdout.lower())
        self.assertIn("fix/1620_lost-work", result.stdout)


class PushVerificationRuleTest(unittest.TestCase):
    def test_workflow_rule_verifies_the_push_landed(self):
        self.assertIn("git ls-remote --heads origin", WORKFLOW_RULE.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
