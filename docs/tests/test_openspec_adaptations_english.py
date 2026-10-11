#!/usr/bin/env python3
"""The live OpenSpec guide is English. Validator headings stay Spanish."""

import unittest
from pathlib import Path

GUIDE = (
    Path(__file__).resolve().parents[1]
    / "openspec"
    / "NOTAIRE-ADAPTATIONS.md"
)


class OpenSpecAdaptationsEnglishTest(unittest.TestCase):
    def test_guide_is_english_and_keeps_required_headings(self):
        text = GUIDE.read_text(encoding="utf-8")
        self.assertTrue(text.startswith("# OpenSpec in Notaire"))
        self.assertIn("## Table of contents", text)
        self.assertIn("Java 26", text)
        self.assertNotIn("¿Qué es OpenSpec?", text)
        self.assertNotIn("Tabla de contenidos", text)
        for required in (
            "Objetivo",
            "Reglas de negocio",
            "Módulos afectados",
            "Actualizar documentación permanente",
            "Use Case (Caso de Uso)",
            "escritura-post-firma-legal-cycle",
        ):
            self.assertIn(required, text, required)


if __name__ == "__main__":
    unittest.main()
