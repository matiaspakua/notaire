#!/usr/bin/env python3
"""The required-documents dialog comment is English. The visible section name stays."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DIALOG = (
    "frontend/src/app/dashboard/administration/procedures/RequiredDocumentsDialog.tsx"
)


class RequiredDocumentsCommentEnglishTest(unittest.TestCase):
    def test_dialog_comment_is_english(self):
        text = (ROOT / DIALOG).read_text(encoding="utf-8")
        self.assertIn("administration section (Administración)", text)
        self.assertNotIn("like the rest of Administración", text)
        self.assertIn("ADMIN only", text)


if __name__ == "__main__":
    unittest.main()
