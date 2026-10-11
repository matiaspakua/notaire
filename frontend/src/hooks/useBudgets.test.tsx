/**
 * Unit tests for useCreateBudget (CU01, CU39 — create a budget from a procedure-type template).
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
import { useCreateBudget } from "@/hooks/useBudgets";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("useCreateBudget (CU01, CU39)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("creates only the budget when no procedure type is given", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idBudget: 7 });

    const { result } = renderHook(() => useCreateBudget(), { wrapper: createWrapper() });
    result.current.mutate({ data: { propertyAmount: 100 } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledTimes(1);
    expect(apiPost).toHaveBeenCalledWith("/budgets", { propertyAmount: 100 });
    expect(result.current.data).toMatchObject({ budget: { idBudget: 7 }, itemsLoaded: false });
  });

  it("loads the template items into the new budget when a procedure type is given", async () => {
    vi.mocked(apiPost).mockResolvedValueOnce({ idBudget: 7 }).mockResolvedValueOnce([{ idItem: 1 }]);

    const { result } = renderHook(() => useCreateBudget(), { wrapper: createWrapper() });
    result.current.mutate({ data: { propertyAmount: 100 }, procedureTypeId: 3 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenNthCalledWith(2, "/budgets/7/items-from-template?procedureTypeId=3", undefined);
    expect(result.current.data).toMatchObject({ itemsLoaded: true });
  });

  it("keeps the created budget and reports no items when the template step fails", async () => {
    vi.mocked(apiPost).mockResolvedValueOnce({ idBudget: 7 }).mockRejectedValueOnce(new Error("no template"));

    const { result } = renderHook(() => useCreateBudget(), { wrapper: createWrapper() });
    result.current.mutate({ data: { propertyAmount: 100 }, procedureTypeId: 3 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toMatchObject({ budget: { idBudget: 7 }, itemsLoaded: false });
  });
});
