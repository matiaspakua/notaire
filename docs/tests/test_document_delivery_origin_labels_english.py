#!/usr/bin/env python3
"""The data dictionary names the seeded document-type delivery origin."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Origin. Stored values `Cliente`, `Entidad`, and `Entidad Externa` stay."
)


class DocumentDeliveryOriginLabelsEnglishTest(unittest.TestCase):
    def test_document_type_names_the_seeded_delivery_origin(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn(
            "| Origin (`Cliente` or `Entidad Externa`). Those stored values stay. |",
            dictionary,
        )
        self.assertNotIn(
            "Text,,,,Origin (`Cliente` or `Entidad Externa`). Those stored values stay.\n",
            entities,
        )
        self.assertIn(
            "Supply origin (`Cliente` or `Entidad Externa`). Those stored values stay.",
            dictionary,
        )


if __name__ == "__main__":
    unittest.main()
