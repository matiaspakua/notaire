/**
 * Unit test for usePlantillaTramite (CU03 — documentos necesarios por trámite).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
}));

import { apiGet } from "@/lib/api-client";
import { plantillaTramiteKeys, usePlantillaTramite } from "@/hooks/usePlantillaTramite";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("plantillaTramiteKeys", () => {
  it("byTramite key includes the trámite id", () => {
    expect(plantillaTramiteKeys.byTramite(7)).toEqual(["plantillaTramite", "tramite", 7]);
  });
});

describe("usePlantillaTramite (CU03)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches required documents via GET /plantilla-tramite/tipo-tramite/{id}", async () => {
    vi.mocked(apiGet).mockResolvedValue([
      {
        observaciones: "",
        tipoDeTramite: { idTipoTramite: 7, nombre: "Compraventa" },
        tipoDeDocumento: { idTipoDocumento: 1, nombre: "DNI", vence: false, quienEntrega: "Cliente" },
      },
    ]);

    const { result } = renderHook(() => usePlantillaTramite(7), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/plantilla-tramite/tipo-tramite/7");
    expect(result.current.data?.[0].tipoDeDocumento?.nombre).toBe("DNI");
  });

  it("does not fetch when idTipoTramite is undefined", () => {
    const { result } = renderHook(() => usePlantillaTramite(undefined), { wrapper: createWrapper() });

    expect(result.current.fetchStatus).toBe("idle");
    expect(apiGet).not.toHaveBeenCalled();
  });
});
