/**
 * Unit tests for the post-firma legal cycle hooks (testimonio generation/
 * verification, copia download, and Registro de la Propiedad movements).
 *
 * Covers: useTestimonios, useMovimientosTestimonio (CU07, CU08, CU11, CU12, CU44)
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiGetBytes: vi.fn(),
}));

import { apiGet, apiPost, apiGetBytes } from "@/lib/api-client";
import {
  testimoniosKeys,
  useTestimonios,
  useGenerarTestimonio,
  useVerificarTestimonio,
  descargarCopiaTestimonio,
} from "@/hooks/useTestimonios";
import {
  useIngresarInscripcion,
  useRegistrarInscripcion,
  useRetirar,
  useReingresar,
} from "@/hooks/useMovimientosTestimonio";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("testimoniosKeys (CU07, CU08)", () => {
  it("all key is ['testimonios']", () => {
    expect(testimoniosKeys.all).toEqual(["testimonios"]);
  });
});

describe("useTestimonios (CU07, CU08)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the testimonio list via GET /testimonio", async () => {
    vi.mocked(apiGet).mockResolvedValue([{ idTestimonio: 1, numero: 10 }]);

    const { result } = renderHook(() => useTestimonios(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/testimonio");
    expect(result.current.data).toEqual([{ idTestimonio: 1, numero: 10 }]);
  });
});

describe("useGenerarTestimonio (CU07)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("posts to /testimonio/{idEscritura}/generar", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idTestimonio: 2, numero: 20 });

    const { result } = renderHook(() => useGenerarTestimonio(), { wrapper: createWrapper() });
    result.current.mutate(5);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/testimonio/5/generar", {});
  });
});

describe("useVerificarTestimonio (CU08)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("posts observado and observaciones to /testimonio/{id}/verificar", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idTestimonio: 3, verificado: true });

    const { result } = renderHook(() => useVerificarTestimonio(), { wrapper: createWrapper() });
    result.current.mutate({ id: 3, observado: true, observaciones: "Falta firma" });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/testimonio/3/verificar", {
      observado: true,
      observaciones: "Falta firma",
    });
  });
});

describe("descargarCopiaTestimonio", () => {
  beforeEach(() => vi.clearAllMocks());

  it("downloads the PDF blob from /reportes/testimonio/{id}/copia", async () => {
    const blob = new Blob(["pdf"], { type: "application/pdf" });
    vi.mocked(apiGetBytes).mockResolvedValue(blob);
    const originalCreateElement = document.createElement.bind(document);
    const clickSpy = vi.fn();
    vi.spyOn(document, "createElement").mockImplementation((tag: string) => {
      const el = originalCreateElement(tag);
      if (tag === "a") el.click = clickSpy;
      return el;
    });
    URL.createObjectURL = vi.fn().mockReturnValue("blob:mock-url");
    URL.revokeObjectURL = vi.fn();

    await descargarCopiaTestimonio(7);

    expect(apiGetBytes).toHaveBeenCalledWith("/reportes/testimonio/7/copia");
    expect(clickSpy).toHaveBeenCalled();
    expect(URL.revokeObjectURL).toHaveBeenCalledWith("blob:mock-url");

    vi.mocked(document.createElement).mockRestore();
  });
});

describe("Movimiento de testimonio hooks (CU11, CU12, CU44)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("useIngresarInscripcion posts to /movimiento-testimonio/{id}/ingresar-inscripcion", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idMovimientoTestimonio: 1 });

    const { result } = renderHook(() => useIngresarInscripcion(), { wrapper: createWrapper() });
    result.current.mutate(5);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/movimiento-testimonio/5/ingresar-inscripcion", {});
  });

  it("useRegistrarInscripcion posts to /movimiento-testimonio/{id}/registrar-inscripcion", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idMovimientoTestimonio: 1, inscripta: true });

    const { result } = renderHook(() => useRegistrarInscripcion(), { wrapper: createWrapper() });
    result.current.mutate(5);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/movimiento-testimonio/5/registrar-inscripcion", {});
  });

  it("useRetirar posts numeroCarton to /movimiento-testimonio/{id}/retirar", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idMovimientoTestimonio: 1, numeroCarton: 123 });

    const { result } = renderHook(() => useRetirar(), { wrapper: createWrapper() });
    result.current.mutate({ idTestimonio: 5, numeroCarton: 123 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/movimiento-testimonio/5/retirar", { numeroCarton: 123 });
  });

  it("useReingresar posts to /movimiento-testimonio/{id}/reingresar", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idMovimientoTestimonio: 2 });

    const { result } = renderHook(() => useReingresar(), { wrapper: createWrapper() });
    result.current.mutate(5);

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/movimiento-testimonio/5/reingresar", {});
  });
});
