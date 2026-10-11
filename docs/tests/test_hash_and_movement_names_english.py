#!/usr/bin/env python3
"""Legacy MD5 locals and testimony-movement test names are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class HashAndMovementNamesEnglishTest(unittest.TestCase):
    def test_legacy_hash_locals_are_english(self):
        source = read(
            "backend-api/src/main/java/com/licensis/notaire/adapter/in/web/user/UserController.java"
        )
        self.assertIn("plainText", source)
        self.assertIn("md5Hex", source)
        self.assertIn("lowNibble", source)
        self.assertIn("highNibble", source)
        self.assertNotIn("stringAEncriptar", source)
        self.assertNotIn("CadenaMD5", source)
        self.assertNotIn("int bajo", source)
        self.assertNotIn("int alto", source)

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
