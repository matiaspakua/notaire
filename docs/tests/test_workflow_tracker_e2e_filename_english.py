#!/usr/bin/env python3
"""The workflow tracker design cites the live E2E file."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TRACKER = ROOT / "docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md"
LIVE = "testing/e2e/tests/TS-0138-workflow-tracker.spec.ts"
RETIRED = "testing/e2e/tests/workflow-tracker.spec.ts"
HISTORICAL = (
    ROOT
    / "docs/openspec/specs/e2e-suite-in-testing/spec.md"
)


class WorkflowTrackerE2EFilenameEnglishTest(unittest.TestCase):
    def test_design_cites_the_live_e2e_file(self):
        text = TRACKER.read_text(encoding="utf-8")
        self.assertIn(f"`{LIVE}`", text)
        self.assertNotIn(f"`{RETIRED}`", text)
        historical = HISTORICAL.read_text(encoding="utf-8")
        self.assertIn("`workflow-tracker.spec.ts`", historical)


if __name__ == "__main__":
    unittest.main()
