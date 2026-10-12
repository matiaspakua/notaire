#!/usr/bin/env python3
"""The data dictionary names the seeded procedure-type flags."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
ENABLED = (
    "`true` when new managements are allowed. Seeded flags stay: `true` for "
    "`Compraventa`, `Donacion`, `Hipoteca`, `Sucesion`, and `Poder`."
)
ARCHIVED = (
    "`true` when a physical binder is required. Seeded flags stay: `true` for "
    "`Compraventa`, `Donacion`, `Hipoteca`, and `Sucesion`, and `false` for `Poder`."
)
REGISTERED = (
    "`true` when registry registration is required. Seeded flags stay: `true` "
    "for `Compraventa`, `Donacion`, and `Hipoteca`, and `false` for `Sucesion` "
    "and `Poder`."
)
PROPERTIES = (
    "`true` when the act involves property. Seeded flags stay: `true` for "
    "`Compraventa`, `Donacion`, and `Hipoteca`, and `false` for `Sucesion` and "
    "`Poder`."
)


class ProcedureTypeFlagLabelsEnglishTest(unittest.TestCase):
    def test_procedure_type_flags_name_the_seeded_values(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in (ENABLED, ARCHIVED, REGISTERED, PROPERTIES):
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        self.assertNotIn("| `true` when new managements are allowed |", dictionary)
        self.assertNotIn("| `true` when a physical binder is required |", dictionary)
        self.assertNotIn(
            "| `true` when registry registration is required |",
            dictionary,
        )
        self.assertNotIn("| `true` when the act involves property |", dictionary)
        self.assertNotIn(
            "Boolean,,,,`true` when new managements are allowed\n",
            entities,
        )
        self.assertNotIn(
            "Boolean,,,,`true` when a physical binder is required\n",
            entities,
        )
        self.assertNotIn(
            "Boolean,,,,`true` when registry registration is required\n",
            entities,
        )
        self.assertNotIn(
            "Boolean,,,,`true` when the act involves property\n",
            entities,
        )


if __name__ == "__main__":
    unittest.main()
