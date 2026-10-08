/**
 * The report download hooks must call the backend with the exact paths and query
 * parameter names of backend-api/openapi/openapi.yaml (issue #567).
 *
 * Covers: CU01, CU03, CU09, CU13 (Reportes)
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

const apiGetBytes = vi.fn();
vi.mock("@/lib/api-client", () => ({ apiGetBytes: (path: string) => apiGetBytes(path) }));

import {
  useReporteDeudaDocumentos,
  useReporteHistorialGestion,
  useReporteListaDocumentos,
  useReportePresupuesto,
  useReportePresupuestoInmuebles,
} from "@/hooks/useReportes";

beforeEach(() => {
  apiGetBytes.mockReset();
  apiGetBytes.mockResolvedValue(new Blob(["%PDF-"], { type: "application/pdf" }));
  URL.createObjectURL = vi.fn(() => "blob:report");
  URL.revokeObjectURL = vi.fn();
});

describe("report hooks call the documented endpoints", () => {
  it("budget report", async () => {
    await useReportePresupuesto().download(7);
    expect(apiGetBytes).toHaveBeenCalledWith("/reportes/presupuesto/7");
  });

  it("budget report with properties", async () => {
    await useReportePresupuestoInmuebles().download(7);
    expect(apiGetBytes).toHaveBeenCalledWith("/reportes/presupuesto-inmuebles/7");
  });

  it("procedure documents report encodes the procedure type name", async () => {
    await useReporteListaDocumentos().download("Compra/Venta & más");
    expect(apiGetBytes).toHaveBeenCalledWith(
      "/reportes/lista-documentos-tramite?nombreTipoTramite=Compra%2FVenta%20%26%20m%C3%A1s"
    );
  });

  it("management history report", async () => {
    await useReporteHistorialGestion().download(12);
    expect(apiGetBytes).toHaveBeenCalledWith("/reportes/historial-gestion/12");
  });

  it("document debt report sends numberManagement, the documented query parameter", async () => {
    await useReporteDeudaDocumentos().download(1001);
    expect(apiGetBytes).toHaveBeenCalledWith("/reportes/consultar-deuda-documentos?numberManagement=1001");
  });
});
