/**
 * Unit test for useDocumentosEntidadExterna (CU10 — movimientos de
 * documentación de entidades externas).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPut: vi.fn(),
}));

import { apiGet, apiPut } from "@/lib/api-client";
import {
  documentosEntidadExternaKeys,
  useDocumentosEntidadExterna,
  useRegistrarMovimientoDocumentoEntidadExterna,
} from "@/hooks/useDocumentosEntidadExterna";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return { Wrapper, queryClient };
}

describe("documentosEntidadExternaKeys", () => {
  it("byGestion key includes the gestión id", () => {
    expect(documentosEntidadExternaKeys.byGestion(9)).toEqual(["documentosEntidadExterna", "gestion", 9]);
  });
});

describe("useDocumentosEntidadExterna (CU10)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the gestión's entidad-externa documents via GET", async () => {
    vi.mocked(apiGet).mockResolvedValue({
      idGestion: 9,
      numero: 1001,
      encabezado: "Gestion",
      nomenclaturaCatastral: "11-22-33",
      documentos: [{ idDocumentoPresentado: 1, nombre: "Certificado de Dominio", entregado: false }],
    });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useDocumentosEntidadExterna(9), { wrapper: Wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/gestiones/9/documentos-entidades-externas");
    expect(result.current.data?.documentos[0].nombre).toBe("Certificado de Dominio");
  });

  it("does not fetch when gestionId is undefined", () => {
    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useDocumentosEntidadExterna(undefined), { wrapper: Wrapper });

    expect(result.current.fetchStatus).toBe("idle");
    expect(apiGet).not.toHaveBeenCalled();
  });
});

describe("useRegistrarMovimientoDocumentoEntidadExterna (CU10)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("registers a movement via PUT", async () => {
    vi.mocked(apiPut).mockResolvedValue({ idDocumentoPresentado: 1, entregado: true });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useRegistrarMovimientoDocumentoEntidadExterna(), { wrapper: Wrapper });

    await result.current.mutateAsync({
      gestionId: 9,
      idDocumentoPresentado: 1,
      movimiento: { entregado: true },
    });

    expect(apiPut).toHaveBeenCalledWith("/gestiones/9/documentos-entidades-externas/1", { entregado: true });
  });
});
