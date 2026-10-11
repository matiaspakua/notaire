#!/usr/bin/env python3
"""The engineering-sequences design names the retired amount-to-pay field."""

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
CU27 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU27 – Ingresar nuevo tipo de documento.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-03-costos-documentos-presupuesto"
    / "design.md"
)
RETIRED = "`DocumentoPresentado.importeAPagar`"
LIVE = "`amountToPay`"


class RetiredAmountToPayEnglishTest(unittest.TestCase):
    def test_design_cites_the_field_cu27_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu27 = CU27.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED, cu27)
        self.assertNotIn(LIVE.strip("`"), cu27)
        self.assertIn(RETIRED, archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
