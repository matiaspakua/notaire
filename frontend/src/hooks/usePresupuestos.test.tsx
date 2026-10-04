/**
 * Unit tests for useCreatePresupuesto (CU01, CU39 — create a presupuesto from a procedure-type template).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiPost } from "@/lib/api-client";
import { useCreatePresupuesto } from "@/hooks/usePresupuestos";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("useCreatePresupuesto (CU01, CU39)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("creates only the presupuesto when no tipo de trámite is given", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idBudget: 7 });

    const { result } = renderHook(() => useCreatePresupuesto(), { wrapper: createWrapper() });
    result.current.mutate({ data: { propertyAmount: 100 } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledTimes(1);
    expect(apiPost).toHaveBeenCalledWith("/presupuestos", { propertyAmount: 100 });
    expect(result.current.data).toMatchObject({ presupuesto: { idBudget: 7 }, itemsLoaded: false });
  });

  it("loads the template items into the new presupuesto when a tipo de trámite is given", async () => {
    vi.mocked(apiPost).mockResolvedValueOnce({ idBudget: 7 }).mockResolvedValueOnce([{ idItem: 1 }]);

    const { result } = renderHook(() => useCreatePresupuesto(), { wrapper: createWrapper() });
    result.current.mutate({ data: { propertyAmount: 100 }, tipoTramiteId: 3 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenNthCalledWith(2, "/presupuestos/7/items-desde-plantilla?tipoTramiteId=3", undefined);
    expect(result.current.data).toMatchObject({ itemsLoaded: true });
  });

  it("keeps the created presupuesto and reports no items when the template step fails", async () => {
    vi.mocked(apiPost).mockResolvedValueOnce({ idBudget: 7 }).mockRejectedValueOnce(new Error("no template"));

    const { result } = renderHook(() => useCreatePresupuesto(), { wrapper: createWrapper() });
    result.current.mutate({ data: { propertyAmount: 100 }, tipoTramiteId: 3 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toMatchObject({ presupuesto: { idBudget: 7 }, itemsLoaded: false });
  });
});
