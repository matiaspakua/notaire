#!/usr/bin/env python3
"""The data dictionary keeps the stored folio-type name Testimonio."""

import unittest
from pathlib import Path

MODEL = (
    Path(__file__).resolve().parents[1]
    / "200-architecture"
    / "205-data-model"
)
DICTIONARY = MODEL / "Diccionario de Datos.md"
ERD = MODEL / "ERD" / "Modelo Relacional Escribania - Entidades.csv"
STORED = "`Protocolo Principal`, `Protocolo Auxiliar`, and `Testimonio` stay"


class DictionaryFolioTypeNamesTest(unittest.TestCase):
    def test_stored_folio_type_name_stays_testimonio(self):
        text = DICTIONARY.read_text(encoding="utf-8")
        erd = ERD.read_text(encoding="utf-8")
        self.assertIn(STORED, text)
        self.assertIn(STORED, erd)
        self.assertIn("Testimony issued from a deed", text)
        self.assertNotIn("`Testimony` stay", text)
        self.assertNotIn("`Testimony` stay", erd)


if __name__ == "__main__":
    unittest.main()
