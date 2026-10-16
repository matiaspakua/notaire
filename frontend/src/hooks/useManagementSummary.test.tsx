/**
 * Unit tests for the management case summary hooks (#774 — deeds, testimonies, copies, payments).
 * Hexagon wiring: #1839 / ADR-030.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/composition/management", () => ({
  managementUseCases: {
    getCaseSummary: vi.fn(),
    getFinancialSummary: vi.fn(),
  },
}));

import { managementUseCases } from "@/composition/management";
import { managementSummaryKeys, useManagementCaseSummary, useManagementFinancialSummary } from "@/hooks/useManagementSummary";

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("managementSummaryKeys", () => {
  it("case and financial keys include the management id", () => {
    expect(managementSummaryKeys.caseSummary(4)).toEqual(["management-case-summary", 4]);
    expect(managementSummaryKeys.financialSummary(4)).toEqual(["management-financial-summary", 4]);
  });
});

describe("useManagementCaseSummary (#774)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the case summary via the management use case", async () => {
    vi.mocked(managementUseCases.getCaseSummary).mockResolvedValue({
      managementId: 4,
      managementNumber: 4,
      deeds: [],
      documents: [],
    });

    const { result } = renderHook(() => useManagementCaseSummary(4), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(managementUseCases.getCaseSummary).toHaveBeenCalledWith(4);
  });

  it("does not fetch without a management id", () => {
    renderHook(() => useManagementCaseSummary(undefined), { wrapper: createWrapper() });

    expect(managementUseCases.getCaseSummary).not.toHaveBeenCalled();
  });
});

describe("useManagementFinancialSummary (CU47)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the financial summary via the management use case", async () => {
    vi.mocked(managementUseCases.getFinancialSummary).mockResolvedValue({
      idManagement: 4,
      totalBudgeted: 0,
      totalCollected: 100,
      pendingBalance: 0,
    });

    const { result } = renderHook(() => useManagementFinancialSummary(4), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(managementUseCases.getFinancialSummary).toHaveBeenCalledWith(4);
  });
});
