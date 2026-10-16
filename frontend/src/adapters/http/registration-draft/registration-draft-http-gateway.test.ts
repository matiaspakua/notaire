// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetBytes: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
}));

import { apiPost, apiGetBytes } from "@/lib/api-client";
import { registrationDraftHttpGateway } from "./registration-draft-http-gateway";

describe("registrationDraftHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("generates a draft and downloads its PDF", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idRegistrationDraft: 1 });
    vi.mocked(apiGetBytes).mockResolvedValue(new Blob());
    await registrationDraftHttpGateway.generate(9);
    await registrationDraftHttpGateway.downloadReport(1);
    expect(apiPost).toHaveBeenCalledWith("/registration-drafts", { idDeed: 9 });
    expect(apiGetBytes).toHaveBeenCalledWith("/reports/registration-draft/1");
  });
});
