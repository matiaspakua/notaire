/**
 * Issue #1360 — full management edit. The edit dialog prefills from
 * GET /managements/{id}/complete-case and saves every field through
 * PUT /managements/{id}/complete-case with the backend's CompleteCaseRequest names.
 * After #1839 hooks call composition; wire mapping is covered in the HTTP gateway test.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor, act } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/composition/management", () => ({
  managementUseCases: {
    getCompleteCase: vi.fn(),
    updateCompleteCase: vi.fn(),
  },
}));

import { managementUseCases } from "@/composition/management";
import { managementsKeys, useManagementCompleteCase, useUpdateCompleteManagement } from "@/hooks/useManagements";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return { Wrapper, queryClient };
}

describe("useManagementCompleteCase (#1360)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("reads the case values from the management use case", async () => {
    vi.mocked(managementUseCases.getCompleteCase).mockResolvedValue({
      idManagement: 7,
      number: 70,
      budgetId: 3,
      notaryId: 4,
    });
    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useManagementCompleteCase(7), { wrapper: Wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(managementUseCases.getCompleteCase).toHaveBeenCalledWith(7);
    expect(result.current.data?.notaryId).toBe(4);
  });

  it("does not fetch without an id", () => {
    const { Wrapper } = createWrapper();
    renderHook(() => useManagementCompleteCase(undefined), { wrapper: Wrapper });
    expect(managementUseCases.getCompleteCase).not.toHaveBeenCalled();
  });
});

describe("useUpdateCompleteManagement (#1360)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("updates through the use case and refreshes the lists", async () => {
    vi.mocked(managementUseCases.updateCompleteCase).mockResolvedValue({ idManagement: 7, notes: "n" });
    const { Wrapper, queryClient } = createWrapper();
    const invalidate = vi.spyOn(queryClient, "invalidateQueries");
    const { result } = renderHook(() => useUpdateCompleteManagement(), { wrapper: Wrapper });

    await act(() =>
      result.current.mutateAsync({
        id: 7,
        data: {
          number: 70,
          heading: "Header",
          notes: "n",
          budgetId: 3,
          notaryId: 5,
          statusManagementId: 2,
          procedureTypeId: 9,
          propertyId: undefined,
        },
      }),
    );

    expect(managementUseCases.updateCompleteCase).toHaveBeenCalledWith(7, {
      number: 70,
      heading: "Header",
      notes: "n",
      budgetId: 3,
      notaryId: 5,
      statusManagementId: 2,
      procedureTypeId: 9,
      propertyId: undefined,
    });
    expect(invalidate).toHaveBeenCalledWith({ queryKey: managementsKeys.all });
  });
});
