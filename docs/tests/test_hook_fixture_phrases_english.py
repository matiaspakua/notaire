#!/usr/bin/env python3
"""Synthetic hook fixtures and save-error describe titles are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class HookFixturePhrasesEnglishTest(unittest.TestCase):
    def test_hook_fixtures_are_english(self):
        text = read("frontend/src/tests/unit/hooks.test.ts")
        self.assertIn('name: "Base fee"', text)
        self.assertIn('name: "Protocol"', text)
        self.assertIn('toBe("Protocol")', text)
        self.assertIn("all key is ['audit']", text)
        self.assertIn('name: "En proceso"', text)
        self.assertIn('name: "Compraventa"', text)
        self.assertNotIn("Cobro base", text)
        self.assertNotIn('"Protocolo"', text)
        self.assertNotIn("auditoria", text)

    def test_save_error_describe_titles_are_english(self):
        person = read("frontend/src/tests/unit/person-save-error.test.ts")
        pagination = read("frontend/src/tests/unit/people-pagination.test.tsx")
        self.assertIn('describe("presentPersonSaveError()"', person)
        self.assertIn('lastName: "Existing"', person)
        self.assertIn("Error al guardar la persona", person)
        self.assertIn("Ver persona existente", person)
        self.assertIn(
            'describe("presentPersonSaveError duplicate link',
            pagination,
        )
        self.assertIn("Ver persona existente", pagination)
        self.assertNotIn("presentPersonaSaveError", person)
        self.assertNotIn("presentPersonaSaveError", pagination)
        self.assertNotIn('"Existente"', person)


if __name__ == "__main__":
    unittest.main()
