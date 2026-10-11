#!/usr/bin/env python3
"""The engineering-sequences design names the retired substitution dates."""

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
CU22 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU22 – Registrar Suplencia.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-07-suplencia-efecto-en-gestiones"
    / "design.md"
)
RETIRED = ("`fechaInicio`", "`fechaFin`")
LIVE = ("`dateStart`", "`dateEnd`")


class RetiredSubstitutionDatesEnglishTest(unittest.TestCase):
    def test_design_cites_the_fields_cu22_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu22 = CU22.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        for token in RETIRED + LIVE:
            self.assertIn(token, text)
        for token in RETIRED:
            self.assertIn(token, cu22)
            self.assertIn(token, archived)
        for token in LIVE:
            bare = token.strip("`")
            self.assertNotIn(bare, cu22)
            self.assertNotIn(bare, archived)


if __name__ == "__main__":
    unittest.main()
