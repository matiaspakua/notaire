#!/usr/bin/env python3
"""The role-unassign response describes the outcome in English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FILES = (
    ROOT / "backend-api/src/main/java/com/licensis/notaire/adapter/in/web/user/RoleController.java",
    ROOT / "backend-api/openapi/openapi.yaml",
    ROOT / "frontend/src/types/api.generated.ts",
)
LIVE = "Role unassigned"


class RoleUnassignDescriptionEnglishTest(unittest.TestCase):
    def test_unassign_response_names_the_outcome_in_english(self):
        for path in FILES:
            text = path.read_text(encoding="utf-8")
            self.assertIn(LIVE, text, path.name)
            self.assertNotIn("desasignado", text, path.name)


if __name__ == "__main__":
    unittest.main()
