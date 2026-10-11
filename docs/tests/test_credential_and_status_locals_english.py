#!/usr/bin/env python3
"""Credential parameters, paid-status tests, and fixture locals are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class CredentialAndStatusLocalsEnglishTest(unittest.TestCase):
    def test_credential_parameters_are_english(self):
        auth = read("testing/e2e/tests/setup/auth.ts")
        self.assertIn('password: string = "admin"', auth)
        self.assertNotIn("contrasenia", auth)

        guard = read("testing/e2e/tests/TS-0094-admin-route-guard.spec.ts")
        self.assertIn("password: string", guard)
        self.assertNotIn("contrasenia", guard)
        self.assertIn('"EMPLEADO"', guard)

        login = read("testing/e2e/tests/TS-0001-login-authentication.spec.ts")
        self.assertIn("invalid_user", login)
        self.assertIn("invalid_password", login)
        self.assertNotIn("usuario_invalido", login)
        self.assertNotIn("contrasenia_invalida", login)
        self.assertIn("Complete usuario y contraseña", login)

        nav = read("testing/e2e/tests/TS-0003-dashboard-navigation.spec.ts")
        helpers = read("testing/e2e/tests/gherkin-helpers.ts")
        self.assertIn('"employee"', nav)
        self.assertIn('"employee"', helpers)
        self.assertNotIn('"empleado"', nav)
        self.assertNotIn('"empleado"', helpers)

    def test_status_methods_and_fixture_locals_are_english(self):
        payment = read(
            "backend-api/src/test/java/com/licensis/notaire/adapter/out/persistence/"
            "payment/PaymentPersistenceAdapterTest.java"
        )
        self.assertIn("shouldCalculateStatusPaymentPaid", payment)
        self.assertIn("shouldEditPayment", payment)
        self.assertNotIn("Saldado", payment)
        self.assertNotIn("Editar", payment)
        self.assertIn("PaymentStatus.PAID", payment)

        draft = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/RegistrationDraftServiceTest.java"
        )
        self.assertIn("shouldRejectSubmissionWhenNotGenerated", draft)
        self.assertNotIn("NotGenerada", draft)
        self.assertIn("status Generada", draft)

        auxiliary = read("testing/e2e/tests/TS-0081-auxiliary-protocol-workflow.spec.ts")
        self.assertIn("auxiliaryType", auxiliary)
        self.assertNotIn("tipoAuxiliar", auxiliary)
        self.assertIn("/iniciar escritura/i", auxiliary)

        store = read("frontend/src/tests/unit/auth-store.test.ts")
        self.assertIn("employeeUser", store)
        self.assertIn('name: "employee1"', store)
        self.assertNotIn("empleadoUser", store)
        self.assertNotIn("empleado1", store)
        self.assertIn('"EMPLEADO"', store)

        reports = read("frontend/src/tests/unit/substitutions-reports.test.ts")
        self.assertIn("Monthly sworn-statement filename includes year and month", reports)
        self.assertIn("Tax sworn-statement filename includes year and month", reports)
        self.assertNotIn("DDJJ", reports)
        self.assertIn("ddjj_mensual_", reports)
        self.assertIn("ddjj_rentas_", reports)


if __name__ == "__main__":
    unittest.main()
