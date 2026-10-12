#!/usr/bin/env python3
"""The data dictionary names the full seeded procedure-type catalog."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
LIVE = (
    "Procedure-type name. Stored examples such as `Compraventa` and `Poder` stay. "
    "The seeded catalog also keeps `Donacion`, `Hipoteca`, and `Sucesion`."
)


class ProcedureTypeCatalogEnglishTest(unittest.TestCase):
    def test_procedure_type_name_lists_the_seeded_catalog(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for path, text in ((DICTIONARY, dictionary), (ENTITIES, entities)):
            self.assertIn(LIVE, text, path.name)
        self.assertNotIn(
            "| Procedure-type name. Stored examples such as `Compraventa` and `Poder` stay. |",
            dictionary,
        )
        self.assertNotIn(
            "Text,,,,Procedure-type name. Stored examples such as `Compraventa` and `Poder` stay.\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
