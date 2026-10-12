#!/usr/bin/env python3
"""The data dictionary names the seeded procedure-type notes."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Rules and notarial description. Stored notes `Tramite de compraventa de "
    "inmuebles`, `Tramite de donacion`, `Tramite de hipoteca`, `Tramite de "
    "sucesion`, and `Otorgamiento de poderes` stay."
)


class ProcedureTypeNoteLabelsEnglishTest(unittest.TestCase):
    def test_procedure_type_notes_name_the_seeded_labels(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn("| Rules and notarial description |", dictionary)
        self.assertNotIn(
            "Text,,,x,Rules and notarial description\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
