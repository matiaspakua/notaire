#!/usr/bin/env python3
"""The data dictionary names the live sex and marital-status labels."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
SEX = "Legal sex. Stored values `Masculino` and `Femenino` stay."
MARITAL = "Marital status. Stored values `Soltero` and `Casado` stay."


class PersonDemographicLabelsEnglishTest(unittest.TestCase):
    def test_person_demographics_name_the_live_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(SEX, text, path.name)
            self.assertIn(MARITAL, text, path.name)
        self.assertNotIn("| Legal sex |", dictionary)
        self.assertNotIn("| Marital status |", dictionary)
        self.assertNotIn("Text,,,x,Legal sex\n", entities)
        self.assertNotIn("Text,,,x,Marital status\n", entities)


if __name__ == "__main__":
    unittest.main()
