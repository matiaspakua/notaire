#!/usr/bin/env python3
"""Registration-draft comments name the stored statuses, not the stale participles."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "backend-api" / "src" / "main" / "java" / "com" / "licensis" / "notaire"
STORED = (
    "Generada",
    "Presentado para inscripción",
    "Observado",
    "Inscripto",
)


class RegistrationDraftStatusesEnglishTest(unittest.TestCase):
    def test_comments_and_diagram_quote_the_stored_statuses(self):
        sources = [
            JAVA / "business" / "RegistrationDraft.java",
            JAVA / "application" / "usecase" / "registrationdraft" / "RegistrationDraftService.java",
            ROOT / "docs" / "200-architecture" / "203-design" / "CICLO-DE-VIDA.md",
            ROOT / "docs" / "300-development" / "EVALUACION-INGENIERIA.md",
        ]
        for path in sources:
            text = path.read_text(encoding="utf-8")
            for status in STORED:
                self.assertIn(status, text, f"{path.name} missing {status}")
            self.assertNotIn("Presentada", text, path.name)
            self.assertNotIn("Observada", text, path.name)

        constants = (JAVA / "business" / "BusinessConstants.java").read_text(encoding="utf-8")
        self.assertIn('RegistrationDraftSubmitted = "Presentado para inscripción"', constants)
        self.assertIn('RegistrationDraftObserved = "Observado"', constants)
        self.assertIn('RegistrationDraftGenerated = "Generada"', constants)
        self.assertIn('RegistrationDraftRegistered = "Inscripto"', constants)
        self.assertIn('DeedRegistered = "Inscripta"', constants)
        self.assertNotIn("RegistrationDraftPRESENTADA", constants)
        self.assertNotIn("RegistrationDraftOBSERVADA", constants)
        self.assertNotIn("RegistrationDraftGENERADA", constants)

        service_test = (
            ROOT
            / "backend-api"
            / "src"
            / "test"
            / "java"
            / "com"
            / "licensis"
            / "notaire"
            / "unit"
            / "RegistrationDraftServiceTest.java"
        ).read_text(encoding="utf-8")
        self.assertNotIn("Observacion", service_test)
        self.assertNotIn("Presentada", service_test)
        self.assertNotIn("Definitiva", service_test)


if __name__ == "__main__":
    unittest.main()
