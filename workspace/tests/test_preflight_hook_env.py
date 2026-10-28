#!/usr/bin/env python3
"""Guards #1958 (CU76): preflight drops the git environment a pre-push hook exports.

In a linked worktree, git runs the pre-push hook with GIT_DIR set to
`.git/worktrees/<name>`. Self-tests that build throwaway repositories then ran
`git init` and `git config` against that GIT_DIR: the shared `.git/config` got
`core.bare = true` and a fixture identity, and every checkout of the repository broke
(2026-10-28). Preflight runs from the repository root, so plain discovery is correct.

Run with: python3 workspace/tests/test_preflight_hook_env.py
"""
from __future__ import annotations

import re
import unittest
from pathlib import Path

PREFLIGHT = Path(__file__).resolve().parents[2] / "workspace" / "sdlc" / "preflight.sh"
HOOK_VARS = ("GIT_DIR", "GIT_WORK_TREE", "GIT_INDEX_FILE")


class PreflightHookEnvironmentTest(unittest.TestCase):
    def test_hook_git_variables_are_unset_before_any_check(self):
        text = PREFLIGHT.read_text(encoding="utf-8")
        unset = re.search(r"^unset ([A-Z_ ]+)$", text, re.M)
        self.assertIsNotNone(unset, "preflight.sh must unset the git variables a hook exports")
        for name in HOOK_VARS:
            self.assertIn(name, unset.group(1).split())
        first_check = text.index('\nrun "')
        self.assertLess(unset.start(), first_check, "unset must come before the first check")


if __name__ == "__main__":
    unittest.main()
