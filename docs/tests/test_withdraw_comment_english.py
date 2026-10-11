#!/usr/bin/env python3
"""The withdraw hook names the retired path it warns against."""

import unittest
from pathlib import Path

HOOK = (
    Path(__file__).resolve().parents[2]
    / "frontend"
    / "src"
    / "hooks"
    / "useTestimonyMovements.ts"
)


class WithdrawCommentEnglishTest(unittest.TestCase):
    def test_comment_cites_the_retired_path(self):
        text = HOOK.read_text(encoding="utf-8")
        self.assertIn("`/testimony-movements/${idTestimony}/withdraw`", text)
        self.assertIn("cardNumber", text)
        self.assertIn("`/retirar`", text)
        self.assertIn("`numeroCarton`", text)
        self.assertNotIn("former Spanish withdraw path", text)


if __name__ == "__main__":
    unittest.main()
