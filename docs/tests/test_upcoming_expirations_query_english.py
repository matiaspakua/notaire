#!/usr/bin/env python3
"""The living upcoming-expirations spec names the live query `days`."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVING = (
    ROOT / "docs" / "openspec" / "specs" / "upcoming-expirations" / "spec.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-05-feat-802-proximos-vencimientos"
    / "specs"
    / "upcoming-expirations"
    / "spec.md"
)
DESIGN = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "docs-921-engineering-sequences"
    / "design.md"
)
CU42 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU42 – Informar próximos vencimientos.md"
)
RETIRED = "`GET /api/v1/documento-presentado/proximos-vencimientos?dias=N`"
LIVE = "`GET /api/v1/submitted-documents/upcoming-expirations`"


class UpcomingExpirationsQueryEnglishTest(unittest.TestCase):
    def test_living_spec_names_the_days_query(self):
        text = LIVING.read_text(encoding="utf-8")
        self.assertIn("days=0", text)
        self.assertIn("days=366", text)
        self.assertIn("without days", text)
        self.assertNotIn("dias=", text)
        self.assertIn(LIVE, text)

    def test_archived_spec_keeps_dias(self):
        text = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn("dias=0", text)
        self.assertNotIn("days=0", text)

    def test_design_cites_the_path_cu42_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU42.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(RETIRED.strip("`"), use_case)
        self.assertIn("`upcoming-expirations`", text)


if __name__ == "__main__":
    unittest.main()
