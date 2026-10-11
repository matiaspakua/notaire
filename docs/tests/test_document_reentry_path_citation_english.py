#!/usr/bin/env python3
"""The engineering-sequences design names the path CU43 still writes."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DESIGN = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "docs-921-engineering-sequences"
    / "design.md"
)
CU43 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU43 – Reingresar documentación.md"
)
PATH = "`GET/POST /api/v1/gestiones/{id}/reingreso-documentacion`"


class DocumentReentryPathCitationEnglishTest(unittest.TestCase):
    def test_design_cites_the_path_cu43_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU43.read_text(encoding="utf-8")
        self.assertIn("## Riesgos / Trade-offs", text)
        self.assertIn(PATH, text)
        self.assertIn("`DOC-GESTION`", text)
        self.assertIn(PATH.strip("`"), use_case)
        self.assertNotIn("A use-case file still names a Spanish path", text)


if __name__ == "__main__":
    unittest.main()
