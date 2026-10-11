#!/usr/bin/env python3
"""Verification identifiers, the withdrawal method, and nearby locals are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class VerificationIdentifiersEnglishTest(unittest.TestCase):
    def test_verification_field_and_class_are_english(self):
        controller = read(
            "backend-api/src/main/java/com/licensis/notaire/adapter/in/web/testimony/TestimonyController.java"
        )
        self.assertIn("generationVerificationService", controller)
        self.assertNotIn("generationVerificacionService", controller)

        simple = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/SimpleControllersTest.java"
        )
        self.assertIn("generationVerificationService", simple)
        self.assertNotIn("generationVerificacionService", simple)

        renamed = ROOT / (
            "backend-api/src/test/java/com/licensis/notaire/unit/"
            "TestimonyVerificationServiceTest.java"
        )
        old = ROOT / (
            "backend-api/src/test/java/com/licensis/notaire/unit/"
            "TestimonyVerificacionServiceTest.java"
        )
        self.assertTrue(renamed.is_file())
        self.assertFalse(old.exists())
        self.assertIn("class TestimonyVerificationServiceTest", renamed.read_text(encoding="utf-8"))
        self.assertNotIn("TestimonyVerificacionServiceTest", renamed.read_text(encoding="utf-8"))

        notes = read("docs/tests/test_synthetic_notes_english.py")
        self.assertIn("TestimonyVerificationServiceTest.java", notes)
        self.assertNotIn("TestimonyVerificacionServiceTest.java", notes)

    def test_withdrawal_jpa_audit_and_index_names_are_english(self):
        movement = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/TestimonyMovementServiceTest.java"
        )
        self.assertIn("shouldRejectWithdrawalWhenTestimonyNotFound", movement)
        self.assertNotIn("shouldRejectRetirarWhenTestimonyNotFound", movement)

        jpa = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/jpa/JpaAdministratorTest.java"
        )
        self.assertIn("class LoadJpaList", jpa)
        self.assertNotIn("CargarListaJpas", jpa)

        audit = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "AuditRecordRepositoryIntegrationTest.java"
        )
        self.assertIn("AuditRecord record2", audit)
        self.assertNotIn("registro2", audit)

        reports = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/ReportsUseCaseIntegrationTest.java"
        )
        self.assertIn("shouldGenerateIndexBookPdf", reports)
        self.assertNotIn("shouldGenerateLibroIndicePdf", reports)
        self.assertIn("/api/v1/reports/index-book", reports)

        jacoco = read(
            "backend-api/src/test/java/com/licensis/notaire/unit/"
            "JacocoCoverageConfigConsistencyTest.java"
        )
        self.assertIn("shouldNotReferenceNonexistentServicesPackageInPomExclusions", jacoco)
        self.assertNotIn("shouldNotReferenceNonexistentServiciosPackageInPomExclusions", jacoco)
        self.assertIn("com/licensis/notaire/servicios/", jacoco)


if __name__ == "__main__":
    unittest.main()
