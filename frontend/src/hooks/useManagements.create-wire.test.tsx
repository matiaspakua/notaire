/**
 * #1495 regression (fixed under #1502): the English rename named the create input field
 * procedureTypeId, but ManagementController's CompleteCaseRequest still reads
 * typeProcedureId, so every create from /dashboard/managements answered 400.
 * After #1839 the wire mapping lives in the HTTP gateway — hook delegates to use cases.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/composition/management", () => ({
  managementUseCases: {
    createCompleteCase: vi.fn(),
  },
}));

import { managementUseCases } from "@/composition/management";
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

  it("passes procedureTypeId through to the use case (gateway maps to typeProcedureId)", async () => {
    vi.mocked(managementUseCases.createCompleteCase).mockResolvedValue({ idManagement: 8 });
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
    expect(managementUseCases.createCompleteCase).toHaveBeenCalledWith({
      number: 80,
      budgetId: 3,
      notaryId: 5,
      statusManagementId: 2,
      procedureTypeId: 9,
    });
  });
});
