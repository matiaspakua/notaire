#!/usr/bin/env python3
"""Reentry and concept comments name the live English schema."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class ReentryCommentsEnglishTest(unittest.TestCase):
    def test_reentry_comment_names_the_live_schema(self):
        source = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "ManagementDocumentReentryPgIntegrationTest.java"
        )
        self.assertIn("against the real schema", source)
        self.assertIn("{@code released}", source)
        self.assertIn("{@code observed}", source)
        self.assertIn("{@code submitted_documents}", source)
        self.assertNotIn("esquema", source)
        self.assertNotIn("contra the", source)
        self.assertNotIn("liberado", source)
        self.assertNotIn("observado", source)
        self.assertNotIn("documentos_presentados", source)
        self.assertIn("Certificado de Dominio", source)
        self.assertIn("Cliente", source)

    def test_concept_comment_names_enabled(self):
        source = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/ConceptControllerTest.java"
        )
        self.assertIn("without 'enabled'", source)
        self.assertNotIn("habilitado", source)


if __name__ == "__main__":
    unittest.main()
