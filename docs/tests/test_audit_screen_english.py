#!/usr/bin/env python3
"""The living navigation spec names the audit screen."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LIVING = (
    ROOT
    / "docs"
    / "openspec"
    / "specs"
    / "dashboard-nav-canonical-routes"
    / "spec.md"
)
ARCHIVED = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-fix-1058-suplencias-reportes-nav"
    / "specs"
    / "dashboard-nav-canonical-routes"
    / "spec.md"
)
DESIGN = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "docs-921-engineering-sequences"
    / "design.md"
)
USE_CASE = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU23 – Ver registro de actividades de usuario.md"
)
SCREEN = "`/dashboard/auditoria`"
LEGACY = "`/dashboard/administracion/auditoria`"
LIVE = "`/dashboard/audit`"
LIVE_ADMIN = "`/dashboard/administration/audit`"


class AuditScreenEnglishTest(unittest.TestCase):
    def test_living_spec_names_the_screen(self):
        text = LIVING.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertIn(LIVE_ADMIN, text)
        self.assertNotIn(SCREEN.strip("`"), text)
        self.assertNotIn(LEGACY.strip("`"), text)
        self.assertIn("`auditoria`", text)

    def test_design_cites_the_screens_cu23_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = USE_CASE.read_text(encoding="utf-8")
        archived = ARCHIVED.read_text(encoding="utf-8")
        self.assertIn(SCREEN, text)
        self.assertIn(LEGACY, text)
        self.assertIn("`dashboard-nav-canonical-routes`", text)
        self.assertIn(SCREEN.strip("`"), use_case)
        self.assertIn(LEGACY.strip("`"), use_case)
        self.assertIn(SCREEN.strip("`"), archived)
        self.assertIn(LEGACY.strip("`"), archived)
        self.assertNotIn(LIVE, archived)
        self.assertNotIn(LIVE_ADMIN, archived)


if __name__ == "__main__":
    unittest.main()
