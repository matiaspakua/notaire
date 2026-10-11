#!/usr/bin/env python3
"""Document-cost fixtures and the printed-copy comment are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class CostFixturePhrasesEnglishTest(unittest.TestCase):
    def test_document_cost_fixture_is_english(self):
        text = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "DocumentCostTemplateControllerTest.java"
        )
        self.assertIn("Previous deed", text)
        self.assertIn("Should return costs by procedure type", text)
        self.assertIn("when no costs are defined", text)
        self.assertIn("shouldReturnCostsByProcedureType", text)
        self.assertIn('"Cliente"', text)
        self.assertNotIn("Deed previa", text)
        self.assertNotIn("previa", text)
        self.assertNotIn("costos", text)
        self.assertNotIn("Costos", text)

    def test_printed_copy_comment_is_english(self):
        text = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "TestimonyCopyReportIntegrationTest.java"
        )
        self.assertIn("printed copy of a verified testimony", text)
        self.assertIn("CU08", text)
        self.assertNotIn("impresa", text)


if __name__ == "__main__":
    unittest.main()
