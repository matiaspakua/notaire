#!/usr/bin/env python3
"""Case-summary state and synthetic status names are English. Live paths stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class CaseSummaryAndStatusNamesEnglishTest(unittest.TestCase):
    def test_case_summary_state_is_english(self):
        page = read("frontend/src/app/dashboard/managements/page.tsx")
        self.assertIn("caseSummaryId", page)
        self.assertIn("setCaseSummaryId", page)
        self.assertNotIn("resumenCaso", page)
        self.assertNotIn("ResumenCaso", page)

    def test_identification_type_display_names_use_the_live_path(self):
        validation = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "IdentificationTypeValidationIntegrationTest.java"
        )
        self.assertIn("POST /identification-types with blank name returns 400", validation)
        self.assertIn("PUT /identification-types/{id} with blank name returns 400", validation)
        self.assertIn('post("/api/v1/identification-types")', validation)
        self.assertNotIn("type-identificacion", validation)

    def test_synthetic_status_names_are_english(self):
        integrity = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "ManagementStatusReferentialIntegrityTest.java"
        )
        self.assertIn('"StatusFree_InUse"', integrity)
        self.assertIn('"StatusUsed_InUse"', integrity)
        self.assertIn('"StatusEditBlocked"', integrity)
        self.assertIn('"StatusEditAllowed"', integrity)
        self.assertIn('"StatusDeleteBlocked"', integrity)
        self.assertNotIn("Estado", integrity)


if __name__ == "__main__":
    unittest.main()
