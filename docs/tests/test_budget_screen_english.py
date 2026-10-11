#!/usr/bin/env python3
"""The living budget-template spec names the live budget screen."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVING = (
    ROOT / "docs" / "openspec" / "specs" / "presupuesto-from-template" / "spec.md"
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
    ROOT / "docs" / "100-business" / "102-use-cases" / "CU01 – Preparar Presupuesto.md",
    ROOT / "docs" / "100-business" / "102-use-cases" / "CU39 – Crear Plantilla Presupuesto.md",
    ROOT / "docs" / "100-business" / "102-use-cases" / "CU71 – Gestión de Items.md",
)
SCREEN = "`/dashboard/presupuestos`"
LIVE = "`/dashboard/budgets`"


class BudgetScreenEnglishTest(unittest.TestCase):
    def test_living_spec_names_the_screen(self):
        text = LIVING.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertNotIn("/dashboard/presupuestos", text)

    def test_design_cites_the_screen_the_use_cases_still_name(self):
        text = DESIGN.read_text(encoding="utf-8")
        self.assertIn(SCREEN, text)
        self.assertIn("`presupuesto-from-template`", text)
        for path in USE_CASES:
            self.assertIn(SCREEN.strip("`"), path.read_text(encoding="utf-8"), path.name)


if __name__ == "__main__":
    unittest.main()
