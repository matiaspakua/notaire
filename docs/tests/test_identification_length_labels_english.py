#!/usr/bin/env python3
"""The data dictionary names the seeded identification-type lengths."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Format or alphanumeric mask. Seeded lengths stay: `8` for `DNI`, `LE`, "
    "and `LC`; `12` for `Pasaporte`; and `11` for `CUIT`."
)


class IdentificationLengthLabelsEnglishTest(unittest.TestCase):
    def test_characters_names_the_seeded_lengths(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertIn(
            "Name. Stored values `DNI`, `CUIT`, `CUIL`, `Pasaporte`, `CI`, `LC`, and `LE` stay.",
            dictionary,
        )
        self.assertNotIn("| Format or alphanumeric mask |", dictionary)
        self.assertNotIn("Text,,,,Format or alphanumeric mask\n", entities)


if __name__ == "__main__":
    unittest.main()
