#!/usr/bin/env python3
"""Included use cases point at the English module name. Official titles stay."""

import unittest
from pathlib import Path

FOLDER = (
    Path(__file__).resolve().parents[1]
    / "200-architecture"
    / "204-diagrams"
    / "Casos de Uso"
)


def combined() -> str:
    return "\n".join(path.read_text(encoding="utf-8") for path in sorted(FOLDER.glob("*.puml")))


class UseCaseModulePointersEnglishTest(unittest.TestCase):
    def test_include_pointers_name_the_english_module(self):
        text = combined()
        for pointer in (
            "(from Budget)",
            "(from Clients)",
            "(from Deeds)",
            "(from Concepts)",
            "(from Procedures)",
        ):
            self.assertIn(pointer, text)
        for retired in (
            "(from Presupuesto)",
            "(from Clientes)",
            "(from Escrituras)",
            "(from Conceptos)",
            "(from Trámites)",
        ):
            self.assertNotIn(retired, text)
        self.assertIn("(from Folios)", text)
        self.assertIn("CU60 - Buscar Presupuesto", text)
        self.assertIn("CU19 - Buscar gestiones de un Cliente", text)
        self.assertIn("CU62 - Buscar Escritura", text)
        self.assertIn("CU66 - Buscar Conceptos", text)
        self.assertIn("CU64 - Buscar Tipo de trámite", text)
        self.assertIn("CU63 - Buscar Folios", text)
        self.assertIn("actor Escribano", text)


if __name__ == "__main__":
    unittest.main()
