#!/usr/bin/env python3
"""Open change notes cite the live Playwright filenames."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CHANGES = ROOT / "docs" / "openspec" / "changes"


class OpenPlaywrightFilenamesEnglishTest(unittest.TestCase):
    def test_open_notes_cite_the_live_playwright_files(self):
        summary = (
            CHANGES / "feat-774-gestion-resumen-caso" / "design.md"
        ).read_text(encoding="utf-8")
        summary_ledger = (
            CHANGES / "feat-774-gestion-resumen-caso" / "traceability.md"
        ).read_text(encoding="utf-8")
        documents = (
            CHANGES / "fix-655-submitted-document-required-fields" / "traceability.md"
        ).read_text(encoding="utf-8")
        properties = (
            CHANGES
            / "fix-655-property-cadastral-designation-required"
            / "traceability.md"
        ).read_text(encoding="utf-8")
        live = {
            summary: "TS-0098-management-case-summary-feature.spec.ts",
            summary_ledger: "TS-0098-management-case-summary-feature.spec.ts",
            documents: "TS-0099-document-procedure-link.spec.ts",
            properties: "TS-0019-properties-valuation-workflow.spec.ts",
        }
        retired = {
            summary: "TS-0098-gestion-resumen-caso-feature.spec.ts",
            summary_ledger: "TS-0098-gestion-resumen-caso-feature.spec.ts",
            documents: "TS-0099-documento-tramite-link.spec.ts",
            properties: "TS-0019-inmuebles-valuacion-workflow.spec.ts",
        }
        for text, name in live.items():
            self.assertIn(name, text, name)
        for text, name in retired.items():
            self.assertNotIn(name, text, name)
        self.assertIn("feat-774-gestion-resumen-caso", summary_ledger)
        self.assertIn("## Riesgos / Trade-offs", summary)
        archive = (
            CHANGES
            / "archive"
            / "2026-10-11-feat-773-documentos-tramite"
            / "design.md"
        ).read_text(encoding="utf-8")
        self.assertIn("TS-0099-documento-tramite-link.spec.ts", archive)
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
        self.assertIn("TS-0098-gestion-resumen-caso-feature.spec.ts", changelog)


if __name__ == "__main__":
    unittest.main()
