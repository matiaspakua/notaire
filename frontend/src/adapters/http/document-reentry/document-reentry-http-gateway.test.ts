// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
}));

import { apiGet, apiPost } from "@/lib/api-client";
import { documentReentryHttpGateway } from "./document-reentry-http-gateway";

describe("documentReentryHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("GETs management document-reentry", async () => {
    vi.mocked(apiGet).mockResolvedValue({ idManagement: 9, procedures: [] });
    await documentReentryHttpGateway.getByManagement(9);
    expect(apiGet).toHaveBeenCalledWith("/managements/9/document-reentry");
  });

  it("POSTs reentry with idProcedure and idDocumentType", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idSubmittedDocument: 1, reentered: true });
    await documentReentryHttpGateway.reenter(9, { idProcedure: 1, idDocumentType: 5 });
    expect(apiPost).toHaveBeenCalledWith("/managements/9/document-reentry", {
      idProcedure: 1,
      idDocumentType: 5,
    });
  });
});
