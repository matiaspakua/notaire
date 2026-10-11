#!/usr/bin/env python3
"""Two living specs name the live contract in English."""

import unittest
from pathlib import Path

SPECS = Path(__file__).resolve().parents[1] / "openspec" / "specs"


class LivingSpecLeftoversEnglishTest(unittest.TestCase):
    def test_history_scenario_is_english(self):
        text = (SPECS / "history-order" / "spec.md").read_text(encoding="utf-8")
        self.assertIn(
            "#### Scenario: History lists same-day entries in insertion order",
            text,
        )
        self.assertNotIn("Bitácora", text)

    def test_named_query_spec_names_the_forbidden_spanish_prefixes(self):
        text = (SPECS / "jpa-namedquery-english-names" / "spec.md").read_text(encoding="utf-8")
        for token in (
            "`Escritura.`",
            "`Usuario.`",
            "`Presupuesto.`",
            "`Persona.`",
            "`Folio.findByNumero`",
            "MUST NOT use `Persona.*`",
        ):
            self.assertIn(token, text, token)
        self.assertNotIn("Spanish prefixes (`Deed`", text)
        self.assertNotIn("MUST NOT use `Person.*` (the entity declares `Person.*`)", text)


if __name__ == "__main__":
    unittest.main()
