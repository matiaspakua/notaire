#!/usr/bin/env python3
"""Bruno incomplete-body assertions match the English RequiredFields message."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
API = ROOT / "backend-api" / "api-test"
FILES = (
    API / "testimonies" / "04a-update-empty-body-rejected.yml",
    API / "testimonies" / "04c-update-missing-version-rejected.yml",
    API / "procedures" / "04a-update-empty-body-rejected.yml",
    API / "procedure-types" / "04a-assign-workflow-empty-body-rejected.yml",
)


class BrunoRequiredMessagesEnglishTest(unittest.TestCase):
    def test_incomplete_put_assertions_say_is_required(self):
        source = (
            ROOT
            / "backend-api"
            / "src"
            / "main"
            / "java"
            / "com"
            / "licensis"
            / "notaire"
            / "adapter"
            / "in"
            / "web"
            / "support"
            / "RequiredFields.java"
        ).read_text(encoding="utf-8")
        self.assertIn('": is required"', source)

        for path in FILES:
            text = path.read_text(encoding="utf-8")
            self.assertIn("is required", text, path.name)
            self.assertNotIn("es obligatorio", text, path.name)


if __name__ == "__main__":
    unittest.main()
