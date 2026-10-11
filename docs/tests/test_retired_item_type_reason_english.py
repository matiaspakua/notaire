#!/usr/bin/env python3
"""The engineering-sequences design names the retired item type and reason."""

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
    / "2026-09-02-descuentos-recargos-presupuesto"
    / "tasks.md"
)
RETIRED = ("`tipo`", "`motivo`")
LIVE = ("`type`", "`reason`")


class RetiredItemTypeReasonEnglishTest(unittest.TestCase):
    def test_design_cites_the_fields_cu71_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu71 = CU71.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        for token in RETIRED + LIVE:
            self.assertIn(token, text)
        for token in RETIRED:
            self.assertIn(token, cu71)
            self.assertIn(token, archived)
        for token in LIVE:
            self.assertNotIn(token.strip("`"), cu71)
            self.assertNotIn(token, archived)


if __name__ == "__main__":
    unittest.main()
