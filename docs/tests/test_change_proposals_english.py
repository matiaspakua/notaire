#!/usr/bin/env python3
"""Open proposals name the live contract and keep required headings."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


class ChangeProposalsEnglishTest(unittest.TestCase):
    def test_case_summary_proposal_names_the_live_summary(self):
        text = (CHANGES / "feat-774-gestion-resumen-caso" / "proposal.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("## Objetivo", text)
        self.assertIn("## Reglas de negocio", text)
        self.assertIn("### Módulos afectados", text)
        self.assertIn("feat/774_gestion_workflow_links", text)
        self.assertIn("CU07 – Generar testimonio", text)
        self.assertIn("CU70 – Gestión de Copias", text)
        self.assertIn("`Resumen del caso`", text)
        self.assertIn("Escrituras, Testimonios, Copias and Pagos", text)
        self.assertIn("SIN_INGRESAR", text)
        self.assertIn("financial-summary", text)
        self.assertIn("ManagementFinancialSummaryService", text)
        self.assertIn("managements screen", text)
        for retired in (
            "gestión",
            "gestiones",
            "resumen-financiero",
            "ManagementResumenFinancieroService",
            "trámites",
        ):
            self.assertNotIn(retired, text, retired)

    def test_paged_endpoints_proposal_names_the_live_paths(self):
        text = (CHANGES / "fix-596-pageable-parameter-object" / "proposal.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("## Objetivo", text)
        self.assertIn("### Módulos afectados", text)
        self.assertIn("CU74 – Performance and Caching Strategy", text)
        self.assertIn(
            "GET /api/v1/{people,budgets,deeds,procedures,managements}",
            text,
        )
        for retired in ("presupuestos", "escrituras", "tramites", "gestiones"):
            self.assertNotIn(retired, text, retired)

    def test_incomplete_put_proposal_names_the_live_message(self):
        text = (
            CHANGES / "fix-655-put-rejects-incomplete-bodies" / "proposal.md"
        ).read_text(encoding="utf-8")
        self.assertIn("## Objetivo", text)
        self.assertIn("## Reglas de negocio", text)
        self.assertIn("CU07 – Testimonios", text)
        self.assertIn("Administración → Trámites", text)
        self.assertIn("`idProcedureType: is required`", text)
        self.assertIn("`number: is required; flagged: is required`", text)
        self.assertNotIn("es obligatorio", text)


if __name__ == "__main__":
    unittest.main()
