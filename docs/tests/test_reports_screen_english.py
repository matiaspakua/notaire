#!/usr/bin/env python3
"""The living navigation spec names the reports screen."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVING = (
    ROOT
    / "docs"
    / "openspec"
    / "specs"
    / "dashboard-nav-canonical-routes"
    / "spec.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-fix-1058-suplencias-reportes-nav"
    / "specs"
    / "dashboard-nav-canonical-routes"
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
USE_CASES = (
    ROOT / "docs" / "100-business" / "102-use-cases" / "CU24 – Generar libro de índices.md",
    ROOT / "docs" / "100-business" / "102-use-cases" / "CU25 – Generar Declaración Jurada del mes.md",
    ROOT / "docs" / "100-business" / "102-use-cases" / "CU50 – Generar Declaración Jurada de Rentas.md",
)
SCREEN = "`/dashboard/reportes`"
LIVE = "/dashboard/reports"


class ReportsScreenEnglishTest(unittest.TestCase):
    def test_living_spec_names_the_screen(self):
        text = LIVING.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertNotIn(SCREEN.strip("`"), text)
        self.assertIn("`reportes`", text)

    def test_design_cites_the_screen_the_use_cases_still_name(self):
        text = DESIGN.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(SCREEN, text)
        self.assertIn("`dashboard-nav-canonical-routes`", text)
        for path in USE_CASES:
            self.assertIn(SCREEN.strip("`"), path.read_text(encoding="utf-8"), path.name)
        self.assertIn(SCREEN.strip("`"), archived)
        self.assertNotIn(LIVE, archived)


if __name__ == "__main__":
    unittest.main()
