#!/usr/bin/env python3
"""The unknown identification-type person fixture uses an English last name."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SPEC = ROOT / "backend-api/api-test/people/08-create-unknown-identification-type.yml"


class UnknownTypePersonFixtureEnglishTest(unittest.TestCase):
    def test_unknown_type_last_name_is_english(self):
        text = SPEC.read_text(encoding="utf-8")
        self.assertIn('"lastName": "Unknown Type E2E"', text)
        self.assertNotIn("Tipo Desconocido", text)
        self.assertIn('"firstName": "Ana"', text)
        self.assertIn("identificationTypeId", text)
        self.assertIn("defaults to DNI", text)
        self.assertIn("to.equal(400)", text)


if __name__ == "__main__":
    unittest.main()
