#!/usr/bin/env python3
"""Open change notes name the live catalog Playwright files."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


class CatalogPlaywrightFilenamesEnglishTest(unittest.TestCase):
    def test_budget_template_delete_names_the_live_file(self):
        design = (
            CHANGES / "fix-1315-budget-template-delete-204" / "design.md"
        ).read_text(encoding="utf-8")
        self.assertIn("TS-0131-budget-template.spec.ts", design)
        self.assertNotIn("presupuesto-plantilla", design)
        self.assertIn("## Riesgos / Trade-offs", design)

    def test_catalog_constraint_notes_name_the_live_files(self):
        design = (
            CHANGES / "fix-579-constraint-errors-400-catalogs" / "design.md"
        ).read_text(encoding="utf-8")
        self.assertIn("TS-0134-document-type-expiration-config.spec.ts", design)
        self.assertIn("TS-0130-budget-catalog-items.spec.ts", design)
        self.assertNotIn("tipo-documento-vencimiento-config", design)
        self.assertNotIn("presupuesto-catalogo-items", design)
        self.assertIn("## Riesgos / Trade-offs", design)

    def test_incomplete_put_tasks_name_the_live_files(self):
        tasks = (
            CHANGES / "fix-655-put-rejects-incomplete-bodies" / "tasks.md"
        ).read_text(encoding="utf-8")
        self.assertIn("TS-0135-folio-type-edit-version.spec.ts", tasks)
        self.assertIn("TS-0136-folios-linking.spec.ts", tasks)
        self.assertNotIn("folios-vinculacion", tasks)
        self.assertNotIn("`folio-type-edit-version`", tasks)
        self.assertIn("## 7. Ejecutar Playwright", tasks)
        self.assertIn("## 2. Crear branch", tasks)
        self.assertIn("## 4. Implementación", tasks)


if __name__ == "__main__":
    unittest.main()
