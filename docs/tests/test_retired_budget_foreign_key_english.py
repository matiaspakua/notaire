#!/usr/bin/env python3
"""The engineering-sequences design names the retired budget foreign key."""

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
CU71 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU71 – Gestión de Items.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-07-presupuesto-plantillas-y-catalogo-items"
    / "design.md"
)
RETIRED = "`fk_id_presupuesto`"
LIVE = "`fk_id_budget`"


class RetiredBudgetForeignKeyEnglishTest(unittest.TestCase):
    def test_design_cites_the_column_cu71_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu71 = CU71.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED, cu71)
        self.assertNotIn(LIVE.strip("`"), cu71)
        self.assertIn(RETIRED, archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
