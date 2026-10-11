#!/usr/bin/env python3
"""Helper names, the stamp-duty fixture, and the amount test id are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
API = ROOT / "backend-api/src"


def read(relative: str) -> str:
    return (API / relative).read_text(encoding="utf-8")


class HelperNamesEnglishTest(unittest.TestCase):
    def test_helper_names_are_english(self):
        deed = read("test/java/com/licensis/notaire/service/unit/DeedServiceTest.java")
        self.assertIn("folioWithNotary", deed)
        self.assertIn("shouldRejectSaveWhenGapIsNotJustified", deed)
        self.assertNotIn("folioConNotary", deed)
        self.assertNotIn("Salto", deed)

        archive = read("test/java/com/licensis/notaire/integration/ManagementArchiveIntegrationTest.java")
        self.assertIn("createProcedureTypeWithWorkflowToArchived", archive)
        self.assertIn("WorkflowTransition transition", archive)
        self.assertNotIn("Hacia", archive)
        self.assertNotIn("transicion", archive)
        self.assertIn("Archivada", archive)
        self.assertNotIn("nuevo", archive)

        template = read("test/java/com/licensis/notaire/integration/BudgetTemplateControllerIntegrationTest.java")
        self.assertIn("createProcedureTypeWithTemplate", template)
        self.assertIn("String conceptName", template)
        self.assertIn("Stamp duty IT", template)
        self.assertIn("Honorarios IT", template)
        self.assertNotIn("ConTemplate", template)
        self.assertNotIn("nameConcept", template)
        self.assertNotIn("Sellado", template)

        concept = read("main/java/com/licensis/notaire/jpa/ConceptJpaController.java")
        self.assertIn("String conceptName", concept)
        self.assertNotIn("nameConcept", concept)

    def test_stamp_duty_fixture_and_amount_testid_are_english(self):
        for relative in (
            "test/java/com/licensis/notaire/unit/SimpleControllersTest.java",
            "test/java/com/licensis/notaire/unit/BudgetCatalogItemsServiceTest.java",
            "test/java/com/licensis/notaire/unit/BudgetTemplateServiceTest.java",
        ):
            source = read(relative)
            self.assertIn("Stamp duty", source, relative)
            self.assertNotIn("Sellado", source, relative)

        catalog = read("test/java/com/licensis/notaire/unit/BudgetCatalogItemsServiceTest.java")
        self.assertIn("Honorarios", catalog)

        page = (ROOT / "frontend/src/app/dashboard/external-entity-documents/page.tsx").read_text(
            encoding="utf-8"
        )
        self.assertIn('data-testid="input-amount-to-pay"', page)
        self.assertIn('t("fields.importeAPagar")', page)
        self.assertNotIn("importe-a-pagar", page)


if __name__ == "__main__":
    unittest.main()
