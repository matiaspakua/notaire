#!/usr/bin/env python3
"""Open change records cite the official use-case titles, not half-translations."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"

FILES = (
    CHANGES / "fix-596-people-pagination" / "proposal.md",
    CHANGES / "fix-596-people-pagination" / "traceability.md",
    CHANGES / "fix-655-testimony-requires-deed" / "proposal.md",
    CHANGES / "fix-655-testimony-requires-deed" / "traceability.md",
    CHANGES / "fix-1315-delete-returns-204" / "proposal.md",
    CHANGES / "fix-1315-delete-returns-204" / "traceability.md",
)


class ProposalUseCaseTitlesEnglishTest(unittest.TestCase):
    def test_use_case_rows_cite_the_official_titles(self):
        text = "\n".join(path.read_text(encoding="utf-8") for path in FILES)
        self.assertIn("CU54 – Modificar Persona", text)
        self.assertIn("CU07 – Generar testimonio", text)
        self.assertIn("CU08 – Verificar Testimonio", text)
        self.assertIn("CU21 – Modificar Usuario", text)
        self.assertIn("Dar Alta Cliente", text)
        self.assertIn("Buscar persona", text)
        self.assertIn("Procesar pago", text)
        self.assertIn("Gestionar historial de gestion", text)
        self.assertNotIn("Modificar Person;", text)
        self.assertNotIn("Generar Testimony", text)
        self.assertNotIn("Verificar Testimony", text)
        self.assertNotIn("Modificar UserAccount", text)


if __name__ == "__main__":
    unittest.main()
