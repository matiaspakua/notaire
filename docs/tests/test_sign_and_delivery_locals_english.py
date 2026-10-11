#!/usr/bin/env python3
"""Sign-deed locals, the delivery flag, and audit record lists are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class SignAndDeliveryLocalsEnglishTest(unittest.TestCase):
    def test_sign_deed_locals_are_english(self):
        page = read("frontend/src/app/dashboard/deeds/page.tsx")
        self.assertIn("UNSIGNED_STATUS", page)
        self.assertIn('= "Sin Firmar"', page)
        self.assertIn("signMutation", page)
        self.assertIn("signId", page)
        self.assertIn("setSignId", page)
        self.assertNotIn("ESTADO_SIN_FIRMAR", page)
        self.assertNotIn("firmarMutation", page)
        self.assertNotIn("firmarId", page)
        self.assertNotIn("setFirmarId", page)
        self.assertIn('t("errorFirmar")', page)
        self.assertIn('t("firmarEscritura")', page)
        self.assertIn('t("firmarConfirmTitle")', page)

    def test_delivery_flag_and_audit_lists_are_english(self):
        service = read(
            "backend-api/src/main/java/com/licensis/notaire/application/usecase/document/"
            "ExternalEntityDocumentService.java"
        )
        self.assertIn("boolean allDelivered", service)
        self.assertNotIn("todosEntregados", service)
        self.assertIn("DocumentationExternalEntity", service)

        audit = read(
            "backend-api/src/test/java/com/licensis/notaire/service/unit/AuditRecordServiceTest.java"
        )
        self.assertIn("List<AuditRecord> records", audit)
        self.assertNotIn("registros", audit)


if __name__ == "__main__":
    unittest.main()
