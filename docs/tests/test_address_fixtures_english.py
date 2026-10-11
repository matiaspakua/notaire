#!/usr/bin/env python3
"""Synthetic addresses, payment notes, and process-payment names are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
API = ROOT / "backend-api/src/test/java/com/licensis/notaire"


def read(relative: str) -> str:
    return (API / relative).read_text(encoding="utf-8")


class AddressFixturesEnglishTest(unittest.TestCase):
    def test_addresses_and_designations_are_english(self):
        files = (
            "integration/PropertyRepositoryIntegrationTest.java",
            "integration/PropertyServiceIntegrationTest.java",
            "integration/PropertyFiscalAppraisalPgIntegrationTest.java",
            "integration/RequestBodyDtoBindingIntegrationTest.java",
            "integration/PropertyRequestValidationIntegrationTest.java",
            "integration/PersonRepositoryIntegrationTest.java",
            "integration/CoreBusinessControllersIntegrationTest.java",
            "unit/PersonEntityTest.java",
            "unit/RegistrationDraftServiceTest.java",
            "unit/ProcedureEntityTest.java",
        )
        for name in files:
            source = read(name)
            self.assertNotIn("Calle", source, name)
            self.assertNotIn("Nomenclatura", source, name)
            self.assertNotIn("Apartamento", source, name)
            self.assertNotIn("Piso", source, name)
            self.assertNotIn("Falsa", source, name)
            self.assertNotIn("valuacion", source, name)
            self.assertNotIn("Honorario", source, name)

        repository = read("integration/PropertyRepositoryIntegrationTest.java")
        self.assertIn("Designation 123", repository)
        self.assertIn("Main Street 123, Apartment 4B", repository)
        self.assertIn("Corrientes Avenue 1234", repository)
        self.assertNotIn("Avenida", repository)
        self.assertIn('"Street " + i', repository)
        self.assertIn("Floor 50", repository)
        self.assertIn('startsWith("Street X")', repository)

        service = read("integration/PropertyServiceIntegrationTest.java")
        self.assertIn("fiscal appraisal", service)
        self.assertIn("Batch Street ", service)

        core = read("integration/CoreBusinessControllersIntegrationTest.java")
        self.assertIn("Additional fee", core)
        self.assertIn("Fake Street 123", core)

        people = read("integration/PersonRepositoryIntegrationTest.java")
        self.assertIn("Fake Street 123", people)
        self.assertIn("Pérez", people)

    def test_payment_notes_and_sample_names_are_english(self):
        integration = read("integration/PaymentIntegrationTest.java")
        self.assertIn("First payment", integration)
        self.assertIn("Edited", integration)
        self.assertIn("Efectivo", integration)
        self.assertIn("Transferencia", integration)
        self.assertNotIn("Primer", integration)
        self.assertNotIn("Editado", integration)

        use_case = read("integration/PaymentUseCaseIntegrationTest.java")
        self.assertIn("First payment", use_case)
        self.assertIn('"Edited"', use_case)
        self.assertNotIn("Primer", use_case)
        self.assertNotIn("Editado", use_case)
        self.assertNotIn("pago", use_case)

        unit = read("unit/PaymentUseCaseTest.java")
        self.assertIn("Partial payment", unit)
        self.assertIn("WhenProcessingPayment", unit)
        self.assertIn("OnProcessingPayment", unit)
        self.assertIn("class ProcessPaymentTests", unit)
        self.assertIn("Efectivo", unit)
        self.assertIn("Transferencia", unit)
        self.assertNotIn("parcial", unit)
        self.assertNotIn("Editado", unit)
        self.assertNotIn("Procesar", unit)

        entity = read("unit/PaymentEntityTest.java")
        self.assertIn("class ProcessPaymentTests", entity)
        self.assertNotIn("Procesar", entity)

        service = read("application/usecase/payment/ProcessPaymentServiceTest.java")
        self.assertIn("Process a payment", service)
        self.assertNotIn("Procesar", service)

        role = read("integration/RoleRequestValidationIntegrationTest.java")
        self.assertIn("roleValid655", role)
        self.assertIn("modulos", role)
        self.assertNotIn("Valido", role)

        identification = read("integration/IdentificationTypeValidationIntegrationTest.java")
        self.assertIn("typeValid655", identification)
        self.assertNotIn("Valido", identification)


if __name__ == "__main__":
    unittest.main()
