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
export function useReportePresupuesto() {
  return {
    download: (idPresupuesto: number) =>
      downloadPdf(
        `/reports/budget/${idPresupuesto}`,
        `presupuesto_${idPresupuesto}.pdf`
      ),
  };
}

/** CU01 — Reporte PDF de presupuesto con inmuebles */
export function useReportePresupuestoInmuebles() {
  return {
    download: (idPresupuesto: number) =>
      downloadPdf(
        `/reports/budget-properties/${idPresupuesto}`,
        `presupuesto_inmuebles_${idPresupuesto}.pdf`
      ),
  };
}

/** CU02 — Lista de documentos para tipo de trámite */
export function useReporteListaDocumentos() {
  return {
    download: (nombreTipoTramite: string) =>
      downloadPdf(
        `/reports/procedure-document-list?nombreTipoTramite=${encodeURIComponent(nombreTipoTramite)}`,
        `lista_documentos_${nombreTipoTramite}.pdf`
      ),
  };
}

/** CU13 — ManagementHistory de gestión */
export function useReporteHistorialGestion() {
  return {
    download: (idGestion: number) =>
      downloadPdf(
        `/reports/management-history/${idGestion}`,
        `historial_gestion_${idGestion}.pdf`
      ),
  };
}

/** CU25 — Declaración Jurada Mensual */
export function useReporteDeclaracionJuradaMensual() {
  return {
    download: (anio: number, mes: number) =>
      downloadPdf(
        `/reports/monthly-sworn-statement?anio=${anio}&mes=${mes}`,
        `ddjj_mensual_${anio}_${mes}.pdf`
      ),
  };
}

/** CU50 — Declaración Jurada de Rentas */
export function useReporteDeclaracionJuradaRentas() {
  return {
    download: (anio: number, mes: number) =>
      downloadPdf(
        `/reports/tax-sworn-statement?anio=${anio}&mes=${mes}`,
        `ddjj_rentas_${anio}_${mes}.pdf`
      ),
  };
}

/** CU24 — Libro Índice */
export function useReporteLibroIndice() {
  return {
    download: (anio: number) =>
      downloadPdf(
        `/reports/index-book?anio=${anio}`,
        `libro_indice_${anio}.pdf`
      ),
  };
}

/** CU09 — Consultar deuda de documentos (query param numberManagement, see openapi.yaml) */
export function useReporteDeudaDocumentos() {
  return {
    download: (numeroGestion: number) =>
      downloadPdf(
        `/reports/document-debt?numberManagement=${numeroGestion}`,
        `deuda_documentos_${numeroGestion}.pdf`
      ),
  };
}

/** CU15 — Emitir recibo de pago */
export function useReciboPago() {
  return {
    download: (idPago: number) =>
      downloadPdf(
        `/reports/payment-receipt/${idPago}`,
        `recibo_pago_${idPago}.pdf`
      ),
  };
}
