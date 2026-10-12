#!/usr/bin/env python3
"""The data dictionary names the live budget item types."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Budget line class. Stored values `NORMAL`, `DESCUENTO`, and `RECARGO` stay. "
    "The default is `NORMAL`."
)
RETIRED = "Budget line class (default `NORMAL`)"


class ItemTypeLabelsEnglishTest(unittest.TestCase):
    def test_item_type_names_the_live_labels(self):
        for path in (DICTIONARY, ENTITIES):
            text = path.read_text(encoding="utf-8")
            self.assertIn(LIVE, text, path.name)
            self.assertNotIn(RETIRED, text, path.name)


if __name__ == "__main__":
    unittest.main()
