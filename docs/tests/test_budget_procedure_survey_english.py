#!/usr/bin/env python3
"""The budget-procedure spec cites the survey index that exists."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class BudgetProcedureSurveyEnglishTest(unittest.TestCase):
    def test_living_spec_cites_the_survey_index(self):
        spec = read("docs/openspec/specs/presupuesto-tramite-relation/spec.md")
        self.assertIn("`docs/openspec/explore.md`", spec)
        self.assertIn("`Budget`", spec)
        self.assertIn("`fk_id_budget`", spec)
        self.assertNotIn("explore_functional_report.md", spec)
        archive = read(
            "docs/openspec/changes/archive/"
            "2026-08-20-resolve-presupuesto-tramite-cardinality/"
            "specs/presupuesto-tramite-relation/spec.md"
        )
        self.assertIn("`openspec/explore_functional_report.md`", archive)
        index = read("docs/openspec/explore.md")
        self.assertIn("explore_functional_report.md", index)


if __name__ == "__main__":
    unittest.main()
