#!/usr/bin/env python3
"""CU10 comments and nearby display names are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class Cu10CommentsEnglishTest(unittest.TestCase):
    def test_external_entity_comments_are_english(self):
        detail = read(
            "backend-api/src/main/java/com/licensis/notaire/dto/"
            "DtoManagementExternalEntityDocuments.java"
        )
        self.assertIn("course of", detail)
        self.assertIn("events):", detail)
        self.assertIn("start date", detail)
        self.assertIn("notary in", detail)
        self.assertIn("charge,", detail)
        self.assertIn("involves a property", detail)
        self.assertIn("external entities", detail)
        self.assertNotIn("Curso", detail)
        self.assertNotIn("Eventos", detail)
        self.assertNotIn("inicio", detail)
        self.assertNotIn("involucra", detail)
        self.assertNotIn("entidades", detail)
        self.assertNotIn("a cargo", detail)

        movement = read(
            "backend-api/src/main/java/com/licensis/notaire/dto/"
            "DtoExternalEntityDocumentMovement.java"
        )
        self.assertIn("course of events", movement)
        self.assertNotIn("Curso", movement)
        self.assertNotIn("Eventos", movement)

    def test_display_names_are_english(self):
        query = read(
            "backend-api/src/test/java/com/licensis/notaire/service/unit/"
            "ManagementQueryServiceTest.java"
        )
        self.assertIn("Should map start date correctly", query)
        self.assertNotIn("inicio", query)

        receipt = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/"
            "ReportServicePaymentReceiptTest.java"
        )
        self.assertIn("PDF receipt", receipt)
        self.assertNotIn("recibo", receipt)
        self.assertIn("Compraventa deed", receipt)


if __name__ == "__main__":
    unittest.main()
