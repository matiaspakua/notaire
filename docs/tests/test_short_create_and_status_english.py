#!/usr/bin/env python3
"""The case schema names the shorter create and current-status calls."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GESTION = ROOT / "docs" / "200-architecture" / "203-design" / "GESTION.md"
DESIGN = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "docs-921-engineering-sequences"
    / "design.md"
)
CU13 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU13 – Ver historial de gestión.md"
)
ARCHIVED_CREATE = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-feat-804-enforce-workflow-transitions"
    / "proposal.md"
)
ARCHIVED_STATUS = (
    ROOT
    / "docs"
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-03-fix-806-gestion-historial-audit"
    / "proposal.md"
)
RETIRED_CREATE = "`POST /gestiones`"
RETIRED_STATUS = "`GET /gestiones/{id}/estado-actual`"
LIVE_CREATE = "`POST /api/v1/managements`"
LIVE_STATUS = "`GET /api/v1/managements/{id}/current-status`"


class ShortCreateAndStatusEnglishTest(unittest.TestCase):
    def test_case_schema_names_the_live_calls(self):
        text = GESTION.read_text(encoding="utf-8")
        self.assertIn(LIVE_CREATE, text)
        self.assertIn(LIVE_STATUS, text)
        self.assertNotIn("POST /gestiones", text)
        self.assertNotIn("GET /gestiones/{id}/estado-actual", text)

    def test_design_cites_the_paths_cu13_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU13.read_text(encoding="utf-8")
        archived_create = ARCHIVED_CREATE.read_text(encoding="utf-8")
        archived_status = ARCHIVED_STATUS.read_text(encoding="utf-8")
        self.assertIn(RETIRED_CREATE, text)
        self.assertIn(RETIRED_STATUS, text)
        self.assertIn("`DOC-GESTION`", text)
        self.assertIn(RETIRED_CREATE, use_case)
        self.assertIn(RETIRED_STATUS, use_case)
        self.assertIn(RETIRED_CREATE, archived_create)
        self.assertNotIn(LIVE_CREATE, archived_create)
        self.assertIn(RETIRED_STATUS, archived_status)
        self.assertNotIn(LIVE_STATUS, archived_status)


if __name__ == "__main__":
    unittest.main()
