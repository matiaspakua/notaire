#!/usr/bin/env python3
"""Property form fields and leftover property fixtures use English words."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
API = ROOT / "backend-api/src/test/java/com/licensis/notaire"
PAGE = ROOT / "frontend/src/app/dashboard/properties/page.tsx"


class PropertyWordsEnglishTest(unittest.TestCase):
    def test_property_form_fields_are_english(self):
        page = PAGE.read_text(encoding="utf-8")
        self.assertIn('useTranslations("inmuebles")', page)
        self.assertIn('t("fields.nomenclatura")', page)
        self.assertIn('t("fields.nomenclaturaPlaceholder")', page)
        self.assertIn('t("fields.domicilio")', page)
        self.assertIn('t("fields.valuacionFiscal")', page)
        self.assertIn('t("fields.matricula")', page)
        self.assertIn('t("fields.tomoFolioFinca")', page)
        self.assertIn('t("fields.tomoFolioFincaPlaceholder")', page)
        self.assertIn('t("fields.linderos")', page)
        self.assertIn('t("fields.linderosPlaceholder")', page)
        self.assertIn('t("registralData")', page)
        self.assertEqual(page.count("tomoFolioFinca"), 2)
        self.assertEqual(page.count("linderos"), 2)
        self.assertNotIn("valuacion-fiscal", page)
        self.assertNotIn("input-nomenclatura", page)
        self.assertIn('data-testid="input-cadastral-designation"', page)
        self.assertIn('data-testid="input-fiscal-appraisal"', page)
        self.assertIn('data-testid="input-boundaries"', page)
        self.assertIn("volumeFolioLandRecord:", page)
        self.assertIn("boundaries:", page)

    def test_property_fixtures_are_english(self):
        validation = (API / "integration/PropertyRequestValidationIntegrationTest.java").read_text(
            encoding="utf-8"
        )
        self.assertIn("Without designation", validation)
        self.assertIn("Another street", validation)
        self.assertIn('\\"Null\\"', validation)
        self.assertIn('\\"Empty\\"', validation)
        self.assertIn('\\"Blank\\"', validation)
        self.assertNotIn("nomenclatura", validation)
        self.assertNotIn("Otra", validation)
        self.assertNotIn("Vacia", validation)
        self.assertNotIn("Nula", validation)
        self.assertNotIn("Blanca", validation)

        for relative in (
            "unit/RegistrationDraftServiceTest.java",
            "integration/CoreBusinessControllersIntegrationTest.java",
        ):
            source = (API / relative).read_text(encoding="utf-8")
            self.assertIn("North, South, East, West", source, relative)
            self.assertNotIn("Norte, Sur, Este, Oeste", source, relative)

        draft = (API / "unit/RegistrationDraftServiceTest.java").read_text(encoding="utf-8")
        self.assertIn("cadastral/registry", draft)
        self.assertNotIn("catastral", draft)
        self.assertNotIn("registral", draft)

        payment = (API / "unit/PaymentEntityTest.java").read_text(encoding="utf-8")
        self.assertIn("not a payment", payment)
        self.assertNotIn("pago", payment)

        helpers = (ROOT / "testing/e2e/tests/gherkin-helpers.ts").read_text(encoding="utf-8")
        self.assertIn('address: "Fake Street 123"', helpers)
        self.assertIn('number: "DEED-001"', helpers)
        self.assertNotIn("Calle Falsa", helpers)
        self.assertNotIn("ESC-001", helpers)
        self.assertIn("Pérez", helpers)
        self.assertIn("Soltero", helpers)
        self.assertIn("Firmada", helpers)
        self.assertIn("Efectivo", helpers)

        properties = (ROOT / "testing/e2e/tests/TS-0019-properties-valuation-workflow.spec.ts").read_text(
            encoding="utf-8"
        )
        self.assertIn("Fake Street 123", properties)
        self.assertIn("Fake Street 456", properties)
        self.assertIn("Street without designation", properties)
        self.assertNotIn("Calle", properties)
        self.assertIn("Domicilio", properties)
        self.assertIn("Nomenclatura Catastral", properties)

        registration = (ROOT / "testing/e2e/tests/TS-0082-registration-draft-feature.spec.ts").read_text(
            encoding="utf-8"
        )
        self.assertIn("Siempreviva Avenue 742", registration)
        self.assertIn("Volume 1 Folio 2", registration)
        self.assertIn("North, South, East, West", registration)
        self.assertNotIn("Siempreviva 742", registration)
        self.assertNotIn("Tomo ", registration)
        self.assertNotIn("Norte, Sur, Este, Oeste", registration)


if __name__ == "__main__":
    unittest.main()
