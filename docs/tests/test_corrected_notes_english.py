#!/usr/bin/env python3
"""Synthetic correction notes are English. Visible testimony labels stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
VITEST = ROOT / "frontend/src/tests/unit/testimony-admin.test.tsx"
INTEGRATION = (
    ROOT
    / "backend-api/src/test/java/com/licensis/notaire/integration"
    / "TestimonyDeleteKeepsHistoryIntegrationTest.java"
)
PAYMENT = (
    ROOT
    / "backend-api/src/test/java/com/licensis/notaire/application/usecase/payment"
    / "EditPaymentServiceTest.java"
)
E2E = ROOT / "testing/e2e/tests/TS-0143-testimony-edit-delete-movements.spec.ts"
ARCHIVE = (
    ROOT
    / "docs/000-archive/300-development/303-testing/PROGRESS-REPORT.md"
)


class CorrectedNotesEnglishTest(unittest.TestCase):
    def test_synthetic_correction_notes_are_english(self):
        vitest = VITEST.read_text(encoding="utf-8")
        integration = INTEGRATION.read_text(encoding="utf-8")
        payment = PAYMENT.read_text(encoding="utf-8")
        e2e = E2E.read_text(encoding="utf-8")
        archive = ARCHIVE.read_text(encoding="utf-8")

        self.assertIn('"First copy"', vitest)
        self.assertIn('"Corrected"', vitest)
        self.assertIn('"Corrected"', integration)
        self.assertIn('"Corrected"', payment)
        self.assertIn("Corrected number E2E", e2e)

        for text in (vitest, integration, payment, e2e):
            self.assertNotIn("Corregido", text)
        self.assertNotIn("Primera copia", vitest)
        self.assertNotIn("Número corregido", e2e)

        self.assertIn("Eliminar", e2e)
        self.assertIn("Testimonio actualizado", e2e)
        self.assertIn("todavía no tiene movimientos", e2e)
        self.assertIn("EFECTIVO", payment)
        self.assertIn("TRANSFERENCIA", payment)
        self.assertIn("Environment File Corregido", archive)


if __name__ == "__main__":
    unittest.main()
