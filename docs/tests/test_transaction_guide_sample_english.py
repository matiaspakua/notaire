#!/usr/bin/env python3
"""The Spring transaction guide sample names the live deed service and package."""

import unittest
from pathlib import Path

GUIDE = (
    Path(__file__).resolve().parents[1]
    / "300-development"
    / "302-code-standards"
    / "SPRING-TRANSACTION-GUIDE.md"
)


class TransactionGuideSampleEnglishTest(unittest.TestCase):
    def test_sample_names_the_live_service_and_package(self):
        text = GUIDE.read_text(encoding="utf-8")
        self.assertIn("public class DeedService", text)
        self.assertIn("com/licensis/notaire/business/", text)
        self.assertIn("Page<Deed>", text)
        self.assertNotIn("EscrituraService", text)
        self.assertNotIn("negocio/", text)


if __name__ == "__main__":
    unittest.main()
