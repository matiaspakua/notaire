#!/usr/bin/env python3
"""Guards #1681 (CU76): agent work is continuously published to its branch and issue."""

from __future__ import annotations

import os
import stat
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
HELPER = REPO_ROOT / "workspace" / "sdlc" / "publish-progress.sh"
RULE_DOCS = [
    REPO_ROOT / "CONSTITUTION.md",
    REPO_ROOT / "CONSTITUTION-AGENT-CARD.md",
    REPO_ROOT / ".claude" / "rules" / "ai-agent-workflow.md",
    REPO_ROOT / ".claude" / "rules" / "general.md",
]
FAKE_GH = '#!/bin/sh\nprintf \'%s\\n\' "$*" >> "$GH_LOG"\ncat >> "$GH_LOG.body"\n'


def git(cwd: Path, *args: str) -> str:
    result = subprocess.run(["git", *args], cwd=cwd, capture_output=True, text=True, check=True)
    return result.stdout.strip()


class PublishProgressTest(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp(prefix="publish-"))
        self.addCleanup(subprocess.run, ["rm", "-rf", str(self.tmp)])
        self.remote = self.tmp / "remote.git"
        self.work = self.tmp / "work"
        subprocess.run(["git", "init", "-q", "--bare", "-b", "main", str(self.remote)], check=True)
        subprocess.run(["git", "init", "-q", "-b", "main", str(self.work)], check=True)
        git(self.work, "config", "user.email", "t@example.com")
        git(self.work, "config", "user.name", "t")
        git(self.work, "remote", "add", "origin", str(self.remote))
        (self.work / "a.txt").write_text("a", encoding="utf-8")
        git(self.work, "add", "a.txt")
        git(self.work, "commit", "-q", "-m", "chore: init")
        git(self.work, "push", "-q", "origin", "main")
        bindir = self.tmp / "bin"
        bindir.mkdir()
        gh = bindir / "gh"
        gh.write_text(FAKE_GH, encoding="utf-8")
        gh.chmod(gh.stat().st_mode | stat.S_IEXEC)
        self.gh_log = self.tmp / "gh.log"
        self.env = {**os.environ, "PATH": f"{bindir}:{os.environ['PATH']}", "GH_LOG": str(self.gh_log)}

    def publish(self, *args: str) -> subprocess.CompletedProcess:
        return subprocess.run(
            ["bash", str(HELPER), *args], cwd=self.work, env=self.env, capture_output=True, text=True, timeout=60
        )

    def feature_branch_with_commit(self):
        git(self.work, "checkout", "-q", "-b", "feat/42_thing")
        (self.work / "b.txt").write_text("b", encoding="utf-8")
        git(self.work, "add", "b.txt")
        git(self.work, "commit", "-q", "-m", "test: add b")

    def test_pushes_the_branch_and_comments_on_the_issue(self):
        self.feature_branch_with_commit()
        result = self.publish("42", "Stage 2: failing tests committed")
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        remote_head = git(self.work, "ls-remote", "--heads", "origin", "feat/42_thing").split()[0]
        self.assertEqual(remote_head, git(self.work, "rev-parse", "HEAD"))
        self.assertIn("issue comment 42", self.gh_log.read_text(encoding="utf-8"))
        body = Path(str(self.gh_log) + ".body").read_text(encoding="utf-8")
        for expected in ("feat/42_thing", "test: add b", "Stage 2: failing tests committed"):
            self.assertIn(expected, body)

    def test_main_is_refused(self):
        result = self.publish("42", "anything")
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse(self.gh_log.exists())

    def test_failed_push_posts_no_comment(self):
        self.feature_branch_with_commit()
        git(self.work, "remote", "set-url", "origin", str(self.tmp / "missing.git"))
        result = self.publish("42", "anything")
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse(self.gh_log.exists())

    def test_issue_number_is_required(self):
        self.feature_branch_with_commit()
        self.assertNotEqual(self.publish().returncode, 0)


class RuleDocumentationTest(unittest.TestCase):
    def test_every_document_states_the_rule(self):
        for doc in RULE_DOCS:
            with self.subTest(doc=doc.name):
                text = doc.read_text(encoding="utf-8").lower()
                self.assertIn("continuous publication", text)
                self.assertIn("publish-progress.sh", text)


if __name__ == "__main__":
    unittest.main()
