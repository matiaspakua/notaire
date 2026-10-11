#!/usr/bin/env python3
"""Synthetic document notes are English. Stored labels and person names stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TEST = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (TEST / relative).read_text(encoding="utf-8")


class DocumentNotePhrasesEnglishTest(unittest.TestCase):
    def test_upcoming_expiration_note_is_english(self):
        text = read("unit/UpcomingExpirationServiceTest.java")
        self.assertIn("Request a copy", text)
        self.assertIn("Certificado de dominio", text)
        self.assertIn("Compraventa Pérez", text)
        self.assertNotIn("Pedir copia", text)
        self.assertNotIn("Pedir", text)

    def test_partial_update_note_is_english(self):
        text = read("integration/SubmittedDocumentPartialUpdateIntegrationTest.java")
        self.assertIn("previous note", text)
        self.assertIn("Deed original", text)
        self.assertIn('"Cliente"', text)
        self.assertNotIn("nota previa", text)
        self.assertNotIn("previa", text)


if __name__ == "__main__":
    unittest.main()
