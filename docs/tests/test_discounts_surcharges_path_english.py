#!/usr/bin/env python3
"""The living discounts spec names the live report path."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVING = (
    ROOT
    / "docs"
    / "openspec"
    / "specs"
    / "descuentos-recargos-presupuesto"
    / "spec.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-02-descuentos-recargos-presupuesto"
    / "design.md"
)
DESIGN = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "docs-921-engineering-sequences"
    / "design.md"
)
CU45 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU45 – Modificar presupuesto.md"
)
RETIRED = "`GET /api/v1/items/presupuesto/{idPresupuesto}/descuentos-recargos`"
LIVE = "`GET /api/v1/items/budget/{idBudget}/discounts-surcharges`"


class DiscountsSurchargesPathEnglishTest(unittest.TestCase):
    def test_living_spec_names_the_report_path(self):
        text = LIVING.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertNotIn("items/presupuesto", text)

    def test_archived_design_keeps_the_retired_path(self):
        text = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED.strip("`"), text)
        self.assertNotIn("discounts-surcharges", text)

    def test_design_cites_the_path_cu45_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU45.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(RETIRED.strip("`"), use_case)
        self.assertIn("`descuentos-recargos-presupuesto`", text)


if __name__ == "__main__":
    unittest.main()
