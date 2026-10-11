#!/usr/bin/env python3
"""The case schema names the retired case-create path."""

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
EVALUATION = ROOT / "docs" / "100-business" / "EVALUACION-DOCUMENTACION.md"
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-22-translate-domain-schema-to-english"
    / "design.md"
)
RETIRED = "`POST /api/v1/gestion`"
RETIRED_TABLE = "`gestiones_de_escrituras`"
LIVE = "`POST /api/v1/managements/complete-case`"
LIVE_TABLE = "deed_managements"


class RetiredCaseCreateEnglishTest(unittest.TestCase):
    def test_case_schema_names_the_live_create(self):
        text = GESTION.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertIn(LIVE_TABLE, text)
        self.assertNotIn("POST /api/v1/gestion`", text)
        self.assertNotIn(RETIRED_TABLE, text)

    def test_design_cites_the_path_the_evaluation_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        evaluation = EVALUATION.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(RETIRED, text)
        self.assertIn(RETIRED_TABLE, text)
        self.assertIn("`DOC-GESTION`", text)
        self.assertIn(RETIRED, evaluation)
        self.assertIn(RETIRED_TABLE, evaluation)
        self.assertIn(LIVE, evaluation)
        self.assertIn(LIVE_TABLE, evaluation)
        self.assertIn("gestiones_de_escrituras", archived)
        self.assertNotIn(LIVE_TABLE, archived)


if __name__ == "__main__":
    unittest.main()
