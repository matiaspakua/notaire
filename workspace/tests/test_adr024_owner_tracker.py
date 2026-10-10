#!/usr/bin/env python3
"""Guards #1443: ADR-024 / REPO-SPLIT-PLAN point at live Owner tracker after #1197 auto-close."""
from __future__ import annotations

import os
import unittest

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
ADR_024 = os.path.join(
    REPO_ROOT, "docs", "200-architecture", "202-ADR", "ADR-024-repository-topology.md"
)
PLAN = os.path.join(REPO_ROOT, "docs", "300-development", "REPO-SPLIT-PLAN.md")


class Adr024OwnerTrackerTest(unittest.TestCase):
    def test_adr024_points_at_live_owner_tracker(self):
        with open(ADR_024, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn("#1443", text, "ADR-024 must reference live Owner tracker #1443")
        self.assertRegex(text, r"(?i)auto-closed|#1197 was closed|follow-up")

    def test_repo_split_plan_umbrella_points_at_1443(self):
        with open(PLAN, encoding="utf-8") as fh:
            text = fh.read()
        self.assertIn("#1443", text)
        self.assertRegex(text, r"Umbrella[^\n]*#1443|#1443[^\n]*Umbrella|live Owner tracker")


if __name__ == "__main__":
    unittest.main()
