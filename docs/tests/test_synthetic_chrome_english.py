#!/usr/bin/env python3
"""Synthetic dialog, header, and deed-fixture strings are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FRONT = ROOT / "frontend/src/tests/unit"
JAVA = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


class SyntheticChromeEnglishTest(unittest.TestCase):
    def test_header_and_dialog_props_are_english(self):
        header = read(FRONT / "app-header.test.tsx")
        self.assertIn('title="People"', header)
        self.assertIn('description="Client management"', header)
        self.assertIn(">New person<", header)
        self.assertNotIn("Personas", header)
        self.assertNotIn("Gestión", header)
        self.assertNotIn("Nueva persona", header)

        confirm = read(FRONT / "confirm-dialog.test.tsx")
        self.assertIn('title="Delete person"', confirm)
        self.assertIn('description="This will be deleted permanently."', confirm)
        self.assertNotIn("Eliminar", confirm)
        self.assertNotIn("eliminará", confirm)

        dialog = read(FRONT / "dialog.test.tsx")
        self.assertIn('title="Edit budget"', dialog)
        self.assertIn('title="Budget summary"', dialog)
        self.assertIn('title="Items"', dialog)
        self.assertIn('title="Template"', dialog)
        self.assertIn('title="New person"', dialog)
        self.assertIn('name: "Cerrar"', dialog)
        self.assertIn('name: "Close"', dialog)
        self.assertIn('.toBe("Cerrar")', dialog)
        self.assertNotIn("presupuesto", dialog)
        self.assertNotIn("Ítems", dialog)
        self.assertNotIn("Plantilla", dialog)
        self.assertNotIn("Nueva persona", dialog)

    def test_layout_and_table_fixtures_are_english(self):
        layout = read(FRONT / "dashboard-layout.test.tsx")
        self.assertIn('title="People"', layout)
        self.assertIn('title="Managements"', layout)
        self.assertIn(">Managements<", layout)
        self.assertIn("Saltar al contenido", layout)
        self.assertIn("Skip to content", layout)
        self.assertNotIn("Personas", layout)
        self.assertNotIn("Gestiones", layout)

        table = read(FRONT / "data-table-mobile.test.tsx")
        self.assertIn('header: "Name"', table)
        self.assertIn('header: "Internal"', table)
        self.assertIn("Edit ${r.name}", table)
        self.assertIn("Edit Ana López", table)
        self.assertIn('emptyMessage="Nothing"', table)
        self.assertIn("Juan García", table)
        self.assertIn("Ana López", table)
        self.assertNotIn("Nombre", table)
        self.assertNotIn("Interno", table)
        self.assertNotIn("Editar", table)
        self.assertNotIn("Nada", table)

        button = read(FRONT / "button.test.tsx")
        self.assertIn('aria-label="Edit"', button)
        self.assertNotIn("Editar", button)

    def test_deed_fixture_names_are_english(self):
        repository = read(JAVA / "integration/BudgetTemplateRepositoryIntegrationTest.java")
        self.assertIn("Deed for Compraventa", repository)
        self.assertIn("Honorarios", repository)
        self.assertIn("Poder", repository)
        self.assertIn("Expenses", repository)
        self.assertIn("Before delete", repository)
        self.assertNotIn("Deed de", repository)
        self.assertNotIn("Gastos", repository)
        self.assertNotIn("Antes", repository)

        service = read(JAVA / "integration/BudgetTemplateServiceIntegrationTest.java")
        self.assertIn("Deed for Compraventa", service)
        self.assertIn("Honorarios", service)
        self.assertIn("Expenses", service)
        self.assertNotIn("Deed de", service)
        self.assertNotIn("Gastos", service)

        entity = read(JAVA / "unit/BudgetTemplateEntityTest.java")
        self.assertIn("Deed of sale", entity)
        self.assertIn("Honorarios", entity)
        self.assertNotIn("Deed de", entity)
        self.assertNotIn("Venta", entity)

        catalog = read(JAVA / "integration/CatalogControllersIntegrationTest.java")
        self.assertIn("Deed of sale", catalog)
        self.assertIn("Comprador", catalog)
        self.assertNotIn("Deed de", catalog)
        self.assertNotIn("Venta", catalog)

        deed = read(JAVA / "unit/DeedEntityTest.java")
        self.assertIn("Sale deed", deed)
        self.assertIn("Notarial body", deed)
        self.assertIn("FIRMADA", deed)
        self.assertNotIn("compraventa", deed)
        self.assertNotIn("Cuerpo", deed)

        document = read(JAVA / "unit/SubmittedDocumentEntityTest.java")
        self.assertIn("Sale deed", document)
        self.assertNotIn("compraventa", document)

        summary = read(JAVA / "unit/ManagementCaseSummaryServiceTest.java")
        self.assertIn('new Date(), "body", "Firmada"', summary)
        self.assertIn("Compraventa Pérez", summary)
        self.assertNotIn("cuerpo", summary)


if __name__ == "__main__":
    unittest.main()
