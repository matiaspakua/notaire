#!/usr/bin/env python3
"""The data dictionary names the seeded concept amounts and flags."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DICTIONARY = ROOT / "docs/200-architecture/205-data-model/Diccionario de Datos.md"
ENTITIES = (
    ROOT
    / "docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv"
)
AMOUNT = (
    "Fixed reference amount. Seeded amounts stay: `0` for `IVA` and "
    "`Honorarios`, `500` for `Documentacion`, and `150` for `Protocolo`."
)
PERCENTAGE = (
    "Standard percentage applied to the act amount. Seeded percentages stay: "
    "`20` for `IVA`, `5` for `Honorarios`, and `0` for `Documentacion` and "
    "`Protocolo`."
)
FIXED = (
    "`true` when the amount is fixed, `false` when it is a percentage. "
    "Seeded flags stay: `false` for `IVA` and `Honorarios`, and `true` for "
    "`Documentacion` and `Protocolo`."
)


class ConceptAmountLabelsEnglishTest(unittest.TestCase):
    def test_concept_amounts_name_the_seeded_values(self):
        dictionary = DICTIONARY.read_text(encoding="utf-8")
        entities = ENTITIES.read_text(encoding="utf-8")
        for live in (AMOUNT, PERCENTAGE, FIXED):
            self.assertIn(live, dictionary, live)
            self.assertIn(live, entities, live)
        self.assertNotIn("| Fixed reference amount |", dictionary)
        self.assertNotIn(
            "| Standard percentage applied to the act amount |",
            dictionary,
        )
        self.assertNotIn("Text,,,,Fixed reference amount\n", entities)
        self.assertNotIn(
            "Text,,,,Standard percentage applied to the act amount\n",
            entities,
        )
        self.assertIn(
            'items,Weak,fixed_concept,,no,Boolean,,,,"`true` when the amount is fixed, '
            '`false` when it is a percentage"\n',
            entities,
        )


if __name__ == "__main__":
    unittest.main()
