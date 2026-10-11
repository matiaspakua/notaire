#!/usr/bin/env python3
"""Signing helpers and sequence counters use English identifiers."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
API = ROOT / "backend-api/src"


def read(relative: str) -> str:
    return (API / relative).read_text(encoding="utf-8")


class SigningIdentifiersEnglishTest(unittest.TestCase):
    def test_signing_names_are_english(self):
        service = read("main/java/com/licensis/notaire/application/usecase/deed/DeedSigningService.java")
        self.assertIn("Sign a deed", service)
        self.assertIn("Sin Firmar", service)
        self.assertNotIn("Firmar deed", service)

        signing = read("test/java/com/licensis/notaire/unit/DeedSigningServiceTest.java")
        self.assertIn("deedSigningService", signing)
        self.assertIn("Deed signed", signing)
        self.assertIn("already signed", signing)
        self.assertIn("Sin Firmar", signing)
        self.assertNotIn("deedFirma", signing)
        self.assertNotIn("firmada", signing)

        controller = read("main/java/com/licensis/notaire/adapter/in/web/deed/DeedController.java")
        self.assertIn("deedSigningService", controller)
        self.assertIn("Sin Firmar", controller)
        self.assertIn("Firmada", controller)
        self.assertNotIn("deedFirma", controller)

        controllers = read("test/java/com/licensis/notaire/unit/SimpleControllersTest.java")
        self.assertIn("signingService", controllers)
        self.assertNotIn("firmaService", controllers)

        draft = read("test/java/com/licensis/notaire/unit/RegistrationDraftServiceTest.java")
        self.assertIn("shouldRejectGenerationWhenDeedNotSigned", draft)
        self.assertIn("not signed", draft)
        self.assertIn("Firmada", draft)
        self.assertNotIn("Firmada()", draft)
        self.assertNotIn("firmada", draft)

        integration = API / "test/java/com/licensis/notaire/integration/DeedSigningControllerIntegrationTest.java"
        self.assertTrue(integration.is_file(), integration)
        old = API / "test/java/com/licensis/notaire/integration/DeedFirmaControllerIntegrationTest.java"
        self.assertFalse(old.exists(), old)
        text = integration.read_text(encoding="utf-8")
        self.assertIn("class DeedSigningControllerIntegrationTest", text)
        self.assertIn("Sign a deed", text)
        self.assertIn("shouldRejectSigningAlreadySignedDeed", text)
        self.assertIn('"Firmada"', text)
        self.assertIn("Sin Firmar", text)
        self.assertNotIn("DeedFirma", text)
        self.assertNotIn("Firmar deed", text)
        self.assertNotIn("AlreadyFirmada", text)

    def test_sequence_names_and_justification_are_english(self):
        numbering = read("test/java/com/licensis/notaire/unit/DeedNumberingUseCaseTest.java")
        self.assertIn("int next ", numbering)
        self.assertIn("nextAuxiliary", numbering)
        self.assertIn("nextPrincipal", numbering)
        self.assertIn("Voided deed No. 6 to 8", numbering)
        self.assertIn("Protocolo Auxiliar", numbering)
        self.assertIn("Protocolo Principal", numbering)
        self.assertNotIn("siguiente", numbering)
        self.assertNotIn("anulada", numbering)

        notebook = read("test/java/com/licensis/notaire/integration/NotebookControllerTest.java")
        self.assertIn("nextNumber", notebook)
        self.assertIn("int count", notebook)
        self.assertNotIn("siguiente", notebook)
        self.assertNotIn("cantidad", notebook)

        testimony = read("test/java/com/licensis/notaire/integration/TestimonyControllerIntegrationTest.java")
        self.assertIn("createSignedDeed", testimony)
        self.assertIn("shouldGenerateTestimonyFromSignedDeed", testimony)
        self.assertIn("shouldRejectGenerateTestimonyFromUnsignedDeed", testimony)
        self.assertIn("from a signed deed", testimony)
        self.assertIn("from an unsigned deed", testimony)
        self.assertIn('"Firmada"', testimony)
        self.assertNotIn("FirmadaDeed", testimony)
        self.assertNotIn("createDeedFirmada", testimony)
        self.assertNotIn("non-Firmada", testimony)


if __name__ == "__main__":
    unittest.main()
