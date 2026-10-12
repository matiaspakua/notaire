#!/usr/bin/env python3
"""The living error-toast spec cites the official user titles. The archive keeps the shorthand."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SPEC = ROOT / "docs" / "openspec" / "specs" / "api-error-toasts" / "spec.md"
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-fix-1054-api-error-toasts"
    / "specs"
    / "api-error-toasts"
    / "spec.md"
)
CU20 = "CU20 – Dar alta usuario"
CU21 = "CU21 – Modificar Usuario"
LOOSE_USERS = "usuarios;"
LOOSE_TABLES = "tablas base"


class ErrorToastExemplarsEnglishTest(unittest.TestCase):
    def test_living_purpose_cites_the_official_user_titles(self):
        text = SPEC.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn("CU15 – Procesar pago", text)
        self.assertIn(CU20, text)
        self.assertIn(CU21, text)
        self.assertIn("base-catalog", text)
        self.assertNotIn(LOOSE_USERS, text)
        self.assertNotIn(LOOSE_TABLES, text)
        self.assertIn(LOOSE_USERS, archived)
        self.assertIn(LOOSE_TABLES, archived)
        self.assertNotIn("base-catalog", archived)


if __name__ == "__main__":
    unittest.main()
