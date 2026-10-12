#!/usr/bin/env python3
"""The unused success-toast default is English. Visible Spanish toasts stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
HELPERS = ROOT / "testing/e2e/tests/gherkin-helpers.ts"
DEEDS = ROOT / "testing/e2e/tests/TS-0013-deeds-signing-workflow.spec.ts"
DOCS = ROOT / "testing/e2e/tests/TS-0012-documentation-testimony-workflow.spec.ts"


class SuccessToastDefaultEnglishTest(unittest.TestCase):
    def test_unused_default_is_english_and_visible_toasts_stay(self):
        helpers = HELPERS.read_text(encoding="utf-8")
        self.assertIn('message: string = "success"', helpers)
        self.assertNotIn('message: string = "éxito"', helpers)
        self.assertIn(
            "name: /confirmar|guardar|crear|registrar|actualizar/i", helpers
        )
        self.assertIn('buttonName: string = "nuevo|registrar"', helpers)

        deeds = DEEDS.read_text(encoding="utf-8")
        self.assertIn("/creada|éxito/i", deeds)

        testimony = DOCS.read_text(encoding="utf-8")
        self.assertIn("thenShowsSuccessMessage('inscripción registrada')", testimony)


if __name__ == "__main__":
    unittest.main()
