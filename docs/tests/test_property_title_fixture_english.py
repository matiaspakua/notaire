#!/usr/bin/env python3
"""The synthetic document type in the procedure-template unit fixture is English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
UNIT = "frontend/src/tests/unit/procedure-template-admin.test.tsx"


class PropertyTitleFixtureEnglishTest(unittest.TestCase):
    def test_synthetic_document_name_is_english(self):
        unit = (ROOT / UNIT).read_text(encoding="utf-8")
        self.assertIn("Property title", unit)
        self.assertNotIn("Título de propiedad", unit)
        self.assertIn("Libre Deuda Municipal", unit)
        self.assertIn("Informe de Dominio", unit)
        self.assertIn('"Cliente"', unit)
        self.assertIn("Compraventa", unit)
        self.assertIn("Administración →", unit)


if __name__ == "__main__":
    unittest.main()
