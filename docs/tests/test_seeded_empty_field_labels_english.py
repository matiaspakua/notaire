#!/usr/bin/env python3
"""The data dictionary names the seeded concept version and empty sample fields."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
VERSION = (
    "Optimistic lock (Hibernate). The seeded `Documentacion` row stays at "
    "version `2`. The other seeded concepts stay at version `1`."
)
PERSON = "File notes and background. The seeded sample stays empty."
MANAGEMENT = "Working notes. The seeded samples stay empty."
PROCEDURE = "Instructions or observations. The seeded samples stay empty."
CONDITION = (
    "Guard condition for the transition. The seeded transitions stay with an "
    "empty condition."
)
LIVE = (VERSION, PERSON, MANAGEMENT, PROCEDURE, CONDITION)


class SeededEmptyFieldLabelsEnglishTest(unittest.TestCase):
    def test_empty_sample_fields_and_concept_version_are_named(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in LIVE:
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        self.assertNotIn("| Optimistic lock (Hibernate) |", dictionary)
        self.assertNotIn("| File notes and background |", dictionary)
        self.assertNotIn("| Working notes |", dictionary)
        self.assertNotIn("| Instructions or observations |", dictionary)
        self.assertNotIn("| Guard condition for the transition |", dictionary)
        self.assertNotIn("Integer,,,,Optimistic lock (Hibernate)\n", entities)
        self.assertNotIn("Text,,,x,File notes and background\n", entities)
        self.assertNotIn("Text,,,x,Working notes\n", entities)
        self.assertNotIn("Text,,,x,Instructions or observations\n", entities)
        self.assertNotIn("Text,,,x,Guard condition for the transition\n", entities)


if __name__ == "__main__":
    unittest.main()
