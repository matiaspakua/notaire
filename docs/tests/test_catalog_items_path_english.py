#!/usr/bin/env python3
"""The living catalog-items spec names the live add path."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVING = (
    ROOT / "docs" / "openspec" / "specs" / "presupuesto-catalogo-items" / "spec.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-07-presupuesto-plantillas-y-catalogo-items"
    / "proposal.md"
)
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
RETIRED = "`POST /api/v1/presupuestos/{id}/items-desde-catalogo`"
LIVE = "`POST /api/v1/budgets/{id}/items-from-catalog`"


class CatalogItemsPathEnglishTest(unittest.TestCase):
    def test_living_spec_names_the_catalog_path(self):
        text = LIVING.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertNotIn("items-desde-catalogo", text)

    def test_archived_proposal_keeps_the_retired_path(self):
        text = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn("items-desde-catalogo", text)
        self.assertNotIn("items-from-catalog", text)

    def test_design_cites_the_path_cu71_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU71.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(RETIRED.strip("`"), use_case)
        self.assertIn("`presupuesto-catalogo-items`", text)


if __name__ == "__main__":
    unittest.main()
