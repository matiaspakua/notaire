#!/usr/bin/env python3
"""The engineering-sequences design names the retired document services."""

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
CU10 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU10 – Registrar movimientos documentación de entidades externas.md"
)
ARCHIVED_REENTRY = (
    ROOT
    / "docs"
    / "000-archive"
    / "speckit"
    / "specs"
    / "archive"
    / "003-cu43-reingresar-documentacion"
    / "plan.md"
)
ARCHIVED_EXTERNAL = (
    ROOT
    / "docs"
    / "000-archive"
    / "speckit"
    / "specs"
    / "archive"
    / "002-cu10-movimientos-documentacion-entidades-externas"
    / "plan.md"
)
RETIRED_REENTRY = "`ReingresoDocumentacionService`"
RETIRED_EXTERNAL = "`DocumentoEntidadExternaService`"
LIVE_REENTRY = "`DocumentReentryService`"
LIVE_EXTERNAL = "`ExternalEntityDocumentService`"


class RetiredDocumentServicesEnglishTest(unittest.TestCase):
    def test_design_cites_the_classes_cu43_and_cu10_still_name(self):
        text = DESIGN.read_text(encoding="utf-8")
        cu43 = CU43.read_text(encoding="utf-8")
        cu10 = CU10.read_text(encoding="utf-8")
        archived_reentry = ARCHIVED_REENTRY.read_text(encoding="utf-8")
        archived_external = ARCHIVED_EXTERNAL.read_text(encoding="utf-8")
        self.assertIn(RETIRED_REENTRY, text)
        self.assertIn(RETIRED_EXTERNAL, text)
        self.assertIn(LIVE_REENTRY, text)
        self.assertIn(LIVE_EXTERNAL, text)
        self.assertIn(RETIRED_REENTRY, cu43)
        self.assertNotIn(LIVE_REENTRY.strip("`"), cu43)
        self.assertIn(RETIRED_EXTERNAL, cu10)
        self.assertNotIn(LIVE_EXTERNAL.strip("`"), cu10)
        self.assertIn(RETIRED_REENTRY.strip("`"), archived_reentry)
        self.assertNotIn(LIVE_REENTRY.strip("`"), archived_reentry)
        self.assertIn(RETIRED_EXTERNAL.strip("`"), archived_external)
        self.assertNotIn(LIVE_EXTERNAL.strip("`"), archived_external)


if __name__ == "__main__":
    unittest.main()
