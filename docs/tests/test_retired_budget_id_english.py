#!/usr/bin/env python3
"""The engineering-sequences design names the retired budget id."""

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
CU45 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU45 – Modificar presupuesto.md"
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
RETIRED = "`idPresupuesto`"
LIVE = "`idBudget`"


class RetiredBudgetIdEnglishTest(unittest.TestCase):
    def test_design_cites_the_id_cu45_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu45 = CU45.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED.strip("`"), cu45)
        self.assertNotIn(LIVE.strip("`"), cu45)
        self.assertIn(RETIRED.strip("`"), archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
