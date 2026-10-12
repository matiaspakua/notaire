#!/usr/bin/env python3
"""The data dictionary names the seeded marital status and node layout."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
MARITAL = (
    "Marital status. Stored values `Soltero` and `Casado` stay. "
    "The seeded sample `Casado` stays."
)
USER = (
    "Legacy descriptive role. Stored values `Escribano`, `Empleado`, `EMPLEADO`, "
    "`ADMIN`, `ADMINISTRADOR`, and `ESCRIBANO` stay. The seeded sample "
    "`Escribano` stays."
)
NODE = (
    "Node type: `INITIAL`, `INTERMEDIATE`, `FINAL`. The live seeded graph uses "
    "all three."
)
POSITION_X = (
    "Visual X coordinate in the designer. The seeded nodes stay without a coordinate."
)
POSITION_Y = (
    "Visual Y coordinate in the designer. The seeded nodes stay without a coordinate."
)
LIVE = (MARITAL, USER, NODE, POSITION_X, POSITION_Y)


class SeededMaritalAndNodeLayoutEnglishTest(unittest.TestCase):
    def test_marital_status_and_node_layout_name_the_seed(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in LIVE:
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        self.assertNotIn(
            "| Marital status. Stored values `Soltero` and `Casado` stay. |",
            dictionary,
        )
        self.assertNotIn(
            "| Legacy descriptive role. Stored values `Escribano`, `Empleado`, "
            "`EMPLEADO`, `ADMIN`, `ADMINISTRADOR`, and `ESCRIBANO` stay. |",
            dictionary,
        )
        self.assertNotIn(
            "| Node type: `INITIAL`, `INTERMEDIATE`, `FINAL` |",
            dictionary,
        )
        self.assertNotIn("| Visual X coordinate in the designer |", dictionary)
        self.assertNotIn("| Visual Y coordinate in the designer |", dictionary)
        self.assertNotIn(
            "Text,,,x,Marital status. Stored values `Soltero` and `Casado` stay.\n",
            entities,
        )
        self.assertNotIn("Real,,,x,Visual X coordinate in the designer\n", entities)
        self.assertNotIn("Real,,,x,Visual Y coordinate in the designer\n", entities)


if __name__ == "__main__":
    unittest.main()
