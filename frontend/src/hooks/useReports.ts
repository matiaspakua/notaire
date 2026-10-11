/**
 * useReports — PDF report download hooks
 * Wraps /api/v1/reports/* endpoints that return application/pdf
 */
import { apiGetBytes } from "@/lib/api-client";

async function downloadPdf(path: string, filename: string): Promise<void> {
  const blob = await apiGetBytes(path);
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

/** CU01/CU39 — Reporte PDF de presupuesto */
export function useBudgetReport() {
  return {
    download: (idBudget: number) =>
      downloadPdf(
        `/reports/budget/${idBudget}`,
        `presupuesto_${idBudget}.pdf`
      ),
  };
}

/** CU01 — Reporte PDF de presupuesto con inmuebles */
export function useBudgetPropertiesReport() {
  return {
    download: (idBudget: number) =>
      downloadPdf(
        `/reports/budget-properties/${idBudget}`,
        `presupuesto_inmuebles_${idBudget}.pdf`
      ),
  };
}

/** CU02 — Lista de documentos para tipo de trámite */
export function useDocumentListReport() {
  return {
    download: (procedureTypeName: string) =>
      downloadPdf(
        `/reports/procedure-document-list?procedureTypeName=${encodeURIComponent(procedureTypeName)}`,
        `lista_documentos_${procedureTypeName}.pdf`
      ),
  };
}

/** CU13 — ManagementHistory de gestión */
export function useManagementHistoryReport() {
  return {
    download: (idManagement: number) =>
      downloadPdf(
        `/reports/management-history/${idManagement}`,
        `historial_gestion_${idManagement}.pdf`
      ),
  };
}

/** CU25 — Declaración Jurada Mensual */
export function useMonthlySwornStatementReport() {
  return {
    download: (year: number, month: number) =>
      downloadPdf(
        `/reports/monthly-sworn-statement?year=${year}&month=${month}`,
        `ddjj_mensual_${year}_${month}.pdf`
      ),
  };
}

/** CU50 — Declaración Jurada de Rentas */
export function useTaxSwornStatementReport() {
  return {
    download: (year: number, month: number) =>
      downloadPdf(
        `/reports/tax-sworn-statement?year=${year}&month=${month}`,
        `ddjj_rentas_${year}_${month}.pdf`
      ),
  };
}

/** CU24 — Libro Índice */
export function useIndexBookReport() {
  return {
    download: (year: number) =>
      downloadPdf(
        `/reports/index-book?year=${year}`,
        `libro_indice_${year}.pdf`
      ),
  };
}

/** CU09 — Consultar deuda de documentos (query param numberManagement, see openapi.yaml) */
export function useDocumentDebtReport() {
  return {
    download: (numeroGestion: number) =>
      downloadPdf(
        `/reports/document-debt?numberManagement=${numeroGestion}`,
        `deuda_documentos_${numeroGestion}.pdf`
      ),
  };
}

/** CU15 — Emitir recibo de pago */
export function usePaymentReceipt() {
  return {
    download: (idPago: number) =>
      downloadPdf(
        `/reports/payment-receipt/${idPago}`,
        `recibo_pago_${idPago}.pdf`
      ),
  };
}
