/**
 * Unit tests for the management case summary hooks (#774 — deeds, testimonies, copies, payments).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
}));

import { apiGet } from "@/lib/api-client";
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

  it("fetches the case summary via GET /managements/{id}/case-summary", async () => {
    vi.mocked(apiGet).mockResolvedValue({ managementId: 4, deeds: [] });

    const { result } = renderHook(() => useManagementCaseSummary(4), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/managements/4/case-summary");
  });

  it("does not fetch without a management id", () => {
    renderHook(() => useManagementCaseSummary(undefined), { wrapper: createWrapper() });

    expect(apiGet).not.toHaveBeenCalled();
  });
});

describe("useManagementFinancialSummary (CU47)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the financial summary via GET /managements/{id}/financial-summary", async () => {
    vi.mocked(apiGet).mockResolvedValue({ idManagement: 4, totalCollected: 100 });

    const { result } = renderHook(() => useManagementFinancialSummary(4), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/managements/4/financial-summary");
  });
});
