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

    def test_package_diagram_uses_the_live_layout(self):
        text = (DIAGRAMS / "backend-package-structure.puml").read_text(encoding="utf-8")
        for live in ("adapter.in.web", "business", "ManagementController", "DeedManagement", "AuditAspect"):
            self.assertIn(live, text, live)
        for retired in ("negocio", "PersonaController", "GestionDeEscritura", "AuditoriaAspect"):
            self.assertNotIn(retired, text, retired)

    def test_target_and_legacy_diagrams_are_english(self):
        target = (DIAGRAMS / "architecture-target.puml").read_text(encoding="utf-8")
        self.assertIn("End user", target)
        self.assertIn("Java 26", target)
        self.assertIn("business", target)
        self.assertNotIn("Usuario final", target)
        self.assertNotIn("negocio", target)
        legacy = (DIAGRAMS / "architecture-legacy.puml").read_text(encoding="utf-8")
        self.assertIn("Desktop interaction", legacy)
        self.assertIn("Escribano", legacy)
        self.assertNotIn("Todo ejecuta", legacy)


if __name__ == "__main__":
    unittest.main()
