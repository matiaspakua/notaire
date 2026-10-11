#!/usr/bin/env python3
"""Application comments are English. Message keys and stored statuses stay."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


class SourceCommentsEnglishTest(unittest.TestCase):
    def test_redirect_and_status_comments_are_english(self):
        config = (ROOT / "frontend" / "next.config.ts").read_text(encoding="utf-8")
        self.assertIn("duplicate administration pages", config)
        self.assertIn("/dashboard/administration/items", config)
        self.assertIn("/dashboard/administration/audit", config)
        self.assertNotIn("administración", config)

        maps = (ROOT / "frontend" / "src" / "lib" / "status-maps.ts").read_text(encoding="utf-8")
        self.assertIn("`administracion.estadosGestion`", maps)
        self.assertIn('"en tramite"', maps)
        self.assertIn('"gestion con escritura firmada"', maps)
        self.assertIn('"gestion con escritura sin firmar"', maps)
        self.assertNotIn("estados-gestion", maps)


if __name__ == "__main__":
    unittest.main()
