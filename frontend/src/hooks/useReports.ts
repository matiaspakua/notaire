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

/** CU01/CU39 — Budget PDF report */
export function useBudgetReport() {
  return {
    download: (idBudget: number) =>
      downloadPdf(
        `/reports/budget/${idBudget}`,
        `presupuesto_${idBudget}.pdf`
      ),
  };
}

/** CU01 — Budget PDF report that includes properties */
export function useBudgetPropertiesReport() {
  return {
    download: (idBudget: number) =>
      downloadPdf(
        `/reports/budget-properties/${idBudget}`,
        `presupuesto_inmuebles_${idBudget}.pdf`
      ),
  };
}

/** CU02 — Document list for a procedure type */
export function useDocumentListReport() {
  return {
    download: (procedureTypeName: string) =>
      downloadPdf(
        `/reports/procedure-document-list?procedureTypeName=${encodeURIComponent(procedureTypeName)}`,
        `lista_documentos_${procedureTypeName}.pdf`
      ),
  };
}

/** CU13 — Management history report */
export function useManagementHistoryReport() {
  return {
    download: (idManagement: number) =>
      downloadPdf(
        `/reports/management-history/${idManagement}`,
        `historial_gestion_${idManagement}.pdf`
      ),
  };
}

/** CU25 — Monthly sworn statement */
export function useMonthlySwornStatementReport() {
  return {
    download: (year: number, month: number) =>
      downloadPdf(
        `/reports/monthly-sworn-statement?year=${year}&month=${month}`,
        `ddjj_mensual_${year}_${month}.pdf`
      ),
  };
}

/** CU50 — Tax sworn statement */
export function useTaxSwornStatementReport() {
  return {
    download: (year: number, month: number) =>
      downloadPdf(
        `/reports/tax-sworn-statement?year=${year}&month=${month}`,
        `ddjj_rentas_${year}_${month}.pdf`
      ),
  };
}

/** CU24 — Index book */
export function useIndexBookReport() {
  return {
    download: (year: number, idNotary?: number, idFolioType?: number) => {
      const params = new URLSearchParams({ year: String(year) });
      if (idNotary) {
        params.set("idNotary", String(idNotary));
      }
      if (idFolioType) {
        params.set("idFolioType", String(idFolioType));
      }
      return downloadPdf(`/reports/index-book?${params.toString()}`, `libro_indice_${year}.pdf`);
    },
  };
}

/** CU09 — Document debt report (query param numberManagement, see openapi.yaml) */
export function useDocumentDebtReport() {
  return {
    download: (numberManagement: number) =>
      downloadPdf(
        `/reports/document-debt?numberManagement=${numberManagement}`,
        `deuda_documentos_${numberManagement}.pdf`
      ),
  };
}

/** CU15 — Payment receipt */
export function usePaymentReceipt() {
  return {
    download: (idPayment: number) =>
      downloadPdf(
        `/reports/payment-receipt/${idPayment}`,
        `recibo_pago_${idPayment}.pdf`
      ),
  };
}
