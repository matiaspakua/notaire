#!/usr/bin/env python3
"""Cleanup comments and the role description fixture are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class CleanupCommentAndRoleDescriptionEnglishTest(unittest.TestCase):
    def test_cleanup_comments_name_live_tables(self):
        text = read("backend-api/src/test/resources/cleanup-test-data.sql")
        self.assertIn("concepts, people, users, budgets", text)
        self.assertIn("deed_managements, deeds,", text)
        self.assertIn("TRUNCATE TABLE deed_managements;", text)
        self.assertIn("TRUNCATE TABLE deeds;", text)
        self.assertNotIn("personas", text)
        self.assertNotIn("gestiones", text)
        self.assertNotIn("escrituras", text)

    def test_role_description_is_english(self):
        text = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/RoleControllerTest.java"
        )
        self.assertIn('"description", "Administrator"', text)
        self.assertIn('buildRole(2, "OPERATOR")', text)
        self.assertIn('"modulos", List.of("administracion")', text)
        self.assertNotIn("Administrador", text)
        self.assertNotIn("OPERADOR", text)


if __name__ == "__main__":
    unittest.main()
