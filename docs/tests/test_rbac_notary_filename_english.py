#!/usr/bin/env python3
"""The RBAC notary-create request uses an English filename."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RBAC = ROOT / "backend-api" / "api-test" / "rbac"


class RbacNotaryFilenameEnglishTest(unittest.TestCase):
    def test_filename_is_english_and_keeps_the_stored_type(self):
        live = RBAC / "04-employee-create-notary.yml"
        retired = RBAC / "04-employee-create-escribano.yml"
        self.assertTrue(live.is_file(), live)
        self.assertFalse(retired.exists(), retired)
        text = live.read_text(encoding="utf-8")
        self.assertIn("Employee cannot create an ESCRIBANO", text)
        self.assertIn('"type": "ESCRIBANO"', text)
        self.assertIn("POST /api/v1/users", text)


if __name__ == "__main__":
    unittest.main()
