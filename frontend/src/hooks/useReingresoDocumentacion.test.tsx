/**
 * Unit test for useReingresoDocumentacion (CU43 — reingresar documentación).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
}));

import { apiGet, apiPost } from "@/lib/api-client";
import {
  reingresoDocumentacionKeys,
  useReingresoDocumentacion,
  useReingresarDocumentacion,
} from "@/hooks/useReingresoDocumentacion";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return { Wrapper, queryClient };
}

describe("reingresoDocumentacionKeys", () => {
  it("byGestion key includes the gestión id", () => {
    expect(reingresoDocumentacionKeys.byGestion(9)).toEqual(["reingresoDocumentacion", "gestion", 9]);
  });
});

describe("useReingresoDocumentacion (CU43)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the gestión's trámites with documentación necesaria via GET", async () => {
    vi.mocked(apiGet).mockResolvedValue({
      idGestion: 9,
      numero: 1001,
      encabezado: "Gestion",
      tramites: [
        {
          idTramite: 1,
          tipoTramiteNombre: "Compraventa",
          documentosNecesarios: [{ idTipoDocumento: 5, nombre: "Certificado de Dominio", vence: true }],
        },
      ],
    });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useReingresoDocumentacion(9), { wrapper: Wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/gestiones/9/reingreso-documentacion");
    expect(result.current.data?.tramites[0].documentosNecesarios[0].nombre).toBe("Certificado de Dominio");
  });

  it("does not fetch when gestionId is undefined", () => {
    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useReingresoDocumentacion(undefined), { wrapper: Wrapper });

    expect(result.current.fetchStatus).toBe("idle");
    expect(apiGet).not.toHaveBeenCalled();
  });
});

describe("useReingresarDocumentacion (CU43)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("reingresa a document via POST", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idDocumentoPresentado: 1, reingresado: true });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useReingresarDocumentacion(), { wrapper: Wrapper });

    await result.current.mutateAsync({
      gestionId: 9,
      reingreso: { idTramite: 1, idTipoDocumento: 5 },
    });

    expect(apiPost).toHaveBeenCalledWith("/gestiones/9/reingreso-documentacion", { idTramite: 1, idTipoDocumento: 5 });
  });
});
