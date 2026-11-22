#!/usr/bin/env python3
"""Guards #2413 (CU76): the pre-push hook does not gate again a commit that already passed.

The real hook runs against a throwaway repository whose preflight is a stub that counts its runs.

Run with: python3 workspace/tests/test_preflight_hook_cache.py
"""
from __future__ import annotations

import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
HOOK = ROOT / ".githooks" / "pre-push"
ZERO = "0" * 40
STUB = """#!/usr/bin/env bash
echo run >> "$(dirname "$0")/../../runs.log"
exit "${STUB_EXIT:-0}"
"""


class HookCacheTest(unittest.TestCase):
    def setUp(self):
        self.repo = Path(tempfile.mkdtemp(prefix="hook-cache-"))
        self.addCleanup(shutil.rmtree, self.repo, ignore_errors=True)
        self.git("init", "-q")
        self.git("config", "user.email", "test@example.invalid")
        self.git("config", "user.name", "test")
        (self.repo / ".githooks").mkdir()
        shutil.copy(HOOK, self.repo / ".githooks" / "pre-push")
        sdlc = self.repo / "workspace" / "sdlc"
        sdlc.mkdir(parents=True)
        (sdlc / "preflight.sh").write_text(STUB, encoding="utf-8")
        self.commit("first")

    def git(self, *args):
        env = {k: v for k, v in os.environ.items() if not k.startswith("GIT_")}
        return subprocess.run(["git", *args], cwd=self.repo, env=env, check=True,
                              capture_output=True, text=True).stdout.strip()

    def commit(self, message):
        (self.repo / "file.txt").write_text(message, encoding="utf-8")
        self.git("add", "-A")
        self.git("commit", "-q", "-m", message)
        return self.git("rev-parse", "HEAD")

    def push(self, sha, **env_extra):
        env = {k: v for k, v in os.environ.items() if not k.startswith(("GIT_", "PREFLIGHT_"))}
        env.update(env_extra)
        line = f"refs/heads/main {sha} refs/heads/main {ZERO}\n"
        return subprocess.run(["bash", ".githooks/pre-push", "origin", "https://example.invalid/repo.git"],
                              cwd=self.repo, env=env, input=line, capture_output=True, text=True)

    def runs(self):
        log = self.repo / "runs.log"
        return len(log.read_text(encoding="utf-8").splitlines()) if log.exists() else 0

    def test_the_same_commit_pushed_again_is_not_gated_twice(self):
        sha = self.git("rev-parse", "HEAD")
        first = self.push(sha)
        self.assertEqual(0, first.returncode, first.stdout + first.stderr)
        second = self.push(sha)
        self.assertEqual(0, second.returncode, second.stdout + second.stderr)
        self.assertEqual(1, self.runs())
        self.assertIn("already passed", second.stdout)

    def test_a_new_commit_runs_the_gate(self):
        self.push(self.git("rev-parse", "HEAD"))
        self.push(self.commit("second"))
        self.assertEqual(2, self.runs())

    def test_a_failed_run_is_not_recorded(self):
        sha = self.git("rev-parse", "HEAD")
        self.assertNotEqual(0, self.push(sha, STUB_EXIT="1").returncode)
        self.push(sha)
        self.assertEqual(2, self.runs())

    def test_a_pass_of_the_full_gate_does_not_cover_all(self):
        sha = self.git("rev-parse", "HEAD")
        self.push(sha)
        self.push(sha, PREFLIGHT_ALL="1")
        self.assertEqual(2, self.runs())
        self.push(sha)
        self.assertEqual(2, self.runs())


if __name__ == "__main__":
    unittest.main()
