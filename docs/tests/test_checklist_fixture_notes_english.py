#!/usr/bin/env python3
"""Synthetic checklist notes and created names are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class ChecklistFixtureNotesEnglishTest(unittest.TestCase):
    def test_checklist_notes_are_english(self):
        e2e = read("testing/e2e/tests/TS-0142-procedure-required-documents.spec.ts")
        self.assertIn("Procedure checklist E2E", e2e)
        self.assertIn("Document checklist E2E", e2e)
        self.assertIn("Original and copy", e2e)
        self.assertNotIn("Trámite checklist", e2e)
        self.assertNotIn("Documento checklist", e2e)
        self.assertNotIn("Original y copia", e2e)
        self.assertIn("Administración → Trámites", e2e)
        self.assertIn("Documentos requeridos", e2e)
        self.assertIn("Documentos necesarios", e2e)
        self.assertIn('"Cliente"', e2e)

        unit = read("frontend/src/tests/unit/procedure-template-admin.test.tsx")
        self.assertIn("Certified copy", unit)
        self.assertIn("Original and copy", unit)
        self.assertNotIn("Copia certificada", unit)
        self.assertNotIn("Original y copia", unit)
        self.assertIn("Administración →", unit)

        java = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "ProcedureTemplateAdminIntegrationTest.java"
        )
        self.assertIn("Original and copy", java)
        self.assertNotIn("Original y copia", java)
        self.assertIn("Cliente", java)

        bruno = read("backend-api/api-test/procedure-templates/06-update.yml")
        self.assertIn("Original and copy", bruno)
        self.assertNotIn("Original y copia", bruno)

    def test_folio_type_names_are_english(self):
        smoke = read("testing/e2e/tests/TS-0024-admin-module-smoke.spec.ts")
        self.assertIn("FolioTypeTest", smoke)
        self.assertIn("AuxiliaryFolioTypeTest", smoke)
        self.assertNotIn("TipoTest", smoke)
        self.assertNotIn("TipoAuxiliarTest", smoke)
        self.assertIn("tipos de trámite", smoke)
        self.assertIn("Protocolo Auxiliar", smoke)


if __name__ == "__main__":
    unittest.main()
