#!/usr/bin/env python3
"""The engineering-sequences design names the retired procedure type."""

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
    / "000-archive"
    / "speckit"
    / "specs"
    / "archive"
    / "001-cu03-documentos-necesarios"
    / "plan.md"
)
RETIRED = "`TipoDeTramite`"
LIVE = "`ProcedureType`"


class RetiredProcedureTypeEnglishTest(unittest.TestCase):
    def test_design_cites_the_type_cu83_and_cu39_still_name(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu83 = CU83.read_text(encoding="utf-8")
        cu39 = CU39.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(LIVE, text)
        self.assertIn(RETIRED, cu83)
        self.assertNotIn(LIVE.strip("`"), cu83)
        self.assertIn(RETIRED, cu39)
        self.assertNotIn(LIVE.strip("`"), cu39)
        self.assertIn(RETIRED, archived)
        self.assertNotIn(LIVE.strip("`"), archived)


if __name__ == "__main__":
    unittest.main()
