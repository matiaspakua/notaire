// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiGet, apiPost, apiPut } from "@/lib/api-client";
import { submittedDocumentHttpGateway } from "./submitted-document-http-gateway";

describe("submittedDocumentHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("POSTs create with typeId, date, delivered and procedureId", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idSubmittedDocument: 1 });
    await submittedDocumentHttpGateway.create({
      typeId: 3,
      date: "2026-06-01",
      delivered: true,
      procedureId: 9,
    });
    expect(apiPost).toHaveBeenCalledWith("/submitted-documents", {
      typeId: 3,
      date: "2026-06-01",
      delivered: true,
      procedureId: 9,
    });
  });

  it("PUTs update with typeId, date and delivered", async () => {
    vi.mocked(apiPut).mockResolvedValue(undefined);
    await submittedDocumentHttpGateway.update(5, { typeId: 3, date: "2026-06-01", delivered: false });
    expect(apiPut).toHaveBeenCalledWith("/submitted-documents/5", {
      typeId: 3,
      date: "2026-06-01",
      delivered: false,
    });
  });

  it("GETs upcoming expirations with days", async () => {
    vi.mocked(apiGet).mockResolvedValue([]);
    await submittedDocumentHttpGateway.listUpcomingExpirations(30);
    expect(apiGet).toHaveBeenCalledWith("/submitted-documents/upcoming-expirations?days=30");
  });
});
