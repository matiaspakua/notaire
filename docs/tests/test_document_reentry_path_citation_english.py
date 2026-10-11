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
CU44 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU44 – Reingresar testimonio.md"
)
REENTER = "`POST /api/v1/movimiento-testimonio/{id}/reenter`"
CU10 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU10 – Registrar movimientos documentación de entidades externas.md"
)
EXTERNAL = (
    "`GET/PUT /api/v1/gestiones/{id}/documentos-entidades-externas"
    "[/{idDocumentoPresentado}]`"
)
CU53 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU53 - Modificar Gestión.md"
)
CU83 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU83 – Definir Workflow de Estados y Transiciones.md"
)
TRANSITION = "`POST /api/v1/gestiones/{id}/transition`"
CU22 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU22 – Registrar Suplencia.md"
)
COMPLETE_CASE = "`PUT /api/v1/gestiones/complete-case`"
WORKFLOW_TRACE = "`GET /api/v1/gestiones/{id}/workflow-trace`"
CU13 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU13 – Ver historial de gestión.md"
)
HISTORY = "`GET /api/v1/gestiones/{id}/historial`"
CURRENT_STATUS = "`GET /api/v1/gestiones/{id}/estado-actual`"
PUT_MANAGEMENT = "`PUT /api/v1/gestiones/{id}`"
PUT_COMPLETE = "`PUT /api/v1/gestiones/{id}/complete-case`"


class DocumentReentryPathCitationEnglishTest(unittest.TestCase):
    def test_design_cites_the_path_cu43_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU43.read_text(encoding="utf-8")
        self.assertIn("## Riesgos / Trade-offs", text)
        self.assertIn(PATH, text)
        self.assertIn("`DOC-GESTION`", text)
        self.assertIn(PATH.strip("`"), use_case)
        self.assertNotIn("A use-case file still names a Spanish path", text)

    def test_design_cites_the_path_cu44_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU44.read_text(encoding="utf-8")
        self.assertIn(REENTER, text)
        self.assertIn(REENTER.strip("`"), use_case)
        self.assertIn("`DOC-GESTION`", text)

    def test_design_cites_the_path_cu10_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU10.read_text(encoding="utf-8")
        self.assertIn(EXTERNAL, text)
        self.assertIn(EXTERNAL.strip("`"), use_case)
        self.assertIn("`DOC-GESTION`", text)

    def test_design_cites_the_path_cu53_and_cu83_still_name(self):
        text = DESIGN.read_text(encoding="utf-8")
        self.assertIn(TRANSITION, text)
        self.assertIn(TRANSITION.strip("`"), CU53.read_text(encoding="utf-8"))
        self.assertIn(TRANSITION.strip("`"), CU83.read_text(encoding="utf-8"))
        self.assertIn("`DOC-GESTION`", text)
        live = (
            ROOT / "docs" / "200-architecture" / "203-design" / "GESTION.md"
        ).read_text(encoding="utf-8")
        self.assertIn("`POST /api/v1/managements/{id}/transition`", live)

    def test_design_cites_the_path_cu22_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU22.read_text(encoding="utf-8")
        self.assertIn(COMPLETE_CASE, text)
        self.assertIn(COMPLETE_CASE.strip("`"), use_case)
        self.assertIn("`DOC-GESTION`", text)
        live = (
            ROOT / "docs" / "200-architecture" / "203-design" / "GESTION.md"
        ).read_text(encoding="utf-8")
        self.assertIn("`POST /api/v1/managements/complete-case`", live)

    def test_design_cites_the_path_cu83_still_names_for_workflow_trace(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU83.read_text(encoding="utf-8")
        self.assertIn(WORKFLOW_TRACE, text)
        self.assertIn(WORKFLOW_TRACE.strip("`"), use_case)
        self.assertIn("`DOC-GESTION`", text)
        live = (
            ROOT / "docs" / "200-architecture" / "203-design" / "GESTION.md"
        ).read_text(encoding="utf-8")
        self.assertIn("`GET /api/v1/managements/{id}/workflow-trace`", live)

    def test_design_cites_the_paths_cu13_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU13.read_text(encoding="utf-8")
        self.assertIn(HISTORY, text)
        self.assertIn(CURRENT_STATUS, text)
        self.assertIn(HISTORY.strip("`"), use_case)
        self.assertIn(CURRENT_STATUS.strip("`"), use_case)
        self.assertIn("`DOC-GESTION`", text)
        live = (
            ROOT / "docs" / "200-architecture" / "203-design" / "GESTION.md"
        ).read_text(encoding="utf-8")
        self.assertIn("`GET /api/v1/managements/{id}/history`", live)
        self.assertIn("`GET /api/v1/managements/{id}/current-status`", live)

    def test_design_cites_the_paths_cu53_still_names_for_status_puts(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU53.read_text(encoding="utf-8")
        self.assertIn(PUT_MANAGEMENT, text)
        self.assertIn(PUT_COMPLETE, text)
        self.assertIn(PUT_MANAGEMENT, use_case)
        self.assertIn(PUT_COMPLETE, use_case)
        self.assertIn("`DOC-GESTION`", text)
        live = (
            ROOT / "docs" / "200-architecture" / "203-design" / "GESTION.md"
        ).read_text(encoding="utf-8")
        self.assertIn("`PUT /api/v1/managements/{id}`", live)
        self.assertIn("`PUT /api/v1/managements/{id}/complete-case`", live)


if __name__ == "__main__":
    unittest.main()
