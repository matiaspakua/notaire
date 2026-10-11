#!/usr/bin/env python3
"""ADR-023 names the registration-draft create. The spec id and gloss stay."""

import unittest
from pathlib import Path

DOCS = Path(__file__).resolve().parents[1]
ADR = DOCS / "200-architecture" / "202-ADR" / "ADR-023-rest-resource-naming.md"
SPEC = DOCS / "openspec" / "specs" / "minuta-inscripcion" / "spec.md"


class AdrRegistrationDraftEnglishTest(unittest.TestCase):
    def test_consequence_names_the_registration_draft(self):
        text = ADR.read_text(encoding="utf-8")
        self.assertIn("registration-draft create", text)
        self.assertIn("`201`", text)
        self.assertIn("`POST /pagos/params`", text)
        self.assertNotIn("/payments/params", text)
        self.assertNotIn("minuta create", text)

    def test_spec_id_and_gloss_stay(self):
        text = SPEC.read_text(encoding="utf-8")
        self.assertIn("# minuta-inscripcion Specification", text)
        self.assertIn("Minuta de Inscripción", text)
        self.assertIn("POST /api/v1/registration-drafts", text)


if __name__ == "__main__":
    unittest.main()
