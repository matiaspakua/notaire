#!/usr/bin/env python3
"""The engineering-sequences design names the retired due-date formula."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
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
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-04-tipo-documento-vencimiento-config"
    / "specs"
    / "documento-presentado-herencia-vencimiento"
    / "spec.md"
)
RETIRED = "`fechaVencimiento = fechaIngreso + diasVencimiento`"
LIVE = ("`dateDue`", "`dateEntry`", "`dueDays`")


class RetiredDueDateFormulaEnglishTest(unittest.TestCase):
    def test_design_cites_the_formula_cu42_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu42 = CU42.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        for live in LIVE:
            self.assertIn(live, text)
        self.assertIn(RETIRED, cu42)
        for live in LIVE:
            self.assertNotIn(live.strip("`"), cu42)
        self.assertIn("`fechaVencimiento`", archived)
        self.assertIn("`fechaIngreso`", archived)
        for live in LIVE:
            self.assertNotIn(live.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
