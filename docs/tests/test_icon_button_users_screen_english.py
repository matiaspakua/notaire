#!/usr/bin/env python3
"""The living icon-button spec names the users screen in English. The archive keeps the shorthand."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SPEC = ROOT / "docs" / "openspec" / "specs" / "icon-button-accessible-names" / "spec.md"
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-fix-1057-a11y-icon-names"
    / "specs"
    / "icon-button-accessible-names"
    / "spec.md"
)


class IconButtonUsersScreenEnglishTest(unittest.TestCase):
    def test_living_scenario_names_the_users_screen(self):
        text = SPEC.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn("#### Scenario: Role-based selector finds edit on users", text)
        self.assertIn("opens the users administration screen", text)
        self.assertIn("“Editar”", text)
        self.assertIn("“Eliminar”", text)
        self.assertIn("/editar/i", text)
        self.assertIn("personas", text)
        self.assertNotIn("edit on usuarios", text)
        self.assertNotIn("Usuarios administration", text)
        self.assertIn("edit on usuarios", archived)
        self.assertIn("Usuarios administration", archived)
        self.assertNotIn("opens the users administration screen", archived)


if __name__ == "__main__":
    unittest.main()
