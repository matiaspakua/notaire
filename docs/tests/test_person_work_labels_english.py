#!/usr/bin/env python3
"""The data dictionary names the seeded occupation and profession sample."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
OCCUPATION = "Occupation. Free text. The seeded sample `Escribano` stays."
PROFESSION = "Profession or trade. Free text. The seeded sample `Escribano` stays."


class PersonWorkLabelsEnglishTest(unittest.TestCase):
    def test_occupation_and_profession_name_the_seeded_sample(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(OCCUPATION, text, path.name)
            self.assertIn(PROFESSION, text, path.name)
        self.assertNotIn("| Occupation |", dictionary)
        self.assertNotIn("| Profession or trade |", dictionary)
        self.assertNotIn("Text,,,x,Occupation\n", entities)
        self.assertNotIn("Text,,,x,Profession or trade\n", entities)


if __name__ == "__main__":
    unittest.main()
