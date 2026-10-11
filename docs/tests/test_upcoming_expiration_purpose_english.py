#!/usr/bin/env python3
"""The living expiry spec cites the official CU42 title. The archive keeps the quote."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SPEC = (
    ROOT
    / "docs"
    / "openspec"
    / "specs"
    / "documento-presentado-herencia-vencimiento"
    / "spec.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-09-04-tipo-documento-vencimiento-config"
    / "specs"
    / "documento-presentado-herencia-vencimiento"
    / "spec.md"
)
CHANGELOG = ROOT / "CHANGELOG.md"
OFFICIAL = "CU42 – Informar próximos vencimientos"
QUOTED = '"informar próximos vencimientos"'
RELEASED = 'CU42\'s "próximos vencimientos"'


class UpcomingExpirationPurposeEnglishTest(unittest.TestCase):
    def test_living_purpose_cites_the_official_title(self):
        text = SPEC.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        changelog = CHANGELOG.read_text(encoding="utf-8")
        self.assertIn(OFFICIAL, text)
        self.assertNotIn(QUOTED, text)
        self.assertIn(QUOTED, archived)
        self.assertIn(RELEASED, changelog)


if __name__ == "__main__":
    unittest.main()
