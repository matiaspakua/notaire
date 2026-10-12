#!/usr/bin/env python3
"""API coverage notes name the live English fields. Stored status Firmada stays."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COVERAGE = ROOT / "backend-api/api-test/COVERAGE.md"
API = ROOT / "backend-api/src/main/java/com/licensis/notaire"


class ApiCoverageDefectNamesEnglishTest(unittest.TestCase):
    def test_defect_notes_use_english_identifiers(self):
        text = COVERAGE.read_text(encoding="utf-8")
        for live in (
            "Boolean expires",
            "DtoDocumentType.isExpires()",
            'column "characters"',
            "added `characters` field",
            "null `associatesProperties`",
            "null `enabled`",
            "findFirstByFkIdPersonIdPerson",
            "omitting `password`",
            "`fkIdBudget` silently dropped",
            "Firmada",
        ):
            self.assertIn(live, text, live)
        for retired in (
            "vence",
            "DtoTipoDeDocumento",
            "isVence",
            "caracteres",
            "asociaInmuebles",
            "habilitado",
            "contrasenia",
            "fkIdPresupuesto",
            "FkIdPersona",
        ):
            self.assertNotIn(retired, text, retired)

    def test_live_code_uses_those_english_names(self):
        document_type = (API / "dto/DtoDocumentType.java").read_text(encoding="utf-8")
        self.assertIn("boolean isExpires()", document_type)
        users = (API / "repository/UserRepository.java").read_text(encoding="utf-8")
        self.assertIn("findFirstByFkIdPersonIdPerson", users)
        items = (API / "business/Item.java").read_text(encoding="utf-8")
        self.assertIn("fkIdBudget", items)


if __name__ == "__main__":
    unittest.main()
