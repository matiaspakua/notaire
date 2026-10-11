#!/usr/bin/env python3
"""The engineering-sequences design names the paths CU47 still writes."""

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
CU47 = (
    ROOT
    / "docs"
    / "100-business"
    / "102-use-cases"
    / "CU47 – Consultar Pago.md"
)
SUMMARY_SPEC = (
    ROOT
    / "docs"
    / "openspec"
    / "specs"
    / "pago-presupuesto-gestion-summary"
    / "spec.md"
)
STATUS_SPEC = (
    ROOT / "docs" / "openspec" / "specs" / "pagos-parciales-cuotas" / "spec.md"
)
SUMMARY = "`GET /api/v1/presupuestos/{id}/resumen`"
STATUS = "`GET /api/v1/pagos/presupuesto/{id}/estado`"


class PaymentConsultPathsEnglishTest(unittest.TestCase):
    def test_design_cites_the_paths_cu47_still_names(self):
        text = DESIGN.read_text(encoding="utf-8")
        use_case = CU47.read_text(encoding="utf-8")
        self.assertIn(SUMMARY, text)
        self.assertIn(STATUS, text)
        self.assertIn(SUMMARY.strip("`"), use_case)
        self.assertIn(STATUS.strip("`"), use_case)
        self.assertIn("`pago-presupuesto-gestion-summary`", text)
        self.assertIn("`pagos-parciales-cuotas`", text)
        summary = SUMMARY_SPEC.read_text(encoding="utf-8")
        status = STATUS_SPEC.read_text(encoding="utf-8")
        self.assertIn("`GET /api/v1/budgets/{id}/summary`", summary)
        self.assertIn("`GET /api/v1/payments/budget/{id}/status`", status)


if __name__ == "__main__":
    unittest.main()
