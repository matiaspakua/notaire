#!/usr/bin/env python3
"""The data dictionary names the seeded person place samples."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LOCALITY = "Locality. The seeded sample `Buenos Aires` stays."
PROVINCE = "Province. The seeded sample `Buenos Aires` stays."
NATIONALITY = "Nationality. The seeded sample `Argentina` stays."


class PersonPlaceLabelsEnglishTest(unittest.TestCase):
    def test_place_columns_name_the_seeded_samples(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LOCALITY, text, path.name)
            self.assertIn(PROVINCE, text, path.name)
            self.assertIn(NATIONALITY, text, path.name)
        self.assertNotIn("| Province |", dictionary)
        self.assertNotIn("| Nationality |", dictionary)
        self.assertIn(
            "people,Strong,locality,,yes,Text,,,x," + LOCALITY,
            entities,
        )
        self.assertIn(
            "properties,Strong,locality,,yes,Text,,,x,Locality\n",
            entities,
        )
        self.assertNotIn("Text,,,x,Province\n", entities)
        self.assertNotIn("Text,,,x,Nationality\n", entities)


if __name__ == "__main__":
    unittest.main()
