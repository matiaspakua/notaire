#!/usr/bin/env python3
"""Full preflight starts a down stack, and cloud branch names are a known pattern."""

from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PREFLIGHT = ROOT / "workspace" / "sdlc" / "preflight.sh"
WORKFLOW = ROOT / ".github" / "workflows" / "pr-validation.yml"


class PreflightStackAndBranchTest(unittest.TestCase):
    def test_full_mode_starts_the_stack_before_declaring_it_unreachable(self) -> None:
        text = PREFLIGHT.read_text(encoding="utf-8")
        start = text.index("workspace/stack/start.sh --no-admin")
        fail = text.index("stack not reachable")
        self.assertLess(start, fail)
        self.assertIn("starting it once", text)

    def test_cursor_branches_match_the_documented_pattern(self) -> None:
        preflight = PREFLIGHT.read_text(encoding="utf-8")
        workflow = WORKFLOW.read_text(encoding="utf-8")
        self.assertIn("|cursor/", preflight)
        self.assertIn("|cursor/", workflow)


if __name__ == "__main__":
    unittest.main()
