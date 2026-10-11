#!/usr/bin/env python3
"""H2 test-seed phrases that are not stored catalog labels are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SEED = ROOT / "backend-api/src/test/resources/data.sql"


class H2SeedPhrasesEnglishTest(unittest.TestCase):
    def test_synthetic_seed_phrases_are_english(self):
        text = SEED.read_text(encoding="utf-8")
        self.assertIn("'Base fee'", text)
        self.assertIn("'Test budget'", text)
        self.assertIn("'Compraventa procedure'", text)
        self.assertIn("required by management, Folio", text)
        self.assertIn("User (administrator)", text)
        self.assertNotIn("Honorario base", text)
        self.assertNotIn("Presupuesto de prueba", text)
        self.assertNotIn("Procedure de compraventa", text)
        self.assertNotIn("required by Gestion", text)
        self.assertNotIn("Administrador", text)

    def test_stored_seed_labels_stay(self):
        text = SEED.read_text(encoding="utf-8")
        self.assertIn("'De documento'", text)
        self.assertIn("'Iniciada'", text)
        self.assertIn("'Inicio Gestion'", text)
        self.assertIn("'Compraventa'", text)
        self.assertIn("'PENDIENTE'", text)
        self.assertIn("'Casado'", text)
        self.assertIn("'Escribano'", text)
        self.assertIn("'DNI'", text)


if __name__ == "__main__":
    unittest.main()
