/**
 * Unit tests for the gestión case summary hooks (#774 — escrituras, testimonios, copias, pagos).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
}));

import { apiGet } from "@/lib/api-client";
import { gestionResumenKeys, useGestionResumenCaso, useGestionResumenFinanciero } from "@/hooks/useGestionResumen";

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("gestionResumenKeys", () => {
  it("caso and financiero keys include the gestión id", () => {
    expect(gestionResumenKeys.caso(4)).toEqual(["gestion-resumen-caso", 4]);
    expect(gestionResumenKeys.financiero(4)).toEqual(["gestion-resumen-financiero", 4]);
  });
});

describe("useGestionResumenCaso (#774)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the case summary via GET /gestiones/{id}/resumen-caso", async () => {
    vi.mocked(apiGet).mockResolvedValue({ managementId: 4, deeds: [] });

    const { result } = renderHook(() => useGestionResumenCaso(4), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/gestiones/4/resumen-caso");
  });

  it("does not fetch without a gestión id", () => {
    renderHook(() => useGestionResumenCaso(undefined), { wrapper: createWrapper() });

    expect(apiGet).not.toHaveBeenCalled();
  });
});

describe("useGestionResumenFinanciero (CU47)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the financial summary via GET /gestiones/{id}/resumen-financiero", async () => {
    vi.mocked(apiGet).mockResolvedValue({ idManagement: 4, totalCobrado: 100 });

    const { result } = renderHook(() => useGestionResumenFinanciero(4), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/gestiones/4/resumen-financiero");
  });
});
