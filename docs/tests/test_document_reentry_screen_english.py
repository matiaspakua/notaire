#!/usr/bin/env python3
"""DOC-GESTION names the live document re-entry screen."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GESTION = ROOT / "docs" / "200-architecture" / "203-design" / "GESTION.md"
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
    / "CU43 – Reingresar documentación.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "000-archive"
    / "speckit"
    / "specs"
    / "archive"
    / "003-cu43-reingresar-documentacion"
    / "plan.md"
)
SCREEN = "`/dashboard/reingreso-documentacion`"
LIVE = "`/dashboard/document-reentry`"


class DocumentReentryScreenEnglishTest(unittest.TestCase):
    def test_gestion_names_the_screen(self):
        text = GESTION.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertNotIn(SCREEN.strip("`"), text)

    def test_design_cites_the_screen_cu43_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = USE_CASE.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(SCREEN, text)
        self.assertIn("`DOC-GESTION`", text)
        self.assertIn(SCREEN.strip("`"), use_case)
        self.assertIn("app/dashboard/reingreso-documentacion/", archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
