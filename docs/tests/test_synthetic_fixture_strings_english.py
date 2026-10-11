#!/usr/bin/env python3
"""Synthetic Java test fixtures use English names. Stored labels stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TEST = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (TEST / relative).read_text(encoding="utf-8")


class SyntheticFixtureStringsEnglishTest(unittest.TestCase):
    def test_notes_and_descriptions_are_english(self):
        transition = read("integration/WorkflowTransitionIntegrationTest.java")
        self.assertIn('"Updated"', transition)
        self.assertNotIn("Actualizado", transition)

        deed = read("unit/DeedEntityTest.java")
        self.assertIn("Pending signature", deed)
        self.assertNotIn("Pendiente de firma", deed)

        service = read("integration/SubstitutionServiceIntegrationTest.java")
        self.assertIn("Full cycle", service)
        self.assertIn("First semester", service)
        self.assertIn("Extended period", service)
        self.assertIn('"Updated"', service)
        self.assertIn("Substitute Batch", service)
        self.assertIn("Notary substitute", service)
        self.assertNotIn("Ciclo completo", service)
        self.assertNotIn("Primer semestre", service)
        self.assertNotIn("Period extendido", service)
        self.assertNotIn("Actualizado", service)
        self.assertNotIn("Suplente", service)
        self.assertNotIn("Suplantado", service)

        repository = read("integration/SubstitutionRepositoryIntegrationTest.java")
        self.assertIn('"Updated"', repository)
        self.assertIn('"Substitute"', repository)
        self.assertIn('"Substituted"', repository)
        self.assertNotIn("Actualizado", repository)
        self.assertNotIn("Suplente", repository)
        self.assertNotIn("Suplantado", repository)

        management = read("integration/DeedManagementRepositoryIntegrationTest.java")
        self.assertIn('"Updated"', management)
        self.assertNotIn("Actualizado", management)

        controller = read("integration/ManagementControllerIntegrationTest.java")
        self.assertIn("Status IT", controller)
        self.assertNotIn("Estado IT", controller)

    def test_budget_heading_and_concept_names_are_english(self):
        budgets = read("unit/SimpleControllersTest.java")
        self.assertIn("With client", budgets)
        self.assertIn('"Active"', budgets)
        self.assertIn('"Protocol"', budgets)
        self.assertNotIn("Con cliente", budgets)
        self.assertNotIn('"Activo"', budgets)
        self.assertNotIn('"Protocolo"', budgets)

        concepts = read("unit/application/usecase/concept/ConceptServiceTest.java")
        self.assertIn("Notary office", concepts)
        self.assertIn("Honorarios", concepts)
        self.assertNotIn("Notaría", concepts)

        adapter = read("unit/adapter/out/concept/ConceptPersistenceAdapterTest.java")
        self.assertIn("Notary office", adapter)
        self.assertNotIn("Notaría", adapter)

    def test_entity_and_folio_type_fixtures_are_english(self):
        entities = read("unit/EntitiesBasicTest.java")
        self.assertIn('"Active"', entities)
        self.assertIn('"Used"', entities)
        self.assertIn('"Protocol"', entities)
        self.assertIn('"Auxiliary"', entities)
        self.assertNotIn('"Activo"', entities)
        self.assertNotIn('"Usado"', entities)
        self.assertNotIn('"Protocolo"', entities)
        self.assertNotIn('"Auxiliar"', entities)

        folio_types = read("unit/FolioTypeControllerTest.java")
        self.assertIn('"Protocol"', folio_types)
        self.assertIn("Modified protocol", folio_types)
        self.assertIn("Protocolo Auxiliar", folio_types)
        self.assertNotIn('"Protocolo"', folio_types)
        self.assertNotIn("Protocolo Modificado", folio_types)

        catalog = read("integration/CatalogControllersIntegrationTest.java")
        self.assertIn("Special folio", catalog)
        self.assertIn("Folio for special documents", catalog)
        self.assertIn("Document - Updated", catalog)
        self.assertNotIn("Folio Especial", catalog)
        self.assertNotIn("especiales", catalog)
        self.assertNotIn("Actualizado", catalog)

    def test_legacy_budget_status_mapping_keeps_activo(self):
        migration = read("integration/BudgetStatusMigrationIntegrationTest.java")
        self.assertIn('"Activo"', migration)


if __name__ == "__main__":
    unittest.main()
