#!/usr/bin/env python3
"""A zero-value concept fixture uses an English name. Seeded Honorarios stays."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
HOOKS = ROOT / "frontend/src/tests/unit/business-logic-hooks.test.ts"


class ConceptFixtureNameEnglishTest(unittest.TestCase):
    def test_zero_value_concept_name_is_english(self):
        text = HOOKS.read_text(encoding="utf-8")
        self.assertIn('name: "Exempt"', text)
        self.assertNotIn("Exento", text)
        self.assertIn('name: "Honorarios"', text)
        self.assertIn("Av. Corrientes", text)


if __name__ == "__main__":
    unittest.main()
