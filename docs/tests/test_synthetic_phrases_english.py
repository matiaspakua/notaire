#!/usr/bin/env python3
"""Leftover synthetic phrases and local names are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FRONT = ROOT / "frontend/src/tests/unit"
JAVA = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


class SyntheticPhrasesEnglishTest(unittest.TestCase):
    def test_table_and_people_fixtures_are_english(self):
        table = read(FRONT / "data-table.test.tsx")
        self.assertIn('header: "Name"', table)
        self.assertIn('emptyMessage="No data available"', table)
        self.assertIn("Juan García", table)
        self.assertIn("Ana López", table)
        self.assertIn("No data", table)
        self.assertNotIn("Nombre", table)
        self.assertNotIn("Sin datos", table)

        pages = read(FRONT / "pages.test.tsx")
        self.assertIn('firstName: "Client A"', pages)
        self.assertIn('firstName: "Employee B"', pages)
        self.assertIn('firstName: "Client C"', pages)
        self.assertIn('"PAID"', pages)
        self.assertIn('"CANCELLED"', pages)
        self.assertIn("PENDIENTE", pages)
        self.assertIn("En proceso", pages)
        self.assertIn("Compraventa", pages)
        self.assertIn("Juan", pages)
        self.assertNotIn("Cliente", pages)
        self.assertNotIn("Empleado", pages)
        self.assertNotIn("PAGADO", pages)
        self.assertNotIn("CANCELADO", pages)

    def test_notes_and_local_names_are_english(self):
        budgets = read(JAVA / "integration/BudgetRepositoryIntegrationTest.java")
        self.assertIn("Approved by the client", budgets)
        self.assertIn("APROBADO", budgets)
        self.assertIn("String[] statuses", budgets)
        self.assertIn("uniqueHeading", budgets)
        self.assertIn("List<Budget> created", budgets)
        self.assertIn("List<Budget> approved", budgets)
        self.assertIn("CANCELLED", budgets)
        self.assertIn("BORRADOR", budgets)
        self.assertNotIn("Aprobado", budgets)
        self.assertNotIn("estados", budgets)
        self.assertNotIn("headingUnico", budgets)
        self.assertNotIn("creados", budgets)
        self.assertNotIn("aprobados", budgets)
        self.assertNotIn("CANCELADO", budgets)

        payment = read(JAVA / "adapter/in/web/payment/PaymentControllerTest.java")
        self.assertIn("notes only", payment)
        self.assertNotIn("solo notas", payment)

        trace = read(JAVA / "integration/WorkflowTraceApiH2IntegrationTest.java")
        self.assertIn("procedure type", trace)
        self.assertIn("statuses", trace)
        self.assertNotIn("tipo", trace)
        self.assertNotIn("estados", trace)

        procedure = read(JAVA / "integration/ProcedureSerializationIntegrationTest.java")
        self.assertIn("budget cycle IT", procedure)
        self.assertIn("procedure cycle IT", procedure)
        self.assertIn("Cycle IT Status", procedure)
        self.assertIn("PENDIENTE", procedure)
        self.assertNotIn("ciclo", procedure)

        template = read(JAVA / "integration/BudgetTemplateSerializationIntegrationTest.java")
        self.assertIn("template cycle IT", template)
        self.assertIn("Concept Cycle IT", template)
        self.assertNotIn("ciclo", template)


if __name__ == "__main__":
    unittest.main()
