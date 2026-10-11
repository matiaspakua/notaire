#!/usr/bin/env python3
"""Synthetic started-status and substitute parameter names are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class StartedAndSubstituteNamesEnglishTest(unittest.TestCase):
    def test_started_status_fixtures_are_english(self):
        for relative in (
            "backend-api/src/test/java/com/licensis/notaire/unit/WorkflowEntityTest.java",
            "backend-api/src/test/java/com/licensis/notaire/unit/WorkflowNodeControllerTest.java",
            "testing/e2e/tests/setup/api-helpers.ts",
        ):
            text = read(relative)
            self.assertIn('"Started"', text, relative)
            self.assertNotIn("Iniciado", text, relative)

    def test_substitute_parameters_are_english(self):
        helpers = read("testing/e2e/tests/setup/api-helpers.ts")
        self.assertIn("idSubstitute: number", helpers)
        self.assertIn("idSubstituted: number", helpers)
        self.assertNotIn("idSuplente", helpers)
        self.assertNotIn("idSuplantado", helpers)

        redirect = read("testing/e2e/tests/TS-0092-management-substitution-redirect.spec.ts")
        self.assertIn("const substitute = await createPerson", redirect)
        self.assertIn("substitute.data!.personId", redirect)
        self.assertIn("/suplente/i", redirect)
        self.assertNotIn("const suplente", redirect)

    def test_identification_type_fixture_is_english(self):
        page = read("frontend/src/tests/unit/identification-types-admin.test.tsx")
        self.assertIn('"Identity card"', page)
        self.assertIn('"DNI"', page)
        self.assertNotIn("Cédula", page)


if __name__ == "__main__":
    unittest.main()
