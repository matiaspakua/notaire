#!/usr/bin/env python3
"""Synthetic person labels in tests use English. Stored roles and toast matches stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class SyntheticPersonLabelsEnglishTest(unittest.TestCase):
    def test_substitution_person_names_are_english(self):
        controller = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "SubstitutionControllerIntegrationTest.java"
        )
        self.assertIn('"Substituted IT"', controller)
        self.assertNotIn("Suplantado", controller)

        redirect = read("testing/e2e/tests/TS-0092-management-substitution-redirect.spec.ts")
        self.assertIn("`Notary${", redirect)
        self.assertIn("`Substituted${", redirect)
        self.assertIn("`Substitute${", redirect)
        self.assertIn("`Client${", redirect)
        self.assertIn("`NoSubstitution${", redirect)
        self.assertIn("`ClientNorm${", redirect)
        self.assertIn("const today", redirect)
        self.assertIn("const tomorrow", redirect)
        self.assertIn("/suplente/i", redirect)
        self.assertNotIn("Escribano", redirect)
        self.assertNotIn("Suplantado", redirect)
        self.assertNotIn("Suplente", redirect)
        self.assertNotIn("Cliente", redirect)
        self.assertNotIn("SinSuplencia", redirect)
        self.assertNotIn("const hoy", redirect)
        self.assertNotIn("manana", redirect)

        edit = read("testing/e2e/tests/TS-0126-management-full-edit.spec.ts")
        self.assertIn("`Absent1360${", edit)
        self.assertIn("`Substitute1360${", edit)
        self.assertIn("/suplente/i", edit)
        self.assertNotIn("Ausente", edit)
        self.assertNotIn("Suplente", edit)

    def test_search_and_payment_person_labels_are_english(self):
        people = read("testing/e2e/tests/TS-0015-people-clients-workflow.spec.ts")
        self.assertIn("`Client${", people)
        self.assertIn("`IsClient${", people)
        self.assertIn("`SearchLastName${", people)
        self.assertIn("'SearchLastName'", people)
        self.assertIn("`SearchDni${", people)
        self.assertIn("Lucía", people)
        self.assertNotIn("Cliente", people)
        self.assertNotIn("BuscaApellido", people)
        self.assertNotIn("BuscaDni", people)

        payments = read("testing/e2e/tests/TS-0014-payments-workflow.spec.ts")
        self.assertIn("lastName: lastName1", payments)
        self.assertIn("lastName: lastName2", payments)
        self.assertNotIn("apellido", payments)


if __name__ == "__main__":
    unittest.main()
