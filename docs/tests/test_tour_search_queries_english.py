#!/usr/bin/env python3
"""The application tour searches with English queries. Stored labels stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TOUR = ROOT / "testing/e2e/tests/TS-0060-full-application-tour-regression.spec.ts"
SEED = ROOT / "backend-api/src/main/resources/db/migration/V10__seed_workflow_demo_data.sql"
H2 = ROOT / "backend-api/src/test/resources/data.sql"


class TourSearchQueriesEnglishTest(unittest.TestCase):
    def test_tour_search_queries_are_english(self):
        text = TOUR.read_text(encoding="utf-8")
        self.assertIn('getByTestId("search-statuses")', text)
        self.assertIn('fill("Iniciada")', text)
        self.assertIn('getByTestId("search-workflows")', text)
        self.assertIn('fill("Workflow")', text)
        self.assertNotIn('fill("inicial")', text)
        self.assertNotIn('fill("Gestión")', text)

    def test_stored_labels_the_queries_match_stay(self):
        self.assertIn("Workflow de Gestión Estándar", SEED.read_text(encoding="utf-8"))
        self.assertIn("'Iniciada'", H2.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
