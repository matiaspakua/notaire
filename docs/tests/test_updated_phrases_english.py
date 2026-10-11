#!/usr/bin/env python3
"""Leftover 'actualizado' phrases and the notebook result name are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (JAVA / relative).read_text(encoding="utf-8")


class UpdatedPhrasesEnglishTest(unittest.TestCase):
    def test_updated_names_are_english(self):
        documents = read("integration/DocumentTypeReferentialIntegrityTest.java")
        self.assertIn("Updated name", documents)
        self.assertIn("Updated name ok", documents)
        self.assertNotIn("actualizado", documents)

        procedures = read("integration/ProcedureTypeReferentialIntegrityTest.java")
        self.assertIn("Updated name", procedures)
        self.assertIn("Updated name ok", procedures)
        self.assertNotIn("actualizado", procedures)

        items = read("unit/ItemControllerTest.java")
        self.assertIn("Updated item", items)
        self.assertNotIn("actualizado", items)

        payments = read("integration/PaymentRepositoryIntegrationTest.java")
        self.assertIn("Updated payment", payments)
        self.assertNotIn("actualizado", payments)

        roles = read("integration/RoleRepositoryIntegrationTest.java")
        self.assertIn("Updated description", roles)
        self.assertNotIn("actualizada", roles)

        managements = read("integration/ManagementControllerIntegrationTest.java")
        self.assertIn("Updated management IT", managements)
        self.assertNotIn("actualizada", managements)

    def test_deed_bodies_and_notebook_result_are_english(self):
        movement = read("integration/TestimonyMovementControllerIntegrationTest.java")
        self.assertIn("Signed deed for a testimony movement", movement)
        self.assertIn('"Firmada"', movement)
        self.assertIn("createSignedDeed", movement)
        self.assertNotIn("firmada", movement)
        self.assertNotIn("para ", movement)

        copy = read("integration/TestimonyCopyReportIntegrationTest.java")
        self.assertIn("Signed deed for a testimony copy", copy)
        self.assertIn('"Firmada"', copy)
        self.assertIn("createSignedDeed", copy)
        self.assertNotIn("firmada", copy)
        self.assertNotIn("para ", copy)

        notebooks = read("integration/NotebookControllerTest.java")
        self.assertIn("MvcResult created", notebooks)
        self.assertNotIn("creado", notebooks)


if __name__ == "__main__":
    unittest.main()
