#!/usr/bin/env python3
"""People-workflow comments name the is-client checkbox in English."""

import unittest
from pathlib import Path

SPEC = (
    Path(__file__).resolve().parents[2]
    / "testing"
    / "e2e"
    / "tests"
    / "TS-0015-people-clients-workflow.spec.ts"
)


class PeopleClientCommentsEnglishTest(unittest.TestCase):
    def test_comments_name_the_is_client_checkbox(self):
        text = SPEC.read_text(encoding="utf-8")
        self.assertIn('catalog label "Es cliente"', text)
        self.assertIn("is-client checkbox", text)
        self.assertIn("check-is-client", text)
        self.assertNotIn('"Es client"', text)
        self.assertNotIn("'es client'", text)
        self.assertNotIn("es-client", text)
        self.assertIn("/creada/i", text)
        self.assertIn("/^cliente$|^sí$/i", text)


if __name__ == "__main__":
    unittest.main()
