#!/usr/bin/env python3
"""The named-query spec forbids the retired prefix Persona."""

import unittest
from pathlib import Path

SPEC = (
    Path(__file__).resolve().parents[1]
    / "openspec"
    / "specs"
    / "jpa-namedquery-english-names"
    / "spec.md"
)


class NamedQueryPersonaPrefixTest(unittest.TestCase):
    def test_create_named_query_scenario_forbids_persona(self):
        text = SPEC.read_text(encoding="utf-8")
        self.assertIn("#### Scenario: No Persona createNamedQuery mismatch", text)
        self.assertIn("zero matches start with `Persona.`", text)
        self.assertIn("The entity declares `Person.*`", text)
        self.assertNotIn("#### Scenario: No Person createNamedQuery mismatch", text)
        self.assertNotIn("zero matches start with `Person.`", text)


if __name__ == "__main__":
    unittest.main()
