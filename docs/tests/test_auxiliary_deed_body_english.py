#!/usr/bin/env python3
"""The auxiliary-protocol edge path fills an English deed body."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SPEC = ROOT / "testing/e2e/tests/TS-0081-auxiliary-protocol-workflow.spec.ts"


class AuxiliaryDeedBodyEnglishTest(unittest.TestCase):
    def test_edge_path_fills_an_english_deed_body(self):
        text = SPEC.read_text(encoding="utf-8")
        self.assertIn('fill("Deed body")', text)
        self.assertIn('fill("E2E test deed body")', text)
        self.assertIn("/iniciar escritura/i", text)
        self.assertIn("/crear/i", text)
        self.assertIn("/protocolo auxiliar/i", text)
        self.assertNotIn("Contenido", text)
        business = (ROOT / "docs/100-business/README.md").read_text(encoding="utf-8")
        self.assertIn("| Carpeta | Contenido |", business)
        catalog = (ROOT / "frontend/messages/es.json").read_text(encoding="utf-8")
        self.assertIn("Contenido de la escritura", catalog)


if __name__ == "__main__":
    unittest.main()
