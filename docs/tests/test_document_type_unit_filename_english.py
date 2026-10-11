#!/usr/bin/env python3
"""The document-type form unit test uses an English filename."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
UNIT = ROOT / "frontend" / "src" / "tests" / "unit"


class DocumentTypeUnitFilenameEnglishTest(unittest.TestCase):
    def test_unit_filename_is_english(self):
        live = UNIT / "document-type-enabled-returned.test.ts"
        retired = UNIT / "tipo-documento-enabled-returned.test.ts"
        self.assertTrue(live.is_file(), live)
        self.assertFalse(retired.exists(), retired)
        text = live.read_text(encoding="utf-8")
        self.assertIn("Document type enabled/returned form defaults", text)
        self.assertIn("EMPTY_DOCUMENT_TYPE", text)
        archive = (
            ROOT
            / "docs/openspec/changes/archive/2026-10-03-feat-800-tipo-documento-defaults/design.md"
        ).read_text(encoding="utf-8")
        self.assertIn("`tipo-documento-enabled-returned.test.ts`", archive)
        self.assertIn("tipo-documento-vencimiento-config", archive)


if __name__ == "__main__":
    unittest.main()
