// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPut: vi.fn(),
}));

import { apiGet, apiPut } from "@/lib/api-client";
import { externalEntityDocumentHttpGateway } from "./external-entity-document-http-gateway";

describe("externalEntityDocumentHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("GETs external-entity documents for a management", async () => {
    vi.mocked(apiGet).mockResolvedValue({ idManagement: 9, documents: [] });
    await externalEntityDocumentHttpGateway.getByManagement(9);
    expect(apiGet).toHaveBeenCalledWith("/managements/9/external-entity-documents");
  });

  it("PUTs a movement", async () => {
    vi.mocked(apiPut).mockResolvedValue({ idSubmittedDocument: 1, delivered: true });
    await externalEntityDocumentHttpGateway.registerMovement(9, 1, { delivered: true });
    expect(apiPut).toHaveBeenCalledWith("/managements/9/external-entity-documents/1", {
      delivered: true,
    });
  });
});
