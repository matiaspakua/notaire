#!/usr/bin/env python3
"""The engineering-sequences design names the retired validation detail."""

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
CU20 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU20 – Dar alta usuario.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-fix-1054-api-error-toasts"
    / "design.md"
)
RETIRED = "`campo: mensaje`"
LIVE = "`field: message`"


class RetiredValidationDetailEnglishTest(unittest.TestCase):
    def test_design_cites_the_detail_cu20_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu20 = CU20.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED, cu20)
        self.assertNotIn(LIVE.strip("`"), cu20)
        self.assertIn(LIVE.strip("`"), archived)
        self.assertNotIn(RETIRED.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
