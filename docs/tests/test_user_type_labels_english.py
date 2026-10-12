#!/usr/bin/env python3
"""The data dictionary names the live user-type labels."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Legacy descriptive role. Stored values `Escribano`, `Empleado`, `EMPLEADO`, "
    "`ADMIN`, `ADMINISTRADOR`, and `ESCRIBANO` stay."
)


class UserTypeLabelsEnglishTest(unittest.TestCase):
    def test_user_type_names_the_live_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn("| Legacy descriptive role |", dictionary)
        self.assertNotIn("Text,,,,Legacy descriptive role", entities)


if __name__ == "__main__":
    unittest.main()
