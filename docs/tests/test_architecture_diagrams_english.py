#!/usr/bin/env python3
"""Live architecture diagrams name the current English types."""

import unittest
from pathlib import Path

DIAGRAMS = Path(__file__).resolve().parents[1] / "200-architecture" / "204-diagrams"


class ArchitectureDiagramsEnglishTest(unittest.TestCase):
    def test_core_model_uses_live_type_names(self):
        text = (DIAGRAMS / "data-model-core.puml").read_text(encoding="utf-8")
        for live in ("DeedManagement", "Person", "Deed", "Budget", "Procedure", "AuditRecord", "User"):
            self.assertIn(live, text, live)
        for retired in ("GestionDeEscritura", "Escritura", "Presupuesto", "Persona", "RegistroAuditoria"):
            self.assertNotIn(retired, text, retired)

    def test_superseded_architecture_diagrams_are_archived(self):
        """#2483: the LikeC4 model replaced these; a live copy would contradict it."""
        archive = DIAGRAMS.parents[1] / "000-archive" / "200-architecture" / "204-diagrams"
        for name in ("architecture-legacy", "architecture-target", "deployment-docker",
                     "backend-package-structure", "migration-phases"):
            self.assertFalse((DIAGRAMS / f"{name}.puml").exists(), name)
            self.assertTrue((archive / f"{name}.puml").exists(), name)

if __name__ == "__main__":
    unittest.main()
