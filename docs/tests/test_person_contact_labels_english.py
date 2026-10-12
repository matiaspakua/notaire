#!/usr/bin/env python3
"""The data dictionary names the seeded person address and emails."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
ADDRESS = (
    "Residential or legal address. The seeded sample `Av. Principal 123` stays."
)
EMAIL = "Primary email. The seeded sample `jcgarcia@notaria.com` stays."
SECONDARY = (
    "Secondary or historical email. The seeded sample `jcgarcia@notaria.com` stays."
)


class PersonContactLabelsEnglishTest(unittest.TestCase):
    def test_address_and_emails_name_the_seeded_samples(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(ADDRESS, text, path.name)
            self.assertIn(EMAIL, text, path.name)
            self.assertIn(SECONDARY, text, path.name)
        self.assertNotIn("| Residential or legal address |", dictionary)
        self.assertNotIn("| Primary email |", dictionary)
        self.assertNotIn("| Secondary or historical email |", dictionary)
        self.assertNotIn("Text,,,x,Residential or legal address\n", entities)
        self.assertNotIn("Text,,,x,Primary email\n", entities)
        self.assertNotIn("Text,,,x,Secondary or historical email\n", entities)


if __name__ == "__main__":
    unittest.main()
