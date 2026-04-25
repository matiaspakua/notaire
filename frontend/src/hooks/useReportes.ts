/**
 * useReportes — PDF report download hooks
 * Wraps /api/v1/reportes/* endpoints that return application/pdf
 */

const BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

async function downloadPdf(path: string, filename: string): Promise<void> {
  const res = await fetch(`${BASE}${path}`, { headers: { Accept: "application/pdf" } });
  if (!res.ok) throw new Error(`Error generando reporte: ${res.status}`);
  const blob = await res.blob();
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
        `/reportes/presupuesto/${idPresupuesto}`,
        `presupuesto_${idPresupuesto}.pdf`
      ),
  };
}

/** CU08 — Reporte PDF de presupuesto con inmuebles */
export function useReportePresupuestoInmuebles() {
  return {
    download: (idPresupuesto: number) =>
      downloadPdf(
        `/reportes/presupuesto-inmuebles/${idPresupuesto}`,
        `presupuesto_inmuebles_${idPresupuesto}.pdf`
      ),
  };
}

/** CU02 — Lista de documentos para tipo de trámite */
export function useReporteListaDocumentos() {
  return {
    download: (nombreTipoTramite: string) =>
      downloadPdf(
        `/reportes/lista-documentos-tramite?nombreTipoTramite=${encodeURIComponent(nombreTipoTramite)}`,
        `lista_documentos_${nombreTipoTramite}.pdf`
      ),
  };
}

/** CU13 — Historial de gestión */
export function useReporteHistorialGestion() {
  return {
    download: (idGestion: number) =>
      downloadPdf(
        `/reportes/historial-gestion/${idGestion}`,
        `historial_gestion_${idGestion}.pdf`
      ),
  };
}

/** CU25 — Declaración Jurada Mensual */
export function useReporteDeclaracionJuradaMensual() {
  return {
    download: (anio: number, mes: number) =>
      downloadPdf(
        `/reportes/declaracion-jurada-mensual?anio=${anio}&mes=${mes}`,
        `ddjj_mensual_${anio}_${mes}.pdf`
      ),
  };
}

/** CU50 — Declaración Jurada de Rentas */
export function useReporteDeclaracionJuradaRentas() {
  return {
    download: (anio: number, mes: number) =>
      downloadPdf(
        `/reportes/declaracion-jurada-rentas?anio=${anio}&mes=${mes}`,
        `ddjj_rentas_${anio}_${mes}.pdf`
      ),
  };
}

/** CU24 — Libro Índice */
export function useReporteLibroIndice() {
  return {
    download: (anio: number) =>
      downloadPdf(
        `/reportes/libro-indice?anio=${anio}`,
        `libro_indice_${anio}.pdf`
      ),
  };
}

/** CU16 — Consultar deuda documentos */
export function useReporteDeudaDocumentos() {
  return {
    download: (numeroGestion: number) =>
      downloadPdf(
        `/reportes/consultar-deuda-documentos?numeroGestion=${numeroGestion}`,
        `deuda_documentos_${numeroGestion}.pdf`
      ),
  };
}
