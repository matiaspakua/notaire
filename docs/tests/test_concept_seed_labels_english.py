#!/usr/bin/env python3
"""The data dictionary names the seeded concept labels."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Concept name. Stored values `IVA`, `Honorarios`, `Documentacion`, and "
    "`Protocolo` stay."
)


class ConceptSeedLabelsEnglishTest(unittest.TestCase):
    def test_concept_name_lists_the_seeded_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn(
            "| Concept name (for example `Honorarios`). Stored names stay. |",
            dictionary,
        )
        self.assertNotIn(
            "Text,,,,Concept name (for example `Honorarios`). Stored names stay.\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
