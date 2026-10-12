#!/usr/bin/env python3
"""The data dictionary names the seeded person sex code."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Legal sex. Stored values `Masculino` and `Femenino` stay. "
    "The seeded sample `M` stays."
)


class PersonSexSeedLabelEnglishTest(unittest.TestCase):
    def test_sex_names_the_seeded_code(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn(
            "| Legal sex. Stored values `Masculino` and `Femenino` stay. |",
            dictionary,
        )
        self.assertNotIn(
            "Text,,,x,Legal sex. Stored values `Masculino` and `Femenino` stay.\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
