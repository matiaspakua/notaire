#!/usr/bin/env python3
"""Workflow tracker stubs quote seeded statuses and an English heading."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TRACKER = "frontend/src/components/motion/WorkflowTracker.tsx"
STUB = "testing/e2e/tests/TS-0138-workflow-tracker.spec.ts"


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class WorkflowStubStoredStatusesTest(unittest.TestCase):
    def test_tracker_comment_quotes_the_seed(self):
        source = read(TRACKER)
        self.assertIn(
            'seeded label "Testimonio Ingresado a Inscripcion"',
            source,
        )
        self.assertNotIn("Testimony Ingresado", source)

    def test_e2e_stub_uses_stored_statuses_and_an_english_heading(self):
        source = read(STUB)
        self.assertIn("Reentry loop fixture", source)
        self.assertIn("Gestion con Escritura Firmada", source)
        self.assertIn("Testimonio Generado", source)
        self.assertIn("Testimonio Ingresado a Inscripcion", source)
        self.assertIn("Testimonio Retirado", source)
        self.assertIn("Iniciada", source)
        self.assertIn("En Tramite", source)
        self.assertNotIn("Reingreso", source)
        self.assertNotIn("Testimony Generado", source)
        self.assertNotIn("Testimony Ingresado", source)
        self.assertNotIn("Testimony Retirado", source)
        self.assertNotIn("Gestion con Deed", source)


if __name__ == "__main__":
    unittest.main()
