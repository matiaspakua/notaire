#!/usr/bin/env python3
"""State-diagram sources name the live management API. The folder name stays."""

import unittest
from pathlib import Path

FOLDER = (
    Path(__file__).resolve().parents[1]
    / "200-architecture"
    / "204-diagrams"
    / "Diagrama de Estados"
)


class StateDiagramsEnglishTest(unittest.TestCase):
    def test_workflow_sequence_uses_the_live_management_api(self):
        text = (FOLDER / "workflow-gestion.puml").read_text(encoding="utf-8")
        for live in (
            "ManagementController",
            "POST /api/v1/managements/complete-case",
            "deed_managements",
            "Escribano",
        ):
            self.assertIn(live, text, live)
        for retired in (
            "GestionController",
            "/api/v1/gestion",
            "gestiones_de_escrituras",
            "registro_auditoria",
            "GestionDTO",
        ):
            self.assertNotIn(retired, text, retired)

    def test_activity_diagram_is_english_and_keeps_the_stored_status(self):
        text = (FOLDER / "transicion-de-estados.puml").read_text(encoding="utf-8")
        self.assertIn("Start the management", text)
        self.assertIn("Documentacion Completa", text)
        self.assertIn("Testimonio Retirado", text)
        self.assertIn("POST /api/v1/managements/{id}/archive", text)
        self.assertNotIn("¿Es cliente?", text)
        self.assertNotIn("Archivar gestión", text)
        self.assertNotIn("Deed status No Pasó", text)


if __name__ == "__main__":
    unittest.main()
