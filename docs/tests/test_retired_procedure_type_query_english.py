#!/usr/bin/env python3
"""The engineering-sequences design names the retired procedure-type query."""

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
CU39 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU39 – Crear Plantilla Presupuesto.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-05-feat-797-presupuesto-desde-plantilla"
    / "proposal.md"
)
RETIRED = "`tipoTramiteId`"
LIVE = "`procedureTypeId`"


class RetiredProcedureTypeQueryEnglishTest(unittest.TestCase):
    def test_design_cites_the_query_cu39_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu39 = CU39.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED.strip("`"), cu39)
        self.assertNotIn(LIVE.strip("`"), cu39)
        self.assertIn(RETIRED.strip("`"), archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
