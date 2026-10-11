#!/usr/bin/env python3
"""The workflow tracker quotes seeded statuses as stored."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TRACKER = ROOT / "docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md"

STORED = (
    "Testimonio Generado",
    "Testimonio Ingresado a Inscripcion",
    "Testimonio Retirado",
    "Documentacion Completa",
    "Gestion con Escritura Inscripta",
    "Gestion con Escritura Firmada",
    "Gestion con Escritura Sin Firmar",
    "En Tramite",
    "Donacion",
)

FORBIDDEN = (
    "Testimony Generado",
    "Testimony Ingresado",
    "Documentación Completa",
    "(Inscripta)",
    "Donación",
    "→ Firmada",
)


class WorkflowTrackerStoredStatusesTest(unittest.TestCase):
    def test_demo_seed_quotes_stored_statuses(self):
        text = TRACKER.read_text(encoding="utf-8")
        for label in STORED:
            self.assertIn(label, text, label)
        for label in FORBIDDEN:
            self.assertNotIn(label, text, label)


if __name__ == "__main__":
    unittest.main()
