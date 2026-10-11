#!/usr/bin/env python3
"""The endpoint registry and auxiliary-deed check use the live English names."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class RegistryPathNamesEnglishTest(unittest.TestCase):
    def test_registry_names_the_live_role_and_document_path(self):
        registry = read("docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md")
        self.assertIn("### RoleController", registry)
        self.assertNotIn("RolController", registry)
        self.assertIn("/api/v1/roles/{idRole}/users/{idUser}", registry)
        self.assertIn("{idSubmittedDocument}", registry)
        self.assertNotIn("idDocumentoPresentado", registry)
        self.assertIn("/api/v1/reports/documents-due/", registry)

    def test_auxiliary_deed_id_is_english(self):
        spec = read("backend-api/api-test/auxiliary-protocol/04-start-deed.yml")
        self.assertIn("const id = b.idDeed;", spec)
        self.assertNotIn("idEscritura", spec)
        self.assertIn("ap_deed_id", spec)


if __name__ == "__main__":
    unittest.main()
