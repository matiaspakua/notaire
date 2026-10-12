#!/usr/bin/env python3
"""The workflow active checkbox uses an English test id."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PAGE = ROOT / "frontend/src/app/dashboard/administration/workflows/page.tsx"


class WorkflowActiveTestIdEnglishTest(unittest.TestCase):
    def test_active_checkbox_testid_is_english(self):
        page = PAGE.read_text(encoding="utf-8")
        self.assertIn('useTranslations("administracion.workflows")', page)
        self.assertIn('data-testid="input-name-workflow"', page)
        self.assertIn('data-testid="btn-save-workflow"', page)
        self.assertIn('data-testid="checkbox-active-workflow"', page)
        self.assertNotIn("checkbox-activo-workflow", page)
        self.assertIn('label={tc("active")}', page)


if __name__ == "__main__":
    unittest.main()
