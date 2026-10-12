#!/usr/bin/env python3
"""The authentication guide names the live stored role labels."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GUIDE = ROOT / "docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md"
LIVE = "stored names such as ADMIN, ESCRIBANO, and SECRETARIA"


class RoleDiagramLabelsEnglishTest(unittest.TestCase):
    def test_role_diagram_names_the_live_labels(self):
        text = GUIDE.read_text(encoding="utf-8")
        self.assertIn(LIVE, text)
        self.assertNotIn("Secretario", text)
        self.assertIn('"Escribano"', text)


if __name__ == "__main__":
    unittest.main()
