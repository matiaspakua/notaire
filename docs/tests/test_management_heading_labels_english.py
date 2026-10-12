#!/usr/bin/env python3
"""The data dictionary names the seeded deed-management headings."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Heading that describes the management. Stored values "
    "`Compraventa Lote 12 — Familia Pérez` and `Donación — Sucesión Gómez` stay."
)


class ManagementHeadingLabelsEnglishTest(unittest.TestCase):
    def test_management_heading_names_the_seeded_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn("| Heading that describes the management |", dictionary)
        self.assertNotIn(
            "Text,,,,Heading that describes the management\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
