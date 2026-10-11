#!/usr/bin/env python3
"""Local identifiers that are not message keys or stored labels are English."""

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


class LocalIdentifiersEnglishTest(unittest.TestCase):
    def test_frontend_locals_are_english(self):
        payments = read("frontend/src/app/dashboard/payments/page.tsx")
        self.assertIn("PAYMENT_FIELD_NAMES", payments)
        self.assertIn("function PaymentsPage", payments)
        self.assertIn("paymentReceipt", payments)
        self.assertIn("handleIssueReceipt", payments)
        self.assertNotIn("PAGO_FIELD_NAMES", payments)
        self.assertNotIn("PagosPage", payments)
        self.assertNotIn("reciboPago", payments)
        self.assertNotIn("handleEmitirRecibo", payments)
        self.assertNotIn("idPago", payments)
        self.assertIn('useTranslations("pagos")', payments)
        self.assertIn('t("estadoSinPagos")', payments)
        self.assertIn('t("emitirRecibo")', payments)
        self.assertIn('t("saldoExcedido")', payments)

        reports = read("frontend/src/hooks/useReports.ts")
        self.assertIn("idPayment: number", reports)
        self.assertIn("recibo_pago_${idPayment}.pdf", reports)
        self.assertIn("deuda_documentos_", reports)
        self.assertNotIn("idPago", reports)

        managements = read("frontend/src/app/dashboard/managements/page.tsx")
        self.assertIn('STATUS_ARCHIVED = "Archivada"', managements)
        self.assertNotIn("ESTADO_ARCHIVADA", managements)

        documents = read("frontend/src/app/dashboard/documents/page.tsx")
        self.assertIn("documentTypes", documents)
        self.assertIn("(documentType)", documents)
        self.assertNotIn("tiposDoc", documents)
        self.assertNotIn("(tipo)", documents)
        self.assertIn('t("selectTipo")', documents)
        self.assertIn('t("editDocumento")', documents)

        templates = read("frontend/src/app/dashboard/administration/templates/page.tsx")
        self.assertIn("documentTypes", templates)
        self.assertNotIn("tiposDocument", templates)

        auth = read("frontend/src/tests/unit/auth-store.test.ts")
        self.assertIn('returns true for type ADMIN"', auth)
        self.assertIn('returns true for type ADMINISTRADOR"', auth)
        self.assertIn('returns false for type EMPLEADO"', auth)
        self.assertNotIn("tipo ", auth)
        self.assertIn("ESCRIBANO", auth)
        self.assertIn("EMPLEADO", auth)

    def test_backend_locals_are_english(self):
        core = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/CoreBusinessControllersIntegrationTest.java"
        )
        self.assertIn(
            "Should save registration number, volume/folio/land record, and boundaries",
            core,
        )
        self.assertIn("shouldSaveRegistrationNumberVolumeFolioLandRecordAndBoundaries", core)
        self.assertNotIn("matricula", core)
        self.assertNotIn("linderos", core)
        self.assertNotIn("YBoundaries", core)

        payment = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/PaymentRepositoryIntegrationTest.java"
        )
        self.assertIn("Payment payment2", payment)
        self.assertNotIn("pago2", payment)

        history = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/RemainingControllersIntegrationTest.java"
        )
        self.assertIn("shouldReturnAllHistoryRecords", history)
        self.assertNotIn("Historiales", history)

        auxiliary = read(
            "backend-api/src/test/java/com/licensis/notaire/integration/AuxiliaryProtocolControllerTest.java"
        )
        self.assertIn("shouldListAvailableAuxiliaryFolios", auxiliary)
        self.assertIn("shouldReturnEmptyWhenNoAuxiliaryFoliosAvailable", auxiliary)
        self.assertIn("shouldCreateDeedOnAvailableAuxiliaryFolio", auxiliary)
        self.assertIn("shouldRejectDeedWhenNoAuxiliaryFolioAvailable", auxiliary)
        self.assertIn("Protocolo Auxiliar", auxiliary)
        self.assertNotIn("Auxiliares", auxiliary)
        self.assertNotIn("FolioAuxiliary", auxiliary)


if __name__ == "__main__":
    unittest.main()
