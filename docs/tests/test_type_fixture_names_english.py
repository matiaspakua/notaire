#!/usr/bin/env python3
"""Synthetic type names and traceability class names are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TEST = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (TEST / relative).read_text(encoding="utf-8")


class TypeFixtureNamesEnglishTest(unittest.TestCase):
    def test_type_fixtures_use_english_names(self):
        documents = read("integration/DocumentTypeReferentialIntegrityTest.java")
        self.assertIn("Type_unused_", documents)
        self.assertNotIn("Tipo", documents)

        submitted = read("integration/SubmittedDocumentRequestValidationIntegrationTest.java")
        self.assertIn("Type #655", submitted)
        self.assertIn("Type procedure #655", submitted)
        self.assertNotIn("Tipo", submitted)

        templates = read("integration/BudgetTemplateRepositoryIntegrationTest.java")
        self.assertIn('"Type 1"', templates)
        self.assertIn('"Type 2"', templates)
        self.assertNotIn("Tipo", templates)

        deeds = read("integration/DeedManagementRepositoryIntegrationTest.java")
        self.assertIn("Type Procedure Test", deeds)
        self.assertNotIn("Tipo", deeds)

        hydration = read("integration/ProcedureControllerFkHydrationIntegrationTest.java")
        self.assertIn("Type FK Hydration", hydration)
        self.assertNotIn("Tipo", hydration)

        summary = read("integration/ManagementCaseSummaryIntegrationTest.java")
        self.assertIn("Case summary type ", summary)
        self.assertIn("Procedure case summary ", summary)
        self.assertIn("Deed case summary", summary)
        self.assertIn("Case summary of a management", summary)
        self.assertIn("Case document", summary)
        self.assertNotIn("Tipo", summary)
        self.assertNotIn("resumen", summary)
        self.assertNotIn("caso", summary)

        expiration = read("integration/UpcomingExpirationControllerIntegrationTest.java")
        self.assertIn("Type procedure CU42 ", expiration)
        self.assertIn("Type CU42 ", expiration)
        self.assertNotIn("Tipo", expiration)

        trace = read("integration/WorkflowTraceApiH2IntegrationTest.java")
        self.assertIn("Type Trace Test", trace)
        self.assertNotIn("Tipo", trace)

        controller = read("integration/SubmittedDocumentControllerTest.java")
        self.assertIn("Type SubmittedDocumentControllerTest ", controller)
        self.assertIn("Procedure SubmittedDocumentControllerTest", controller)
        self.assertNotIn("Tipo", controller)
        self.assertNotIn("DocumentoPresentado", controller)

        partial = read("integration/SubmittedDocumentPartialUpdateIntegrationTest.java")
        self.assertIn("Type with expiration", partial)
        self.assertIn("Type procedure doc", partial)
        self.assertNotIn("Tipo", partial)

        person = read("integration/PersonIdentificationTypeIntegrationTest.java")
        self.assertIn("Type1361", person)
        self.assertIn("Ana", person)
        self.assertNotIn("Tipo", person)

    def test_workflow_and_display_names_are_english(self):
        workflow = read("integration/BusinessWorkflowIntegrationTest.java")
        self.assertIn("class ProcedureTypesWorkflow", workflow)
        self.assertIn("getAllProcedureTypesReturnsArray", workflow)
        self.assertIn("Base fee", workflow)
        self.assertIn("Base fee for the deed", workflow)
        self.assertIn("Compraventa", workflow)
        self.assertNotIn("TiposProcedure", workflow)
        self.assertNotIn("getAllTipos", workflow)
        self.assertNotIn("Honorario", workflow)

        remaining = read("integration/RemainingControllersIntegrationTest.java")
        self.assertIn("IdentificationTypeController", remaining)
        self.assertIn("shouldReturnAllSubmittedDocuments", remaining)
        self.assertIn("Should return all submitted documents", remaining)
        self.assertNotIn("TipoIdentificacion", remaining)
        self.assertNotIn("presentados", remaining)

    def test_traceability_registry_uses_live_class_names(self):
        matrix = read("unit/TraceabilityMatrixTest.java")
        self.assertIn("BudgetEntityTest", matrix)
        self.assertIn("BudgetServiceTest", matrix)
        self.assertIn("DeedManagementEntityTest", matrix)
        self.assertIn("DeedEntityTest", matrix)
        self.assertIn("PaymentEntityTest", matrix)
        self.assertIn("ProcessPaymentServiceTest", matrix)
        self.assertIn("ProcedureTypeWorkflowAssignmentTest", matrix)
        self.assertIn("BudgetTemplateControllerTest", matrix)
        self.assertIn("ProcedureTemplateControllerTest", matrix)
        self.assertIn("AuditRecordServiceTest", matrix)
        self.assertNotIn("Presupuesto", matrix)
        self.assertNotIn("GestionDeEscritura", matrix)
        self.assertNotIn("Escritura", matrix)
        self.assertNotIn("Pago", matrix)
        self.assertNotIn("TipoDeTramite", matrix)
        self.assertNotIn("Plantilla", matrix)
        self.assertNotIn("RegistroAuditoria", matrix)

        hygiene = read("unit/NamedQueryEnglishNamesHygieneTest.java")
        self.assertIn("TipoDeTramite.", hygiene)
        self.assertIn("DocumentoPresentado.", hygiene)

    def test_untyped_person_fixture_is_english(self):
        page = (ROOT / "frontend/src/tests/unit/people-identification-type.test.tsx").read_text(encoding="utf-8")
        self.assertIn('firstName: "No"', page)
        self.assertIn('lastName: "Type"', page)
        self.assertIn('findByText("No Type")', page)
        self.assertIn("tipoIdentificacion", page)
        self.assertIn('"DNI"', page)
        self.assertIn('"Pasaporte"', page)
        self.assertNotIn("Sin Tipo", page)


if __name__ == "__main__":
    unittest.main()
