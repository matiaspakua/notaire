// @vitest-environment node
/**
 * HTTP gateway for managements (#1839 / ADR-030).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetPage: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiGet, apiGetPage, apiPost, apiPut } from "@/lib/api-client";
import { managementHttpGateway, toCompleteCaseRequest } from "./management-http-gateway";

describe("toCompleteCaseRequest", () => {
  it("maps procedureTypeId to wire typeProcedureId", () => {
    expect(
      toCompleteCaseRequest({
        number: 80,
        budgetId: 3,
        notaryId: 5,
        statusManagementId: 2,
        procedureTypeId: 9,
      }),
    ).toEqual({
      number: 80,
      heading: undefined,
      notes: undefined,
      budgetId: 3,
      notaryId: 5,
      statusManagementId: 2,
      typeProcedureId: 9,
      propertyId: undefined,
    });
    expect(
      toCompleteCaseRequest({
        number: 80,
        budgetId: 3,
        notaryId: 5,
        statusManagementId: 2,
        procedureTypeId: 9,
      }),
    ).not.toHaveProperty("procedureTypeId");
  });
});

describe("managementHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("lists a page and maps content", async () => {
    vi.mocked(apiGetPage).mockResolvedValue({
      content: [{ idManagement: 1, number: 10 }],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 20,
    });
    const page = await managementHttpGateway.listPage({
      page: 0,
      size: 20,
      sort: "idManagement,desc",
    });
    expect(apiGetPage).toHaveBeenCalledWith("/managements", {
      page: 0,
      size: 20,
      sort: "idManagement,desc",
    });
    expect(page.content[0]).toMatchObject({ idManagement: 1, number: 10 });
  });

  it("POSTs complete-case with CompleteCaseRequest wire names", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idManagement: 8 });
    await managementHttpGateway.createCompleteCase({
      number: 80,
      budgetId: 3,
      notaryId: 5,
      statusManagementId: 2,
      procedureTypeId: 9,
    });
    expect(apiPost).toHaveBeenCalledWith(
      "/managements/complete-case",
      expect.objectContaining({ typeProcedureId: 9, budgetId: 3, notaryId: 5, statusManagementId: 2 }),
    );
    expect(vi.mocked(apiPost).mock.calls[0][1]).not.toHaveProperty("procedureTypeId");
  });

  it("PUTs complete-case with CompleteCaseRequest wire names", async () => {
    vi.mocked(apiPut).mockResolvedValue({ idManagement: 7 });
    await managementHttpGateway.updateCompleteCase(7, {
      number: 70,
      heading: "Header",
      notes: "n",
      budgetId: 3,
      notaryId: 5,
      statusManagementId: 2,
      procedureTypeId: 9,
    });
    expect(apiPut).toHaveBeenCalledWith("/managements/7/complete-case", {
      number: 70,
      heading: "Header",
      notes: "n",
      budgetId: 3,
      notaryId: 5,
      statusManagementId: 2,
      typeProcedureId: 9,
      propertyId: undefined,
    });
  });

  it("returns null when findByNumber gets 404", async () => {
    vi.mocked(apiGet).mockRejectedValue({ status: 404 });
    await expect(managementHttpGateway.findByNumber(99)).resolves.toBeNull();
  });

  it("archives with confirmado query flag", async () => {
    vi.mocked(apiPost).mockResolvedValue({
      idManagement: 1,
      pendingBalance: 0,
      pendingDebtAtArchiving: false,
    });
    await managementHttpGateway.archive(1, true);
    expect(apiPost).toHaveBeenCalledWith("/managements/1/archive?confirmado=true", {});
  });

  it("loads case and financial summaries", async () => {
    vi.mocked(apiGet)
      .mockResolvedValueOnce({ managementId: 4, managementNumber: 4, deeds: [], documents: [] })
      .mockResolvedValueOnce({ idManagement: 4, totalBudgeted: 1, totalCollected: 1, pendingBalance: 0 });
    await managementHttpGateway.getCaseSummary(4);
    await managementHttpGateway.getFinancialSummary(4);
    expect(apiGet).toHaveBeenCalledWith("/managements/4/case-summary");
    expect(apiGet).toHaveBeenCalledWith("/managements/4/financial-summary");
  });
});
