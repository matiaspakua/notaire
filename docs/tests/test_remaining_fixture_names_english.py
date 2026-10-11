#!/usr/bin/env python3
"""Remaining synthetic fixture names and test methods are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TEST = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (TEST / relative).read_text(encoding="utf-8")


class RemainingFixtureNamesEnglishTest(unittest.TestCase):
    def test_catalog_and_search_names_are_english(self):
        catalog = read("integration/CatalogControllersIntegrationTest.java")
        self.assertIn("Archived management", catalog)
        self.assertIn("Initial - Modified", catalog)
        self.assertIn('"Updated"', catalog)
        self.assertIn("Notarial power", catalog)
        self.assertIn("Grantor", catalog)
        self.assertIn("shouldReturnAllManagementStatuses", catalog)
        self.assertIn('"Archivada"', catalog)
        self.assertNotIn("archivada", catalog)
        self.assertNotIn("Modificada", catalog)
        self.assertNotIn("Actualizada", catalog)
        self.assertNotIn("Poder Notarial", catalog)
        self.assertNotIn("Mandante", catalog)
        self.assertNotIn("EstadosDeManagement", catalog)

        concepts = read("integration/ConceptReferentialIntegrityTest.java")
        self.assertIn("FindThisConcept", concepts)
        self.assertNotIn("BuscarEste", concepts)

        statuses = read("integration/ManagementStatusReferentialIntegrityTest.java")
        self.assertIn("FindThisStatus", statuses)
        self.assertIn("shouldSearchStatusesByName", statuses)
        self.assertNotIn("BuscarEste", statuses)
        self.assertNotIn("shouldSearchEstadosByName", statuses)

    def test_procedure_and_document_names_are_english(self):
        procedures = read("integration/ProcedureTypeReferentialIntegrityTest.java")
        self.assertIn("Procedure_unused_", procedures)
        self.assertIn("SearchableProcedure", procedures)
        self.assertNotIn("Tramite", procedures)

        documents = read("integration/SubmittedDocumentRequestValidationIntegrationTest.java")
        self.assertIn("Document #655", documents)
        self.assertNotIn("Documento", documents)

        templates = read("integration/BudgetTemplateRepositoryIntegrationTest.java")
        self.assertIn("Updated notes", templates)
        self.assertNotIn("Observaciones", templates)

        serialized = read("integration/BudgetTemplateSerializationIntegrationTest.java")
        self.assertIn("Concept Cycle IT", serialized)
        self.assertIn("Procedure Cycle IT", serialized)
        self.assertNotIn("Ciclo", serialized)

        cycle = read("integration/ProcedureSerializationIntegrationTest.java")
        self.assertIn("Cycle IT Status", cycle)
        self.assertIn("Management Cycle IT", cycle)
        self.assertNotIn("Ciclo", cycle)

    def test_status_and_person_fixtures_are_english(self):
        history = read("integration/ManagementHistoryOrphanWriteIntegrationTest.java")
        self.assertIn("Status from history", history)
        self.assertIn("Status fallback", history)
        self.assertIn("shouldReturnCurrentStatusFromHistoryWhenRowsExist", history)
        self.assertIn("shouldReturnCurrentStatusFallbackWhenHistoryEmpty", history)
        self.assertIn("shouldReturn404CurrentStatusWhenManagementMissing", history)
        self.assertIn("shouldReturn404CurrentStatusWhenStatusNullAndHistoryEmpty", history)
        self.assertNotIn("Estado", history)

        archive = read("integration/ManagementArchiveIntegrationTest.java")
        self.assertIn("Status Archive IT ", archive)
        self.assertIn("ManagementStatus archived", archive)
        self.assertIn('"Archivada"', archive)
        self.assertNotIn("Estado", archive)
        self.assertNotIn("archivada", archive)

        workflow = read("unit/WorkflowValidationServiceTest.java")
        self.assertIn('"Status " + id', workflow)
        self.assertNotIn("Estado", workflow)

        missing = read("unit/AdditionalControllersTest.java")
        self.assertIn("Missing status", missing)
        self.assertNotIn("Estado Inexistente", missing)

        substitute = read("integration/SubstitutionControllerIntegrationTest.java")
        self.assertIn("Substitute IT", substitute)
        self.assertNotIn("Suplente", substitute)

        notary = read("unit/ExternalEntityDocumentServiceTest.java")
        self.assertIn("Ana Notary", notary)
        self.assertNotIn("Notaria", notary)

        catalog_workflow = read("integration/BusinessWorkflowIntegrationTest.java")
        self.assertIn("getAllStatusesReturnsArray", catalog_workflow)
        self.assertNotIn("getAllEstadosReturnsArray", catalog_workflow)

        log = read("unit/ManagementStatusLogServiceTest.java")
        self.assertIn("ManagementStatus archived", log)
        self.assertIn('"Archivada"', log)
        self.assertNotIn("archivada", log)


if __name__ == "__main__":
    unittest.main()
