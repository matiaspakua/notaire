#!/usr/bin/env python3
"""Leftover Spanish fixture words and the deed-search query name are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class LeftoverFixtureWordsEnglishTest(unittest.TestCase):
    def test_payment_and_path_words_are_english(self):
        amounts = read(
            "backend-api/src/test/java/com/licensis/notaire/adapter/out/persistence/payment/"
            "PaymentPersistenceAdapterTest.java"
        )
        self.assertIn("amountsToPay", amounts)
        self.assertNotIn("importesAPay", amounts)

        notes = read(
            "backend-api/src/test/java/com/licensis/notaire/adapter/in/web/payment/"
            "PaymentControllerTest.java"
        )
        self.assertIn("Test payment", notes)
        self.assertNotIn("Test pago", notes)

        search = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/SimpleControllersTest.java"
        )
        self.assertIn("/api/v1/deeds/search?number=10", search)
        self.assertNotIn("?numero=", search)

        edge = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "EdgeCaseBoundaryConditionsTest.java"
        )
        self.assertIn("/api/v1/managements/not-a-number", edge)
        self.assertNotIn("not-a-numero", edge)

        debt = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/"
            "ManagementArchiveDebtServiceTest.java"
        )
        self.assertIn("Total debt = 40000", debt)
        self.assertNotIn("deuda", debt)
        self.assertIn("Archivada", debt)

    def test_synthetic_trace_and_document_names_are_english(self):
        trace = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "WorkflowTraceApiH2IntegrationTest.java"
        )
        self.assertIn("/api/v1/managements/number/{number}", trace)
        self.assertNotIn("{numero}", trace)
        self.assertIn("Trace Started", trace)
        self.assertIn("Trace In Progress", trace)
        self.assertIn("Trace Closed", trace)
        self.assertNotIn("Iniciada", trace)
        self.assertNotIn("En Curso", trace)
        self.assertNotIn("Cerrada", trace)
        self.assertNotIn("enCurso", trace)
        self.assertNotIn("iniciada", trace)
        self.assertNotIn("cerrada", trace)

        documents = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "ManagementExternalEntityDocumentsIntegrationTest.java"
        )
        self.assertIn("Ownership report", documents)
        self.assertIn("Debt clearance", documents)
        self.assertNotIn("Informe de Dominio", documents)
        self.assertNotIn("Libre de Deuda", documents)


if __name__ == "__main__":
    unittest.main()
