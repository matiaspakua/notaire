#!/usr/bin/env python3
"""Mixed Spanish phrases in fixtures match the English live contract."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (JAVA / relative).read_text(encoding="utf-8")


class MixedPhrasesEnglishTest(unittest.TestCase):
    def test_item_fixtures_are_english(self):
        controller = read("unit/ItemControllerTest.java")
        self.assertIn("Discount without reason", controller)
        self.assertIn("Discount for early payment", controller)
        self.assertIn("DESCUENTO", controller)
        self.assertNotIn("Descuento", controller)
        self.assertNotIn("sin ", controller)
        self.assertNotIn("pronto", controller)

        service = read("unit/ItemServiceTest.java")
        self.assertIn("Item without a type", service)
        self.assertIn("Discount for early payment", service)
        self.assertIn("Surcharge for late payment", service)
        self.assertIn('"Discount"', service)
        self.assertIn('"Surcharge"', service)
        self.assertIn("DESCUENTO", service)
        self.assertIn("RECARGO", service)
        self.assertNotIn("Descuento", service)
        self.assertNotIn("Recargo", service)
        self.assertNotIn("mora", service)
        self.assertNotIn("sin ", service)
        self.assertNotIn("pronto", service)

    def test_notes_and_not_found_are_english(self):
        testimony = read("integration/TestimonyControllerIntegrationTest.java")
        self.assertIn("Verified by the notary", testimony)
        self.assertNotIn("por ", testimony)

        draft = read("unit/RegistrationDraftControllerTest.java")
        self.assertIn("Deed not found with ID: 999", draft)
        self.assertNotIn("No existe", draft)
        self.assertNotIn("con ID", draft)

        initializer = read("unit/DataInitializerTest.java")
        self.assertIn("hash-already-rotated-by-the-operator", initializer)
        self.assertIn("default admin user", initializer)
        self.assertNotIn("rotado", initializer)
        self.assertNotIn("operador", initializer)
        self.assertNotIn("defecto", initializer)


if __name__ == "__main__":
    unittest.main()
