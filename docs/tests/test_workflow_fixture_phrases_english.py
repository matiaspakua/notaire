#!/usr/bin/env python3
"""Synthetic workflow and person-label fixtures use English. Stored types stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class WorkflowFixturePhrasesEnglishTest(unittest.TestCase):
    def test_transition_fixtures_are_english(self):
        workflow = read("backend-api/src/test/java/com/licensis/notaire/unit/WorkflowEntityTest.java")
        self.assertIn('"Status advance"', workflow)
        self.assertIn("status.documentsComplete == true", workflow)
        self.assertIn("Workflow Compraventa", workflow)
        self.assertNotIn("Avance", workflow)
        self.assertNotIn("documentosCompletos", workflow)

    def test_special_character_query_is_english(self):
        reports = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "ReportsUseCaseIntegrationTest.java"
        )
        self.assertIn("Purchase/Sale (special) & more", reports)
        self.assertNotIn("especial", reports)
        self.assertNotIn("Compra/Venta", reports)

    def test_remaining_person_prefixes_are_english(self):
        people = read("testing/e2e/tests/TS-0015-people-clients-workflow.spec.ts")
        self.assertIn("`Create${", people)
        self.assertIn("`Promote${", people)
        self.assertIn("name: 'First'", people)
        self.assertIn("name: 'Second'", people)
        self.assertIn("fill('NoId')", people)
        self.assertIn("fill('Sample')", people)
        self.assertIn("fill('Company')", people)
        self.assertIn("Lucía", people)
        self.assertIn("María", people)
        self.assertIn("Pedro", people)
        self.assertNotIn("`Alta${", people)
        self.assertNotIn("`Promover${", people)
        self.assertNotIn("Primero", people)
        self.assertNotIn("Segundo", people)
        self.assertNotIn("SinDNI", people)
        self.assertNotIn("Prueba", people)
        self.assertNotIn("'Empresa'", people)

        redirect = read("testing/e2e/tests/TS-0092-management-substitution-redirect.spec.ts")
        self.assertIn("`Edit${", redirect)
        self.assertIn("/suplente/i", redirect)
        self.assertNotIn("`Modif${", redirect)


if __name__ == "__main__":
    unittest.main()
