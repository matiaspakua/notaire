#!/usr/bin/env python3
"""E2E file-header comments name administration screens in English."""

import unittest
from pathlib import Path

E2E = Path(__file__).resolve().parents[2] / "testing" / "e2e" / "tests"


class E2EHeaderCommentsEnglishTest(unittest.TestCase):
    def test_procedure_confirm_comment_is_english(self):
        text = (E2E / "TS-0141-required-docs-on-confirm.spec.ts").read_text(encoding="utf-8")
        self.assertIn("when a procedure is confirmed via complete-case", text)
        self.assertNotIn("when a trámite is confirmed", text)

    def test_admin_route_comment_names_the_english_url(self):
        text = (E2E / "TS-0094-admin-route-guard.spec.ts").read_text(encoding="utf-8")
        self.assertIn("open the administration URL", text)
        self.assertIn("/dashboard/administration/users", text)
        self.assertIn('"EMPLEADO"', text)
        self.assertNotIn("administración URL", text)

    def test_admin_module_headers_are_english_and_keep_visible_labels(self):
        smoke = (E2E / "TS-0024-admin-module-smoke.spec.ts").read_text(encoding="utf-8")
        self.assertIn("Playwright E2E tests — Administration module.", smoke)
        self.assertIn("/administración/i", smoke)
        self.assertNotIn("Administración module", smoke)

        tour = (E2E / "TS-0070-supervised-tour-tutorial.spec.ts").read_text(encoding="utf-8")
        self.assertIn("/** Administration sub-pages", tour)
        self.assertIn('name: "Administración"', tour)
        self.assertIn('"Trámites"', tour)
        self.assertNotIn("Administración sub-pages", tour)

        sidebar = (E2E / "TS-0121-grouped-sidebar.spec.ts").read_text(encoding="utf-8")
        self.assertIn("neither Administration nor Audit", sidebar)
        self.assertIn("nav-administracion", sidebar)
        self.assertIn("nav-auditoria", sidebar)
        self.assertNotIn("neither Administración nor Auditoría", sidebar)

        catalog = (E2E / "TS-0139-identification-type-catalog.spec.ts").read_text(encoding="utf-8")
        self.assertIn("ADMIN opens Administration → Identification types", catalog)
        self.assertIn("/dashboard/administration/identification-types", catalog)
        self.assertNotIn("Administración → Tipos de identificación", catalog)


if __name__ == "__main__":
    unittest.main()
