#!/usr/bin/env python3
"""The doc-traceability fixture heading is English. The design path stays."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CHECKS = ROOT / "workspace/tests/test_pr_checks.py"


class DocTraceabilityFixtureEnglishTest(unittest.TestCase):
    def test_fixture_heading_is_english(self):
        text = CHECKS.read_text(encoding="utf-8")
        self.assertIn('"# Management\\n"', text)
        self.assertNotIn('"# Gestión\\n"', text)
        self.assertIn("docs/200-architecture/203-design/GESTION.md", text)


if __name__ == "__main__":
    unittest.main()
