#!/usr/bin/env python3
"""The engineering-sequences design names the retired deed link field."""

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
CU87 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU87 – Vincular Escritura a Folio y Copia a Testimonio.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-08-folio-vinculacion-escritura"
    / "design.md"
)
RETIRED = "`escrituraId`"
LIVE = "`deedId`"


class RetiredDeedLinkFieldEnglishTest(unittest.TestCase):
    def test_design_cites_the_field_cu87_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu87 = CU87.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED, cu87)
        self.assertNotIn(LIVE.strip("`"), cu87)
        self.assertIn(RETIRED, archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
