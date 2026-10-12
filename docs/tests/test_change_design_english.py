#!/usr/bin/env python3
"""Unarchived change designs name the live surface in English."""

import unittest
from pathlib import Path

CHANGES = Path(__file__).resolve().parents[1] / "openspec" / "changes"


class ChangeDesignEnglishTest(unittest.TestCase):
    def test_case_summary_design_names_the_managements_screen(self):
        text = (CHANGES / "feat-774-gestion-resumen-caso" / "design.md").read_text(
            encoding="utf-8"
        )
        for title in (
            "CU07 – Generar testimonio",
            "CU11 – Ingresar para inscripción",
            "CU12 – Retirar testimonio",
            "CU15 – Procesar pago",
            "CU70 – Gestión de Copias",
        ):
            self.assertIn(title, text, title)
        self.assertIn("## Riesgos / Trade-offs", text)
        self.assertIn("TS-0098-management-case-summary-feature.spec.ts", text)
        self.assertNotIn("TS-0098-gestion-resumen-caso-feature.spec.ts", text)
        self.assertIn("useManagementSummary", text)
        self.assertIn("financial-summary", text)
        self.assertIn("managements screen", text)
        for retired in ("gestión", "gestiones", "resumen-financiero", "useResumenCaso", "Pagos "):
            self.assertNotIn(retired, text, retired)

    def test_required_documents_design_names_the_screen(self):
        folder = CHANGES / "feat-1362-procedure-template-admin"
        spec = (folder / "specs" / "ui-procedure-template-admin" / "spec.md").read_text(
            encoding="utf-8"
        )
        design = (folder / "design.md").read_text(encoding="utf-8")
        self.assertIn("the required-documents screen", spec)
        self.assertIn("the required-documents screen", design)
        self.assertIn("procedure-types page", design)
        self.assertIn("## Riesgos / Trade-offs", design)
        self.assertNotIn("Documentos necesarios", spec)
        self.assertNotIn("Documentos necesarios", design)
        self.assertNotIn("Trámites page", design)
        proposal = (folder / "proposal.md").read_text(encoding="utf-8")
        self.assertIn("## Objetivo", proposal)
        self.assertIn("CU55 – Crear plantilla de trámite", proposal)
        self.assertIn("Documentos necesarios", proposal)

    def test_property_and_document_designs_name_the_live_surface(self):
        properties = (
            CHANGES / "fix-655-property-cadastral-designation-required" / "design.md"
        ).read_text(encoding="utf-8")
        self.assertIn("properties require `cadastralDesignation`", properties)
        self.assertIn("properties.nomenclature", properties)
        self.assertIn("## Riesgos / Trade-offs", properties)
        self.assertNotIn("inmuebles", properties)

        documents = (
            CHANGES / "fix-655-submitted-document-required-fields" / "design.md"
        ).read_text(encoding="utf-8")
        self.assertIn("`fk_id_tramite`", documents)
        self.assertIn("Documents screen", documents)
        self.assertIn("DocumentReentryService", documents)
        self.assertIn("management number", documents)
        for retired in (
            "gestión",
            "trámite",
            "Documentos screen",
            "ReingresoDocumentacionService",
        ):
            self.assertNotIn(retired, documents, retired)


if __name__ == "__main__":
    unittest.main()
