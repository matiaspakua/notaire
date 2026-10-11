#!/usr/bin/env python3
"""Found locals, sworn-statement tests, and report dialog keys are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class FoundAndReportLocalsEnglishTest(unittest.TestCase):
    def test_found_locals_and_used_status_are_english(self):
        procedure_type = read(
            "backend-api/src/main/java/com/licensis/notaire/jpa/ProcedureTypeJpaController.java"
        )
        self.assertIn("foundProcedureType", procedure_type)
        self.assertNotIn("procedureTypeEncontrado", procedure_type)

        person_procedure = read(
            "backend-api/src/main/java/com/licensis/notaire/jpa/PersonProcedureJpaController.java"
        )
        self.assertIn("PersonProcedure found", person_procedure)
        self.assertNotIn("encontrado", person_procedure)

        folio = read(
            "backend-api/src/main/java/com/licensis/notaire/adapter/in/web/folio/FolioController.java"
        )
        self.assertIn("STATUS_USED", folio)
        self.assertIn('"Utilizado"', folio)
        self.assertNotIn("StatusUTILIZADO", folio)

    def test_sworn_statement_methods_and_dialog_keys_are_english(self):
        reports = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/ReportsUseCaseIntegrationTest.java"
        )
        validation = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/"
            "ReportControllerValidationIntegrationTest.java"
        )
        combined = reports + validation
        self.assertIn("shouldGenerateMonthlySwornStatementPdf", reports)
        self.assertIn("shouldGenerateTaxSwornStatementPdf", reports)
        self.assertIn("shouldRejectMonthlySwornStatementWithMonthOutOfRange", validation)
        self.assertIn("shouldRejectMonthlySwornStatementWithNonPositiveYear", validation)
        self.assertNotIn("DeclaracionJurada", combined)
        self.assertNotIn("Declaraciones", combined)
        self.assertNotIn("DDJJ", combined)
        self.assertNotIn("WithMes", combined)
        self.assertIn("shouldRejectInvalidMonthForSwornStatements", reports)

        protocol = read("frontend/src/app/dashboard/protocol/page.tsx")
        self.assertIn('"index-book"', protocol)
        self.assertIn('"monthly-sworn-statement"', protocol)
        self.assertIn('"tax-sworn-statement"', protocol)
        self.assertNotIn('"libro-indice"', protocol)
        self.assertNotIn("declaracion-jurada", protocol)
        self.assertIn("libro_indice_", protocol)
        self.assertIn("declaracion_jurada_mensual_", protocol)
        self.assertIn("declaracion_jurada_rentas_", protocol)
        self.assertIn('t("reports.libroIndice.title")', protocol)
        self.assertIn('t("reports.ddjjMensual.title")', protocol)
        self.assertIn('t("reports.ddjjRentas.title")', protocol)

        roles = read("frontend/src/app/dashboard/administration/roles/page.tsx")
        self.assertIn("MODULE_VALUES", roles)
        self.assertIn("toggleModule(module: string)", roles)
        self.assertNotIn("MODULO_VALUES", roles)
        self.assertNotIn("toggleModulo", roles)
        self.assertIn("modulos", roles)
        self.assertIn('"administracion"', roles)
        self.assertIn('t("fields.modulos")', roles)


if __name__ == "__main__":
    unittest.main()
