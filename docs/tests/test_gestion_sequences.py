#!/usr/bin/env python3
"""The case schema names every movement the services actually run.

CU07, CU11, CU12, CU43, CU44. States stay in CICLO-DE-VIDA.md.
"""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GESTION = ROOT / "docs" / "200-architecture" / "203-design" / "GESTION.md"
INGENIERIA = ROOT / "docs" / "300-development" / "INGENIERIA.md"


class GestionSequenceTest(unittest.TestCase):
    def test_testimony_path_includes_verify_and_inscription(self):
        text = GESTION.read_text(encoding="utf-8")
        for call in (
            "POST /api/v1/testimonies/{id}/verify",
            "POST /api/v1/testimony-movements/{idTestimony}/register-inscription",
            "POST /api/v1/testimony-movements/{idTestimony}/reenter",
            "POST /api/v1/managements/{id}/document-reentry",
        ):
            self.assertIn(call, text, call)

    def test_reentry_does_not_invent_a_return_from_observada(self):
        text = GESTION.read_text(encoding="utf-8")
        self.assertIn("No hay transición de vuelta", text)
        self.assertIn("No mueve la gestión", text)

    def test_case_schema_has_an_artifact_id(self):
        text = INGENIERIA.read_text(encoding="utf-8")
        self.assertIn("DOC-GESTION", text)
        self.assertIn("GESTION.md", text)


if __name__ == "__main__":
    unittest.main()
