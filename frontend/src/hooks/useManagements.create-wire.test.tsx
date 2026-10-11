/**
 * #1495 regression (fixed under #1502): the English rename named the create input field
 * procedureTypeId, but ManagementController's CompleteCaseRequest still reads
 * typeProcedureId, so every create from /dashboard/managements answered 400.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetPage: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiPost } from "@/lib/api-client";
import { useCreateCompleteManagement } from "@/hooks/useManagements";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return { Wrapper };
}

describe("useCreateCompleteManagement wire names", () => {
  beforeEach(() => vi.clearAllMocks());

  it("POSTs typeProcedureId, the CompleteCaseRequest name, not the input's procedureTypeId", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idManagement: 8 });
    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useCreateCompleteManagement(), { wrapper: Wrapper });
    await act(() =>
      result.current.mutateAsync({
        number: 80,
        budgetId: 3,
        notaryId: 5,
        statusManagementId: 2,
        procedureTypeId: 9,
      }),
    );
    expect(apiPost).toHaveBeenCalledWith(
      "/managements/complete-case",
      expect.objectContaining({ typeProcedureId: 9, budgetId: 3, notaryId: 5, statusManagementId: 2 }),
    );
    expect(vi.mocked(apiPost).mock.calls[0][1]).not.toHaveProperty("procedureTypeId");
  });
});
