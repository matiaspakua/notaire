#!/usr/bin/env python3
"""Lifecycle proposals name a management and keep stored statuses."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


class LifecycleProposalsEnglishTest(unittest.TestCase):
    def test_assessment_proposal_names_a_management(self):
        text = (CHANGES / "docs-921-business-assessment" / "proposal.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("## Objetivo", text)
        self.assertIn("## Reglas de negocio", text)
        self.assertIn("### Módulos afectados", text)
        self.assertIn("`GESTION.md`", text)
        self.assertIn("Documentación completa", text)
        self.assertIn("Testimonio Retirado", text)
        self.assertIn("POST /api/v1/managements/complete-case", text)
        self.assertIn("deed_managements", text)
        self.assertIn("create-management sequence", text)
        self.assertIn("A management moves only on a seeded edge", text)
        self.assertNotIn("gestión", text)
        self.assertNotIn("create-gestión", text)

    def test_lifecycle_proposal_names_a_management(self):
        text = (CHANGES / "docs-921-domain-lifecycle" / "proposal.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("## Objetivo", text)
        self.assertIn("## Reglas de negocio", text)
        self.assertIn("CICLO-DE-VIDA.md", text)
        self.assertIn("Documentación completa", text)
        self.assertIn("`deeds.status`", text)
        self.assertIn("A management moves only along the seeded workflow", text)
        self.assertIn("the management node", text)
        self.assertNotIn("gestión", text)


if __name__ == "__main__":
    unittest.main()
