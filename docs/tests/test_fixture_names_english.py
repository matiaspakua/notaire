#!/usr/bin/env python3
"""Synthetic fixture names and status mocks are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
API = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (API / relative).read_text(encoding="utf-8")


class FixtureNamesEnglishTest(unittest.TestCase):
    def test_person_and_budget_fixtures_are_english(self):
        names = (
            "integration/PaymentRepositoryIntegrationTest.java",
            "integration/BudgetRepositoryIntegrationTest.java",
            "integration/DeedManagementRepositoryIntegrationTest.java",
            "integration/PaymentUseCaseIntegrationTest.java",
            "integration/PaymentIntegrationTest.java",
        )
        for name in names:
            source = read(name)
            self.assertIn('setFirstName("Client")', source, name)
            self.assertNotIn('"Cliente"', source, name)

        budgets = read("integration/BudgetRepositoryIntegrationTest.java")
        self.assertIn("Updatable budget", budgets)
        self.assertIn('"Budget" + System.nanoTime()', budgets)
        self.assertIn("PENDIENTE", budgets)
        self.assertIn("BORRADOR", budgets)
        self.assertIn("APROBADO", budgets)
        self.assertNotIn("Actualizable", budgets)
        self.assertNotIn('"Pres"', budgets)

        people = read("integration/PersonServiceIntegrationTest.java")
        self.assertIn('setFirstName("Lawyer")', people)
        self.assertIn('setLastName("Criminal")', people)
        self.assertNotIn("Abogado", people)
        self.assertNotIn("Penal", people)
        self.assertIn("Pérez", people)

        comment = read("integration/BudgetPersonAssociationPgIntegrationTest.java")
        self.assertIn("Prepare a budget", comment)
        self.assertNotIn("Preparar", comment)

    def test_status_mocks_and_helpers_are_english(self):
        controllers = read("unit/AdditionalControllersTest.java")
        self.assertIn('\\"heading\\":\\"Management\\"', controllers)
        self.assertIn("In progress", controllers)
        self.assertNotIn("Gestion", controllers)
        self.assertNotIn("En Progreso", controllers)

        query = read("service/unit/ManagementQueryServiceTest.java")
        self.assertIn("managementWithoutStatus", query)
        self.assertNotIn("SinStatus", query)

        audit_unit = read("unit/AuditRecordServiceTest.java")
        self.assertIn("findByModule", audit_unit)
        self.assertIn("shouldReturnAllRecords", audit_unit)
        self.assertIn("shouldReturnPagedRecords", audit_unit)
        self.assertNotIn("findByModulo", audit_unit)
        self.assertNotIn("Registros", audit_unit)

        controller = read("unit/AuditRecordControllerTest.java")
        self.assertIn("shouldReturnRecordsByUserId", controller)
        self.assertNotIn("Registros", controller)

        core = read("integration/CoreBusinessControllersIntegrationTest.java")
        self.assertIn("shouldReturnAllAuditRecords", core)
        self.assertNotIn("Registros", core)

        repository = read("integration/AuditRecordRepositoryIntegrationTest.java")
        self.assertIn('"DOCUMENTS"', repository)
        self.assertNotIn("DOCUMENTOS", repository)

        audit = read("service/unit/AuditRecordServiceTest.java")
        self.assertIn("recordWithoutUser", audit)
        self.assertIn('"DOCUMENTS"', audit)
        self.assertNotIn("SinUser", audit)
        self.assertNotIn("DOCUMENTOS", audit)
        self.assertIn("shouldReturnEmptyListWhenNoRecordsExist", audit)
        self.assertIn("shouldFindRecordsByUserIdList", audit)
        self.assertIn("shouldFindRecordsByUserIdAsDto", audit)
        self.assertNotIn("Registros", audit)

        payments = read("adapter/out/persistence/payment/PaymentPersistenceAdapterTest.java")
        self.assertIn("shouldCalculateStatusWithNoPayments", payments)
        self.assertIn("status NoPayments", payments)
        self.assertIn("PaymentStatus.NoPayments", payments)
        self.assertNotIn("SinPayments", payments)
        self.assertNotIn("SIN_PAGOS", payments)


if __name__ == "__main__":
    unittest.main()
