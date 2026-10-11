#!/usr/bin/env python3
"""The engineering-sequences design names the retired reentered flag."""

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
CU43 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU43 – Reingresar documentación.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "000-archive"
    / "speckit"
    / "specs"
    / "archive"
    / "003-cu43-reingresar-documentacion"
    / "plan.md"
)
RETIRED = "`reingresado=true`"
LIVE = "`reentered`"


class RetiredReenteredFlagEnglishTest(unittest.TestCase):
    def test_design_cites_the_flag_cu43_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu43 = CU43.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED, cu43)
        self.assertNotIn(LIVE.strip("`"), cu43)
        self.assertIn(RETIRED, archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
