#!/usr/bin/env python3
"""Synthetic notes, headings, and the management-number alias are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TEST = ROOT / "backend-api/src/test/java/com/licensis/notaire"
E2E = ROOT / "testing/e2e/tests"


def read(relative: str) -> str:
    return (TEST / relative).read_text(encoding="utf-8")


class SyntheticNotesEnglishTest(unittest.TestCase):
    def test_document_and_budget_fixtures_are_english(self):
        partial = read("integration/SubmittedDocumentPartialUpdateIntegrationTest.java")
        self.assertIn("New name", partial)
        self.assertNotIn("Nombre nuevo", partial)

        listed = read("integration/SubmittedDocumentControllerTest.java")
        self.assertIn("Listed document", listed)
        self.assertIn("shouldListSubmittedDocuments", listed)
        self.assertIn("listing submitted documents", listed)
        self.assertNotIn("listado", listed)
        self.assertNotIn("presentados", listed)

        hooks = (ROOT / "frontend/src/hooks/useSubmittedDocuments.test.tsx").read_text(encoding="utf-8")
        self.assertIn("submitted-document hooks", hooks)
        self.assertNotIn("presentados", hooks)

        budgets = read("integration/BudgetPersonAssociationPgIntegrationTest.java")
        self.assertIn("without client", budgets)
        self.assertIn("with client", budgets)
        self.assertIn("without listed client", budgets)
        self.assertNotIn("sin client", budgets)
        self.assertNotIn("con client", budgets)
        self.assertNotIn("listado", budgets)

    def test_missing_item_notes_are_english(self):
        testimony = read("integration/TestimonyControllerIntegrationTest.java")
        self.assertIn("Missing grantor signature", testimony)
        self.assertNotIn("Falta", testimony)
        self.assertNotIn("otorgante", testimony)

        verify = read("unit/TestimonyVerificationServiceTest.java")
        self.assertIn("Missing a signature", verify)
        self.assertNotIn("Falta", verify)

        service = read("unit/RegistrationDraftServiceTest.java")
        self.assertIn("Missing survey plan", service)
        self.assertNotIn("Falta", service)
        self.assertNotIn("mensura", service)

        controller = read("unit/RegistrationDraftControllerTest.java")
        self.assertIn("Missing plan", controller)
        self.assertNotIn("Falta", controller)

        for name, english in (
            ("TS-0012-documentation-testimony-workflow.spec.ts", "Missing grantor signature"),
            ("TS-0031-testimony-generation-verification-feature.spec.ts", "Missing grantor signature"),
            ("TS-0082-registration-draft-feature.spec.ts", "Missing cadastral certificate"),
            ("TS-0032-testimony-registration-feature.spec.ts", "Missing registry seal"),
        ):
            text = (E2E / name).read_text(encoding="utf-8")
            self.assertIn(english, text)
            self.assertNotIn("Falta", text)

        cadastral = (E2E / "TS-0090-demo-two-full-cases.spec.ts").read_text(encoding="utf-8")
        self.assertIn("nomenclatura catastral", cadastral)

    def test_query_alias_and_hero_fixture_are_english(self):
        controller = (ROOT / "backend-api/src/main/java/com/licensis/notaire/jpa/DeedManagementJpaController.java").read_text(encoding="utf-8")
        self.assertIn("as latestManagement", controller)
        self.assertNotIn("ultimaGestion", controller)

        hero = (ROOT / "frontend/src/tests/unit/dashboard-hero.test.tsx").read_text(encoding="utf-8")
        self.assertIn("findLatestTracedManagement (#1347)", hero)
        self.assertIn("Case ${id}", hero)
        self.assertIn("#1029 — Case 29", hero)
        self.assertNotIn("TracedGestion", hero)
        self.assertNotIn("Caso", hero)

        historical = read("integration/ManagementDocumentReentryPgIntegrationTest.java")
        self.assertIn("submitted_documents", historical)
        self.assertNotIn("documentos_presentados", historical)


if __name__ == "__main__":
    unittest.main()
