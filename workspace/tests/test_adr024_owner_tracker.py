#!/usr/bin/env python3
"""Guards live Owner umbrella tracker after keyword-parse closures of #1197/#1443."""
from __future__ import annotations

import os
import re
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
ADR_024 = os.path.join(
    REPO_ROOT, "docs", "200-architecture", "202-ADR", "ADR-024-repository-topology.md"
)
PLAN = os.path.join(REPO_ROOT, "docs", "300-development", "REPO-SPLIT-PLAN.md")
LIVE = "1445"


class Adr024OwnerTrackerTest(unittest.TestCase):
    def test_adr024_points_at_live_owner_tracker(self):
        with open(ADR_024, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn(f"#{LIVE}", text, f"ADR-024 must reference live Owner tracker #{LIVE}")
        self.assertRegex(text, r"(?i)keyword parsing|auto-closed|were closed by")

    def test_repo_split_plan_umbrella_points_at_live_tracker(self):
        with open(PLAN, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn(f"#{LIVE}", text)
        self.assertRegex(text, rf"(?i)live Owner tracker.*#{LIVE}|#{LIVE} \(replaces")


if __name__ == "__main__":
    unittest.main()
