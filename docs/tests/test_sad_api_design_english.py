#!/usr/bin/env python3
"""The SAD names the live English collection paths in the API design section."""

import unittest
from pathlib import Path

SAD = (
    Path(__file__).resolve().parents[1]
    / "200-architecture"
    / "201-SAD"
    / "sad.md"
)


class SadApiDesignEnglishTest(unittest.TestCase):
    def test_api_design_names_english_collections(self):
        text = SAD.read_text(encoding="utf-8")
        self.assertIn("### 8.7 API Design", text)
        self.assertIn(
            "RESTful resource naming (English collections: "
            "`/managements`, `/people`, `/deeds`).",
            text,
        )
        self.assertNotIn("Spanish domain:", text)
        self.assertNotIn("`/management`, `/person`, `/deed`", text)


if __name__ == "__main__":
    unittest.main()
