#!/usr/bin/env python3
"""The engineering-sequences design names the retired status-log service."""

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
CU13 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU13 – Ver historial de gestión.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-fix-806-gestion-historial-audit"
    / "design.md"
)
RETIRED = "`ManagementBitacoraService`"
LIVE = "`ManagementStatusLogService`"


class RetiredStatusLogServiceEnglishTest(unittest.TestCase):
    def test_design_cites_the_service_cu13_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu13 = CU13.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED, cu13)
        self.assertNotIn(LIVE.strip("`"), cu13)
        self.assertIn(RETIRED, archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
