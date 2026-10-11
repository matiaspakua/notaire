#!/usr/bin/env python3
"""Unarchived change specs name the live contract in English."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


class ChangeContractEnglishTest(unittest.TestCase):
    def test_archive_debt_change_spec_uses_the_live_fields(self):
        folder = CHANGES / "gestion-archive-deuda-check"
        text = (folder / "spec.md").read_text(encoding="utf-8")
        self.assertIn("CU16 – Archivar Gestión.md", text)
        self.assertIn("GestionArchiveIntegrationTest", text)
        self.assertIn("`pendingBalance`", text)
        self.assertIn("`pendingDebtAtArchiving`", text)
        self.assertIn('"Archivada"', text)
        self.assertIn("HTTP 200", text)
        for retired in (
            "gestión",
            "deudaPendienteAlArchivar",
            "saldoPendiente",
            "presupuestos",
            "saldo pendiente",
        ):
            self.assertNotIn(retired, text, retired)
        proposal = (folder / "proposal.md").read_text(encoding="utf-8")
        self.assertIn("## Objetivo", proposal)
        self.assertIn("deudaPendienteAlArchivar", proposal)

    def test_incomplete_put_delta_names_the_live_message(self):
        text = (
            CHANGES
            / "fix-655-put-rejects-incomplete-bodies"
            / "specs"
            / "api-error-contract"
            / "spec.md"
        ).read_text(encoding="utf-8")
        self.assertIn("PUT /api/v1/testimonies/{id}", text)
        self.assertIn("PUT /api/v1/procedures/{id}", text)
        self.assertIn("`idProcedureType: is required`", text)
        self.assertIn("`version: is required`", text)
        self.assertIn("folio-types", text)
        self.assertIn("management-statuses", text)
        self.assertIn("document-types", text)
        self.assertIn("procedure-types", text)
        self.assertIn("concepts", text)
        self.assertIn("workflow-definition", text)
        self.assertIn("testimony-movements", text)
        for retired in (
            "es obligatorio",
            "tipo-folio",
            "estado-gestion",
            "tipo-de-documento",
            "tipo-tramite",
            "conceptos",
            "movimiento-testimonio",
        ):
            self.assertNotIn(retired, text, retired)

    def test_people_list_delta_names_the_people_screen(self):
        text = (
            CHANGES / "fix-596-people-pagination" / "specs" / "people-management" / "spec.md"
        ).read_text(encoding="utf-8")
        self.assertIn("GET /api/v1/people", text)
        self.assertIn("usePeople", text)
        self.assertIn("the people screen loads", text)
        self.assertNotIn("personas screen", text)


if __name__ == "__main__":
    unittest.main()
