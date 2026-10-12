#!/usr/bin/env python3
"""Comments name the live English columns. Catalog keys and the retired-param check stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BUDGETS = ROOT / "frontend/src/app/dashboard/budgets/page.tsx"
PAGINATION = ROOT / "frontend/src/tests/unit/budgets-deeds-pagination.test.tsx"
IDENTIFICATION = (
    ROOT / "backend-api/src/main/java/com/licensis/notaire/business/IdentificationType.java"
)


class RetiredQueryCommentsEnglishTest(unittest.TestCase):
    def test_comments_name_the_english_fields(self):
        budgets = BUDGETS.read_text(encoding="utf-8")
        self.assertIn(
            "the backend reads `status`. The retired query name was ignored",
            budgets,
        )
        self.assertNotIn("`estado` was ignored", budgets)
        self.assertIn('t("estadoFilter")', budgets)
        self.assertIn('t("resumen.numeroGestion")', budgets)

        pagination = PAGINATION.read_text(encoding="utf-8")
        self.assertIn("retired query names the backend ignores", pagination)
        self.assertNotIn("`estado` instead of `status`", pagination)
        self.assertNotIn("`numero` instead of `number`", pagination)
        self.assertIn('u.includes("numero=")', pagination)

        identification = IDENTIFICATION.read_text(encoding="utf-8")
        self.assertIn('null value in column characters"', identification)
        self.assertNotIn("column caracteres", identification)
        self.assertIn('@Column(name = "characters")', identification)


if __name__ == "__main__":
    unittest.main()
