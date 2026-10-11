#!/usr/bin/env python3
"""Payment edit methods and the registry note test id are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class PaymentEditLocalsEnglishTest(unittest.TestCase):
    def test_payment_edit_methods_are_english(self):
        unit = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/PaymentUseCaseTest.java"
        )
        integration = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "PaymentUseCaseIntegrationTest.java"
        )
        self.assertIn("shouldUpdatePaymentMethodWhenEditingPayment", unit)
        self.assertIn("shouldHandleEditPaymentWithNullAmount", integration)
        self.assertIn("shouldHandleEditPaymentWithNullDate", integration)
        self.assertIn("shouldHandleEditPaymentWithNullNotes", integration)
        self.assertIn("shouldRejectNegativeAmountInEditPayment", integration)
        self.assertNotIn("Editar", unit)
        self.assertNotIn("Editar", integration)
        self.assertIn('"Efectivo"', unit)
        self.assertIn('"Transferencia"', unit)

    def test_registry_note_test_id_is_english(self):
        page = read("frontend/src/app/dashboard/registration-drafts/page.tsx")
        spec = read("testing/e2e/tests/TS-0082-registration-draft-feature.spec.ts")
        self.assertIn('data-testid="input-notes-registry"', page)
        self.assertIn('getByTestId("input-notes-registry")', spec)
        self.assertNotIn("input-notes-registro", page)
        self.assertNotIn("input-notes-registro", spec)
        self.assertIn('t("fields.observacionesRegistro")', page)
        self.assertIn('t("observar")', page)
        self.assertIn("/Generada/i", spec)


if __name__ == "__main__":
    unittest.main()
