#!/usr/bin/env python3
"""Load-test check names, dialog locals, and item-form prose are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class LoadCheckAndDialogNamesEnglishTest(unittest.TestCase):
    def test_load_check_names_are_english(self):
        script = read("infra/performance/k6/load-test.js")
        self.assertIn("name: 'managements'", script)
        self.assertIn("name: 'budgets'", script)
        self.assertIn("name: 'procedures'", script)
        self.assertIn("/api/v1/managements", script)
        self.assertIn("/api/v1/budgets", script)
        self.assertIn("/api/v1/procedures", script)
        self.assertIn("name: ADMIN_USER, password: ADMIN_PASSWORD", script)
        self.assertNotIn("'gestiones'", script)
        self.assertNotIn("'presupuestos'", script)
        self.assertNotIn("'tramites'", script)
        self.assertNotIn("nombre:", script)
        self.assertNotIn("contrasenia:", script)

    def test_dialog_local_and_item_prose_are_english(self):
        reentry = read("testing/e2e/tests/TS-0018-document-reentry-workflow.spec.ts")
        self.assertIn("detailDialog", reentry)
        self.assertIn("Certificado de Dominio", reentry)
        self.assertIn("no tiene trámites", reentry)
        self.assertIn("documento reingresado", reentry)
        self.assertNotIn("detalleDialog", reentry)

        items = read("testing/e2e/tests/TS-0091-items-discounts-surcharges.spec.ts")
        self.assertIn("select stored types DESCUENTO and RECARGO", items)
        self.assertIn("The reason field becomes required", items)
        self.assertIn("discounts and surcharges report", items)
        self.assertIn("without a reason", items)
        self.assertIn('"Descuento"', items)
        self.assertIn('"Recargo"', items)
        self.assertIn("motivo es obligatorio", items)
        self.assertIn("descuentos-recargos-budget", items)
        self.assertNotIn("select tipo", items)
        self.assertNotIn("Motivo", items)
        self.assertNotIn("descuentos/recargos", items)
        self.assertNotIn("tipo Descuento", items)
        self.assertNotIn("without motivo", items)


if __name__ == "__main__":
    unittest.main()
