#!/usr/bin/env python3
"""Password verify path and testimony-movement test names are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class HashAndMovementNamesEnglishTest(unittest.TestCase):
    def test_password_verify_has_no_legacy_md5_or_spanish_locals(self):
        # #1762 hard cutoff: MessageDigest MD5 verify removed; Spanish MD5
        # locals from the Swing-era helper must not return.
        # After #1731, verify lives in PasswordHasherAdapter (ports-only UserController).
        source = read(
            "backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/user/"
            "PasswordHasherAdapter.java"
        )
        self.assertIn("passwordMatches", source)
        self.assertIn("isBcryptHash", source)
        self.assertNotIn("MessageDigest", source)
        self.assertNotIn('getInstance("MD5")', source)
        self.assertNotIn("legacyMd5Hash", source)
        self.assertNotIn("stringAEncriptar", source)
        self.assertNotIn("CadenaMD5", source)
        self.assertNotIn("int bajo", source)
        self.assertNotIn("int alto", source)

        auth = read(
            "backend-api/src/main/java/com/licensis/notaire/application/usecase/user/"
            "AuthenticateUserService.java"
        )
        self.assertNotIn("MessageDigest", auth)
        self.assertNotIn("legacyMd5Hash", auth)
        self.assertNotIn('getInstance("MD5")', auth)

        controller = read(
            "backend-api/src/main/java/com/licensis/notaire/adapter/in/web/user/"
            "UserController.java"
        )
        self.assertNotIn("MessageDigest", controller)
        self.assertNotIn("legacyMd5Hash", controller)
        self.assertNotIn('getInstance("MD5")', controller)

    def test_movement_names_and_receipt_heading_are_english(self):
        broken = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "PreviouslyBrokenEndpointsIntegrationTest.java"
        )
        self.assertIn("shouldListTestimonyMovementsWhenRequested", broken)
        self.assertNotIn("Movimientos", broken)

        catalog = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "CatalogControllersIntegrationTest.java"
        )
        self.assertIn("shouldReturnEmptyListOfMovements", catalog)
        self.assertNotIn("Movimientos", catalog)

        copies = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/CopyControllerTest.java"
        )
        self.assertIn("shouldCreateCopyWhenTestimonyHasNoMovements", copies)
        self.assertNotIn("Movimientos", copies)

        schema = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "FlywaySchemaValidationIntegrationTest.java"
        )
        self.assertIn("shouldListTestimonyMovementsWhenSchemaMatchesEntities", schema)
        self.assertNotIn("Movimientos", schema)

        entity = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/TestimonyEntityTest.java"
        )
        self.assertIn("List<TestimonyMovement> movements", entity)
        self.assertNotIn("movimientos", entity)

        receipt = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "PaymentReceiptReportIntegrationTest.java"
        )
        self.assertIn("Budget receipt E2E", receipt)
        self.assertNotIn("recibo", receipt)
        self.assertIn("PENDIENTE", receipt)


if __name__ == "__main__":
    unittest.main()
