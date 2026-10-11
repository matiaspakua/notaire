#!/usr/bin/env python3
"""Synthetic status and type names in management fixtures are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TEST = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (TEST / relative).read_text(encoding="utf-8")


class SyntheticStatusNamesEnglishTest(unittest.TestCase):
    def test_query_and_repository_fixtures_are_english(self):
        query = read("service/unit/ManagementQueryServiceTest.java")
        self.assertIn('"STARTED"', query)
        self.assertNotIn("INICIADO", query)

        repository = read("integration/DeedManagementRepositoryIntegrationTest.java")
        self.assertIn('"Professional"', repository)
        self.assertIn('"STARTED"', repository)
        self.assertIn('"DNI"', repository)
        self.assertNotIn("Profesional", repository)
        self.assertNotIn("INICIADO", repository)

    def test_complete_case_type_names_are_english(self):
        complete = read("integration/ManagementCompleteCaseReadIntegrationTest.java")
        self.assertIn('"Status 1360"', complete)
        self.assertIn('"Procedure 1360"', complete)
        self.assertIn('"PENDIENTE"', complete)
        self.assertNotIn("Estado 1360", complete)
        self.assertNotIn("Tramite 1360", complete)


if __name__ == "__main__":
    unittest.main()
