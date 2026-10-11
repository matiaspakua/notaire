#!/usr/bin/env python3
"""The living source-surface spec says the domain model maps English names."""

import unittest
from pathlib import Path

SPEC = (
    Path(__file__).resolve().parents[1]
    / "openspec"
    / "specs"
    / "english-source-surface"
    / "spec.md"
)


class EnglishSourceSurfacePurposeEnglishTest(unittest.TestCase):
    def test_purpose_names_english_identifiers(self):
        text = SPEC.read_text(encoding="utf-8")
        self.assertIn(
            "The English domain model maps those identifiers to English names.",
            text,
        )
        self.assertNotIn("still maps to Spanish names", text)
        self.assertIn("`POST /api/v1/usuarios/login`", text)
        for segment in (
            "`usuarios`",
            "`gestiones`",
            "`tramites`",
            "`presupuestos`",
            "`escrituras`",
        ):
            self.assertIn(segment, text, segment)


if __name__ == "__main__":
    unittest.main()
