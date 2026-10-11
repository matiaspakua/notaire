#!/usr/bin/env python3
"""The living substitution spec names the plain management update."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVING = (
    ROOT
    / "docs"
    / "openspec"
    / "specs"
    / "gestion-asignacion-suplencia"
    / "spec.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-feat-805-suplencia-assignment"
    / "specs"
    / "gestion-asignacion-suplencia"
    / "spec.md"
)
DESIGN = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "docs-921-engineering-sequences"
    / "design.md"
)
USE_CASE = (
    ROOT / "docs" / "100-business" / "102-use-cases" / "CU22 – Registrar Suplencia.md"
)
RETIRED = "`PUT /api/v1/gestiones`"
LIVE = "`PUT /api/v1/managements/{id}`"


class FlatManagementPutEnglishTest(unittest.TestCase):
    def test_living_spec_names_the_plain_update(self):
        text = LIVING.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertNotIn("PUT /api/v1/gestiones", text)

    def test_design_cites_the_path_cu22_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = USE_CASE.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn("`gestion-asignacion-suplencia`", text)
        self.assertIn(RETIRED, use_case)
        self.assertIn(RETIRED, archived)
        self.assertNotIn(LIVE, archived)


if __name__ == "__main__":
    unittest.main()
