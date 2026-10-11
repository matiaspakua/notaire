#!/usr/bin/env python3
"""The living admin-route spec names the administration screen."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVING = (
    ROOT / "docs" / "openspec" / "specs" / "admin-route-guard" / "spec.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-fix-1052-admin-route-guard"
    / "specs"
    / "admin-route-guard"
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
USE_CASE = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU78 – Security and Compliance.md"
)
RETIRED = "`/dashboard/administracion/**`"
LIVE = "/dashboard/administration"


class AdminScreenEnglishTest(unittest.TestCase):
    def test_living_spec_names_the_screen(self):
        text = LIVING.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertNotIn("/dashboard/administracion", text)

    def test_design_cites_the_screen_cu78_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = USE_CASE.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn("`admin-route-guard`", text)
        self.assertIn("/dashboard/administracion/**", use_case)
        self.assertIn("/dashboard/administracion", archived)
        self.assertNotIn(LIVE, archived)


if __name__ == "__main__":
    unittest.main()
