#!/usr/bin/env python3
"""Demo-case property addresses use an English street. CABA and stored labels stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SPEC = ROOT / "testing/e2e/tests/TS-0090-demo-two-full-cases.spec.ts"


class DemoCaseAddressEnglishTest(unittest.TestCase):
    def test_property_address_is_english(self):
        text = SPEC.read_text(encoding="utf-8")
        self.assertIn("Demo Avenue ${label} 1234, CABA ${suffix}", text)
        self.assertNotIn("Av. Demo", text)
        self.assertNotIn("[Caso ", text)
        self.assertIn("[Case ${def.label}] Property", text)
        self.assertIn("[Case ${def.label}] Payment", text)
        self.assertIn("CABA ${suffix}", text)
        self.assertIn('"María"', text)
        self.assertIn("Compraventa Demo", text)
        self.assertIn("/guardar|crear/i", text)
        self.assertIn("/domicilio/i", text)


if __name__ == "__main__":
    unittest.main()
