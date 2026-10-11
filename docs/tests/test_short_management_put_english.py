#!/usr/bin/env python3
"""The case schema names the shorter management updates."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GESTION = ROOT / "docs" / "200-architecture" / "203-design" / "GESTION.md"
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
    / "2026-10-03-feat-804-enforce-workflow-transitions"
    / "proposal.md"
)
RETIRED = "`PUT /gestiones/{id}`"
RETIRED_COMPLETE = "`PUT /gestiones/{id}/complete-case`"
LIVE = "`PUT /api/v1/managements/{id}`"
LIVE_COMPLETE = "`PUT /api/v1/managements/{id}/complete-case`"


class ShortManagementPutEnglishTest(unittest.TestCase):
    def test_case_schema_names_the_live_updates(self):
        text = GESTION.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertIn(LIVE_COMPLETE, text)
        self.assertNotIn("PUT /gestiones/{id}", text)

    def test_design_cites_the_paths_cu83_and_cu13_still_name(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu83 = CU83.read_text(encoding="utf-8")
        cu13 = CU13.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(RETIRED_COMPLETE, text)
        self.assertIn("`DOC-GESTION`", text)
        self.assertIn(RETIRED, cu83)
        self.assertIn(RETIRED_COMPLETE, cu83)
        self.assertIn(RETIRED, cu13)
        self.assertIn(RETIRED_COMPLETE, cu13)
        self.assertIn(RETIRED, archived)
        self.assertIn(RETIRED_COMPLETE, archived)
        self.assertNotIn(LIVE, archived)
        self.assertNotIn(LIVE_COMPLETE, archived)


if __name__ == "__main__":
    unittest.main()
