#!/usr/bin/env python3
"""The data dictionary names the seeded procedure headings."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Descriptive heading (optional since V13). Stored values "
    "`Compraventa inmueble Lote 12` and `Donación a herederos` stay."
)


class ProcedureHeadingLabelsEnglishTest(unittest.TestCase):
    def test_procedure_heading_names_the_seeded_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn("| Descriptive heading (optional since V13) |", dictionary)
        self.assertNotIn(
            "Text,,,x,Descriptive heading (optional since V13)\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
