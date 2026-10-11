#!/usr/bin/env python3
"""Living folio and state-diagram notes quote stored statuses in English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FOLIO = (
    ROOT
    / "backend-api/src/main/java/com/licensis/notaire/business/Folio.java"
)
README = (
    ROOT
    / "docs/200-architecture/204-diagrams/Diagrama de Estados/README.md"
)


class FolioStatusCommentEnglishTest(unittest.TestCase):
    def test_folio_comment_names_stored_statuses_in_english(self):
        text = FOLIO.read_text(encoding="utf-8")
        self.assertIn("Business rule", text)
        for label in (
            "Nuevo",
            "Utilizado",
            "Errose",
            "no pasó",
            "Asignado a cuaderno",
        ):
            self.assertIn(label, text, label)
        self.assertNotIn("REGLA DE NEGOCIO", text)
        self.assertNotIn("Estados posibles", text)
        self.assertNotIn("-New", text)

    def test_state_diagram_readme_quotes_the_stored_archive_status(self):
        text = README.read_text(encoding="utf-8")
        self.assertIn("`Documentacion Completa`", text)
        self.assertNotIn("Documentación completa", text)


if __name__ == "__main__":
    unittest.main()
