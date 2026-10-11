#!/usr/bin/env python3
"""The engineering-sequences design names the retired management status type."""

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
CU83 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU83 – Definir Workflow de Estados y Transiciones.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-07-silent-delete-persistable-fix"
    / "design.md"
)
RETIRED = "`EstadoDeGestion`"
LIVE = "`ManagementStatus`"


class RetiredManagementStatusEnglishTest(unittest.TestCase):
    def test_design_cites_the_type_cu83_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu83 = CU83.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED, cu83)
        self.assertIn(RETIRED, archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
