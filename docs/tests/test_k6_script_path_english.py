#!/usr/bin/env python3
"""The living k6 spec names the script the weekly workflow runs."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVING = ROOT / "docs" / "openspec" / "specs" / "k6-load-test-restore" / "spec.md"
ARCHIVE = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-fix-1047-k6-load-test"
    / "specs"
    / "k6-load-test-restore"
    / "spec.md"
)
LIVE = "`infra/performance/k6/load-test.js`"
RETIRED = "`performance-test/k6/load-test.js`"


class K6ScriptPathEnglishTest(unittest.TestCase):
    def test_living_spec_names_the_script_the_workflow_runs(self):
        text = LIVING.read_text(encoding="utf-8")
        self.assertEqual(text.count(LIVE), 3)
        self.assertNotIn(RETIRED, text)
        workflow = (ROOT / ".github" / "workflows" / "performance-test.yml").read_text(
            encoding="utf-8"
        )
        self.assertIn("infra/performance/k6/load-test.js", workflow)

    def test_archived_change_keeps_the_path_it_restored(self):
        text = ARCHIVE.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertNotIn(LIVE, text)


if __name__ == "__main__":
    unittest.main()
