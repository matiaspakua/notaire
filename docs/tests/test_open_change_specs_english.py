#!/usr/bin/env python3
"""Open change specs name managements and procedures in English."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


class OpenChangeSpecsEnglishTest(unittest.TestCase):
    def test_case_summary_spec_is_english_and_keeps_stored_states(self):
        folder = CHANGES / "feat-774-gestion-resumen-caso"
        text = (folder / "specs" / "management-case-summary" / "spec.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("owner CU07", text)
        self.assertIn(
            "#### Scenario: A management with a deed, a testimony, a movement and a copy",
            text,
        )
        self.assertIn("### Requirement: The managements screen shows the case summary", text)
        for state in ("INSCRIPTO", "SIN_INGRESAR", "RETIRADO"):
            self.assertIn(state, text, state)
        for retired in ("gestión", "trámite", "escrituras", "testimonios", "gestiones"):
            self.assertNotIn(retired, text, retired)
        design = (folder / "design.md").read_text(encoding="utf-8")
        ledger = (folder / "traceability.md").read_text(encoding="utf-8")
        title = "A management with a deed, a testimony, a movement and a copy"
        self.assertIn(title, design)
        self.assertIn(title, ledger)
        self.assertIn("CU07 – Generar testimonio", design)
        self.assertNotIn("A gestión with a deed", design)
        self.assertNotIn("A gestión with a deed", ledger)

    def test_submitted_document_delta_is_english(self):
        text = (
            CHANGES
            / "fix-655-submitted-document-required-fields"
            / "specs"
            / "submitted-documents"
            / "spec.md"
        ).read_text(encoding="utf-8")
        self.assertIn("# submitted-documents — delta", text)
        self.assertIn("POST /api/v1/submitted-documents", text)
        self.assertIn("`typeId`", text)
        self.assertIn("`procedureId`", text)
        self.assertIn("### Requirement: UI registers documents for a procedure", text)
        self.assertIn("The Documents dialog", text)
        for retired in ("gestión", "trámite", "Documentos dialog"):
            self.assertNotIn(retired, text, retired)


if __name__ == "__main__":
    unittest.main()
