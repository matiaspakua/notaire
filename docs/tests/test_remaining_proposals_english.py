#!/usr/bin/env python3
"""Remaining open proposals name the live surface and keep official titles."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


class RemainingProposalsEnglishTest(unittest.TestCase):
    def test_archive_debt_proposal_names_the_live_fields(self):
        text = (CHANGES / "gestion-archive-deuda-check" / "proposal.md").read_text(
            encoding="utf-8"
        )
        self.assertIn("## Objetivo", text)
        self.assertIn("**Regla de Negocio:**", text)
        self.assertIn("CU16 – Archivar Gestión", text)
        self.assertIn("feat/169_gestion-archive-deuda-check", text)
        self.assertIn("`gestion-archive-deuda-check`", text)
        self.assertIn("GET /outstanding-balance", text)
        self.assertIn("ManagementArchiveDebtService", text)
        self.assertIn("`pendingDebtAtArchiving`", text)
        self.assertIn("managements/page.tsx", text)
        for retired in (
            "deudaPendienteAlArchivar",
            "GestionArchiveDebtService",
            "gestiones/page.tsx",
            "saldo pendiente",
            "presupuestos",
        ):
            self.assertNotIn(retired, text, retired)

    def test_submitted_document_proposal_names_the_documents_screen(self):
        text = (
            CHANGES / "fix-655-submitted-document-required-fields" / "proposal.md"
        ).read_text(encoding="utf-8")
        self.assertIn("## Objetivo", text)
        self.assertIn("## Reglas de negocio", text)
        self.assertIn("### Módulos afectados", text)
        self.assertIn("CU04 – Registrar documentación cliente", text)
        self.assertIn("CU72 – Gestionar documentos presentados", text)
        self.assertIn("`fk_id_tramite`", text)
        self.assertIn("Documents screen", text)
        self.assertIn("Documents create/edit dialog", text)
        for retired in ("gestión", "trámite", "Documentos page", "Documentos create"):
            self.assertNotIn(retired, text, retired)

    def test_property_proposal_names_the_properties_screen(self):
        text = (
            CHANGES / "fix-655-property-cadastral-designation-required" / "proposal.md"
        ).read_text(encoding="utf-8")
        self.assertIn("## Objetivo", text)
        self.assertIn("## Reglas de negocio", text)
        self.assertIn("### Módulos afectados", text)
        self.assertIn("CU69 – Gestión de Inmuebles", text)
        self.assertIn("CU82 – Datos registrales del inmueble", text)
        self.assertIn("nomenclatura catastral", text)
        self.assertIn("Properties page", text)
        self.assertIn("Properties dialog", text)
        for retired in ("(inmuebles)", "Inmuebles page", "Inmuebles form", "Inmuebles dialog"):
            self.assertNotIn(retired, text, retired)


if __name__ == "__main__":
    unittest.main()
