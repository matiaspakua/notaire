// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGetPage: vi.fn(),
}));

import { apiGetPage } from "@/lib/api-client";
import { auditHttpGateway } from "./audit-http-gateway";

describe("auditHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("pages audit-log newest first with filters", async () => {
    vi.mocked(apiGetPage).mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: 20,
    });
    await auditHttpGateway.listPage({
      page: 0,
      size: 20,
      module: "People",
      idUser: 3,
      from: "2026-01-01",
      to: "2026-01-31",
    });
    expect(apiGetPage).toHaveBeenCalledWith("/audit-log", {
      page: 0,
      size: 20,
      sort: "date,desc",
      params: { module: "People", idUser: 3, from: "2026-01-01", to: "2026-01-31" },
    });
  });
});
