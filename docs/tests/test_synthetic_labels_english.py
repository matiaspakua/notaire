#!/usr/bin/env python3
"""Synthetic form labels and sale headings are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FRONT = ROOT / "frontend/src/tests/unit"
JAVA = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


class SyntheticLabelsEnglishTest(unittest.TestCase):
    def test_form_labels_are_english(self):
        form = read(FRONT / "form-field.test.tsx")
        self.assertIn('label="Name"', form)
        self.assertIn("The name is required", form)
        self.assertIn('label="CUIT"', form)
        self.assertIn("Without hyphens", form)
        self.assertIn('label="Notes"', form)
        self.assertIn("Too long", form)
        self.assertIn('label="Status"', form)
        self.assertIn("Choose a status", form)
        self.assertNotIn("Nombre", form)
        self.assertNotIn("obligatorio", form)
        self.assertNotIn("guiones", form)
        self.assertNotIn("Notas", form)
        self.assertNotIn("Demasiado", form)
        self.assertNotIn("Estado", form)
        self.assertNotIn("Elegí", form)

        select = read(FRONT / "select.test.tsx")
        self.assertIn('aria-label="Status"', select)
        self.assertIn('placeholder="Choose a status"', select)
        self.assertIn("Borrador", select)
        self.assertIn("Aprobado", select)
        self.assertIn("Rechazado", select)
        self.assertNotIn("Estado", select)
        self.assertNotIn("Elegir", select)

        badge = read(FRONT / "status-badge.test.tsx")
        self.assertIn("Status E2E 42", badge)
        self.assertIn("Iniciada", badge)
        self.assertIn("Pendiente", badge)
        self.assertNotIn("Estado E2E", badge)

    def test_picker_fixtures_are_english(self):
        people = read(FRONT / "person-picker.test.tsx")
        self.assertIn('aria-label="Client"', people)
        self.assertIn(">Client<", people)
        self.assertIn("Ana Latest", people)
        self.assertIn("Bruno Recent", people)
        self.assertIn("Carla", people)
        self.assertNotIn("Cliente", people)
        self.assertNotIn("Nueva", people)
        self.assertNotIn("Reciente", people)

        lists = read(FRONT / "management-lists-pagination.test.tsx")
        self.assertIn("New Compraventa", lists)
        self.assertIn("Hipoteca", lists)
        self.assertIn("Donación", lists)
        self.assertIn("Iniciada", lists)
        self.assertNotIn("Nueva", lists)

    def test_sale_headings_are_english(self):
        repository = read(JAVA / "integration/DeedManagementRepositoryIntegrationTest.java")
        self.assertIn("Sale property", repository)
        self.assertIn("parcel sale", repository)
        self.assertIn('"parcel"', repository)
        self.assertNotIn("Venta Property", repository)
        self.assertNotIn("propiedad", repository)

        query = read(JAVA / "service/unit/ManagementQueryServiceTest.java")
        self.assertIn("Sale property", query)
        self.assertNotIn("Venta Property", query)

        deeds = read(JAVA / "service/unit/DeedServiceTest.java")
        self.assertIn("parcel sale", deeds)
        self.assertIn("FIRMADA", deeds)
        self.assertIn("Juan", deeds)
        self.assertNotIn("propiedad", deeds)
        self.assertNotIn("Venta de", deeds)

        budgets = read(JAVA / "service/unit/BudgetServiceTest.java")
        self.assertIn("Sale budget", budgets)
        self.assertIn("APROBADO", budgets)
        self.assertNotIn("Budget Venta", budgets)

        budget = read(JAVA / "unit/BudgetEntityTest.java")
        self.assertIn("Compraventa property", budget)
        self.assertNotIn(" de property", budget)

        procedure = read(JAVA / "unit/ProcedureEntityTest.java")
        self.assertIn("Urban Compraventa property", procedure)
        self.assertIn("Compraventa", procedure)
        self.assertNotIn("urbano", procedure)
        self.assertNotIn(" de property", procedure)


if __name__ == "__main__":
    unittest.main()
