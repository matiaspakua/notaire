/**
 * The report download hooks must call the backend with the exact paths and query
 * parameter names of backend-api/openapi/openapi.yaml (issue #567).
 *
 * Covers: CU01, CU03, CU09, CU13 (Reports)
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

const apiGetBytes = vi.fn();
vi.mock("@/lib/api-client", () => ({ apiGetBytes: (path: string) => apiGetBytes(path) }));

import {
  useDocumentDebtReport,
  useManagementHistoryReport,
  useDocumentListReport,
  useBudgetReport,
  useBudgetPropertiesReport,
} from "@/hooks/useReports";

beforeEach(() => {
  apiGetBytes.mockReset();
  apiGetBytes.mockResolvedValue(new Blob(["%PDF-"], { type: "application/pdf" }));
  URL.createObjectURL = vi.fn(() => "blob:report");
  URL.revokeObjectURL = vi.fn();
});

describe("report hooks call the documented endpoints", () => {
  it("budget report", async () => {
    await useBudgetReport().download(7);
    expect(apiGetBytes).toHaveBeenCalledWith("/reports/budget/7");
  });

  it("budget report with properties", async () => {
    await useBudgetPropertiesReport().download(7);
    expect(apiGetBytes).toHaveBeenCalledWith("/reports/budget-properties/7");
  });

  it("procedure documents report encodes the procedure type name", async () => {
    await useDocumentListReport().download("Purchase/Sale & more");
    expect(apiGetBytes).toHaveBeenCalledWith(
      "/reports/procedure-document-list?procedureTypeName=Purchase%2FSale%20%26%20more"
    );
  });

  it("management history report", async () => {
    await useManagementHistoryReport().download(12);
    expect(apiGetBytes).toHaveBeenCalledWith("/reports/management-history/12");
  });

  it("document debt report sends numberManagement, the documented query parameter", async () => {
    await useDocumentDebtReport().download(1001);
    expect(apiGetBytes).toHaveBeenCalledWith("/reports/document-debt?numberManagement=1001");
  });
});
