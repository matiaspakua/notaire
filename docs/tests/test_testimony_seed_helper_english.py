#!/usr/bin/env python3
"""The deed-required design names the live testimony seed helper."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DESIGN = (
    ROOT
    / "docs/openspec/changes/fix-655-testimony-requires-deed/design.md"
)
HELPERS = ROOT / "testing/e2e/tests/setup/api-helpers.ts"


class TestimonySeedHelperEnglishTest(unittest.TestCase):
    def test_design_names_the_live_helper(self):
        design = DESIGN.read_text(encoding="utf-8")
        helpers = HELPERS.read_text(encoding="utf-8")
        self.assertIn("`createTestimony`", design)
        self.assertIn("TS-0032", design)
        self.assertIn("retired helper name `createTestimonio`", design)
        self.assertNotIn("(`createTestimonio`, TS-0032)", design)
        self.assertIn("export async function createTestimony(", helpers)
        self.assertNotIn("createTestimonio", helpers)


if __name__ == "__main__":
    unittest.main()
