#!/usr/bin/env python3
"""The engineering-sequences design names the retired submitted-document id."""

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
CU10 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU10 – Registrar movimientos documentación de entidades externas.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "000-archive"
    / "speckit"
    / "specs"
    / "archive"
    / "002-cu10-movimientos-documentacion-entidades-externas"
    / "spec.md"
)
RETIRED = "`idDocumentoPresentado`"
LIVE = "`idSubmittedDocument`"


class RetiredSubmittedDocumentIdEnglishTest(unittest.TestCase):
    def test_design_cites_the_id_cu10_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu10 = CU10.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED.strip("`"), cu10)
        self.assertNotIn(LIVE.strip("`"), cu10)
        self.assertIn(RETIRED.strip("`"), archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
