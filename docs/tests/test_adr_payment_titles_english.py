#!/usr/bin/env python3
"""ADR-021 cites the official payment use-case titles."""

import unittest
from pathlib import Path

ADR = (
    Path(__file__).resolve().parents[1]
    / "200-architecture"
    / "202-ADR"
    / "ADR-021-hexagonal-architecture-pilot.md"
)


class AdrPaymentTitlesEnglishTest(unittest.TestCase):
    def test_official_payment_titles_stay(self):
        text = ADR.read_text(encoding="utf-8")
        self.assertIn('CU15 "Procesar pago"', text)
        self.assertIn('CU47 "Consultar pago"', text)
        self.assertIn("PaymentService", text)
        self.assertNotIn("Procesar Payment", text)
        self.assertNotIn("Estado de Payment", text)


if __name__ == "__main__":
    unittest.main()
