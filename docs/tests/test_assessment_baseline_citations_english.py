#!/usr/bin/env python3
"""The October 2026 assessment keeps the paths and titles measured on its baseline."""

import unittest
from pathlib import Path

ASSESSMENT = (
    Path(__file__).resolve().parents[1]
    / "300-development"
    / "ASSESSMENT-2026-10.md"
)


class AssessmentBaselineCitationsEnglishTest(unittest.TestCase):
    def test_measured_paths_and_official_titles_stay(self):
        text = ASSESSMENT.read_text(encoding="utf-8")
        self.assertIn("Baseline | `main` @ `d4473af2`", text)
        self.assertIn("# System assessment — October 2026", text)
        for measured in (
            "`pagos/fecha`",
            "`presupuestos/persona`",
            "`historial/gestion`",
            "`reportes/documentos-por-vencer`",
            "`usuarios/persona`",
            "`tipo-identificacion`",
            "`/gestiones`",
            "`/pagos`",
            "`/folio`",
            "`/inmueble`",
            "`/copia`",
            "`/buscar`",
            "`/usuarios`",
            "(Documentos Presentados)",
            "(Plantillas de Trámite)",
            "EMPLEADO",
            "ESCRIBANO",
        ):
            self.assertIn(measured, text, measured)
        for collapsed in (
            "`pagos/date`",
            "`presupuestos/person`",
            "`historial/management`",
            "`reportes/documents-due`",
            "`usuarios/person`",
            "`/managements`",
            "`/payments`",
            "`/folios`",
            "`/properties`",
            "`/copies`",
            "`/search`",
            "`/users`",
            "(submitted documents)",
            "(procedure templates)",
        ):
            self.assertNotIn(collapsed, text, collapsed)


if __name__ == "__main__":
    unittest.main()
