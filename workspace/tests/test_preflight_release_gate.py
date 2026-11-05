#!/usr/bin/env python3
"""#2001: preflight --full is the pull-request gate. GitHub does not re-run it."""

from __future__ import annotations

import unittest
from pathlib import Path

PREFLIGHT = Path(__file__).resolve().parents[2] / "workspace" / "sdlc" / "preflight.sh"
HOOK = Path(__file__).resolve().parents[2] / ".githooks" / "pre-push"


class PreflightReleaseGateTest(unittest.TestCase):
    def test_help_says_default_is_not_enough_to_open_a_pr(self):
        text = PREFLIGHT.read_text(encoding="utf-8")
        self.assertIn("not sufficient to open a pull request", text)
        self.assertNotIn("CI WILL run", text)

    def test_pre_push_hook_runs_full(self):
        text = HOOK.read_text(encoding="utf-8")
        self.assertIn("--full", text)
        self.assertNotIn("does not need a running app stack", text)


if __name__ == "__main__":
    unittest.main()
