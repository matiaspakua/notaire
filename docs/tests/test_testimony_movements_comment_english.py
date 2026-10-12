#!/usr/bin/env python3
"""The testimony-movements dialog comment names the screen in English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DIALOG = (
    ROOT
    / "frontend/src/app/dashboard/testimonies/TestimonyMovementsDialog.tsx"
)
PROPOSAL = (
    ROOT
    / "docs/openspec/changes/fix-579-constraint-errors-400-testimony-person/proposal.md"
)


class TestimonyMovementsCommentEnglishTest(unittest.TestCase):
    def test_dialog_comment_names_the_screen_in_english(self):
        text = DIALOG.read_text(encoding="utf-8")
        self.assertIn("the testimony-movements screen", text)
        self.assertIn("manual correction stays API-only", text)
        self.assertNotIn("Movimientos de testimonio", text)
        proposal = PROPOSAL.read_text(encoding="utf-8")
        self.assertIn("CU08 – Movimientos de testimonio", proposal)
        catalog = (ROOT / "frontend/messages/es.json").read_text(encoding="utf-8")
        self.assertIn("Movimientos de testimonio", catalog)


if __name__ == "__main__":
    unittest.main()
