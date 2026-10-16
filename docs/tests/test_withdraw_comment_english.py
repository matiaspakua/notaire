#!/usr/bin/env python3
"""Withdraw keeps the English path; the hook still names the retired Spanish path."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
HOOK = ROOT / "frontend" / "src" / "hooks" / "useTestimonyMovements.ts"
GATEWAY = (
    ROOT
    / "frontend"
    / "src"
    / "adapters"
    / "http"
    / "testimony-movement"
    / "testimony-movement-http-gateway.ts"
)


class WithdrawCommentEnglishTest(unittest.TestCase):
    def test_gateway_uses_english_withdraw_path(self):
        text = GATEWAY.read_text(encoding="utf-8")
        self.assertIn("`/testimony-movements/${idTestimony}/withdraw`", text)
        self.assertIn("cardNumber", text)

    def test_hook_comment_cites_the_retired_path(self):
        text = HOOK.read_text(encoding="utf-8")
        self.assertIn("cardNumber", text)
        self.assertIn("`/retirar`", text)
        self.assertIn("`numeroCarton`", text)
        self.assertNotIn("former Spanish withdraw path", text)


if __name__ == "__main__":
    unittest.main()
