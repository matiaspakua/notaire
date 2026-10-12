#!/usr/bin/env python3
"""Synthetic person occupations are English. The stored user type stays."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PERSON = (
    ROOT
    / "backend-api/src/test/java/com/licensis/notaire/unit/PersonEntityTest.java"
)
HELPERS = ROOT / "testing/e2e/tests/gherkin-helpers.ts"
CONSTANTS = (
    ROOT
    / "backend-api/src/main/java/com/licensis/notaire/business/BusinessConstants.java"
)


class SyntheticOccupationEnglishTest(unittest.TestCase):
    def test_person_fixture_occupation_is_english(self):
        text = PERSON.read_text(encoding="utf-8")
        self.assertIn('person.setOccupation("Clerk")', text)
        self.assertIn('assertThat(person.getOccupation()).isEqualTo("Clerk")', text)
        self.assertNotIn('setOccupation("empleada")', text)
        self.assertIn('setMaritalStatus("soltero")', text)
        self.assertIn('setSex("femenino")', text)
        self.assertNotIn("UserEmployee", text)

    def test_gherkin_occupation_is_not_the_stored_user_type(self):
        text = HELPERS.read_text(encoding="utf-8")
        self.assertIn('occupation: "Clerk"', text)
        self.assertNotIn('occupation: "Empleado"', text)
        self.assertIn('type: "EMPLEADO"', text)
        self.assertIn('maritalStatus: "Soltero"', text)
        self.assertIn('sex: "Masculino"', text)
        constants = CONSTANTS.read_text(encoding="utf-8")
        self.assertIn('UserEmployee = "Empleado"', constants)


if __name__ == "__main__":
    unittest.main()
