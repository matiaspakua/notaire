#!/usr/bin/env python3
"""Unarchived task lists name the screen in English and keep template headings."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


class ChangeTasksEnglishTest(unittest.TestCase):
    def test_case_summary_tasks_name_the_managements_screen(self):
        text = (CHANGES / "feat-774-gestion-resumen-caso" / "tasks.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("feat/774_gestion_workflow_links", text)
        self.assertIn("feat-774-gestion-resumen-caso", text)
        self.assertIn("## 2. Crear branch", text)
        self.assertIn("## 9. Commits atómicos", text)
        self.assertIn("## 10. Pull Request y validación CI", text)
        self.assertIn("## 12. Gate 5 — Smoke test y cierre", text)
        self.assertIn(
            "[#774] feat(managements): add a case summary of deeds, testimonies, copies and payments",
            text,
        )
        self.assertIn("the case summary opens from the managements screen", text)
        for retired in ("gestiones", "escrituras", "testimonios", "pagos"):
            self.assertNotIn(retired, text, retired)

    def test_assessment_tasks_name_the_create_management_diagram(self):
        text = (CHANGES / "docs-921-business-assessment" / "tasks.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("## 4. Implementación", text)
        self.assertIn("`GESTION.md`", text)
        self.assertIn("create-management diagram", text)
        self.assertNotIn("create-gestión", text)


if __name__ == "__main__":
    unittest.main()
