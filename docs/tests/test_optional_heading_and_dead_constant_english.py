#!/usr/bin/env python3
"""Synthetic headings and the deed-status comment use the live English names."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class OptionalHeadingAndDeadConstantEnglishTest(unittest.TestCase):
    def test_optional_budget_heading_is_english(self):
        association = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "BudgetPersonAssociationPgIntegrationTest.java"
        )
        self.assertIn('"Budget CU01 pg optional"', association)
        self.assertNotIn("opcional", association)
        self.assertIn('"BORRADOR"', association)

    def test_template_concept_name_is_english(self):
        spec = read("testing/e2e/tests/TS-0131-budget-template.spec.ts")
        self.assertIn('"Honorarios Create E2E"', spec)
        self.assertNotIn("Honorarios Alta E2E", spec)
        self.assertIn('"Honorarios E2E"', spec)

    def test_deed_status_comment_names_the_live_constant(self):
        spec = read("testing/e2e/tests/TS-0013-deeds-signing-workflow.spec.ts")
        self.assertIn("BusinessConstants.DeedUnsigned", spec)
        self.assertNotIn("ConstantesNegocio", spec)
        self.assertNotIn("ESCRITURA_SIN_FIRMAR", spec)
        self.assertIn("/Sin Firmar/i", spec)


if __name__ == "__main__":
    unittest.main()
