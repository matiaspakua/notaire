// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetBytes: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiGetBytes, apiPost, apiPut } from "@/lib/api-client";
import { testimonyHttpGateway } from "./testimony-http-gateway";

describe("testimonyHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("lists, generates, creates with deed, updates with version", async () => {
    vi.mocked(apiGet).mockResolvedValue([
      { idTestimony: 1, number: 3, version: 1, deed: { idDeed: 9, number: 90 } },
    ]);
    await expect(testimonyHttpGateway.list()).resolves.toMatchObject([
      { idTestimony: 1, deed: { idDeed: 9, number: 90 } },
    ]);

    vi.mocked(apiPost).mockResolvedValue({ idTestimony: 2, number: 4, deed: { idDeed: 8 } });
    await testimonyHttpGateway.generate(8);
    expect(apiPost).toHaveBeenCalledWith("/testimonies/8/generate", {});

    const createBody = {
      deed: { idDeed: 8, number: 80 },
      number: 4,
      notes: "",
      flagged: false,
      verified: false,
    };
    await testimonyHttpGateway.create(createBody);
    expect(apiPost).toHaveBeenCalledWith("/testimonies", createBody);

    vi.mocked(apiPut).mockResolvedValue(undefined);
    const update = { number: 4, notes: "n", flagged: false, verified: true, version: 1 };
    await testimonyHttpGateway.update(2, update);
    expect(apiPut).toHaveBeenCalledWith("/testimonies/2", update);

    vi.mocked(apiDelete).mockResolvedValue(undefined);
    await testimonyHttpGateway.delete(2);
    expect(apiDelete).toHaveBeenCalledWith("/testimonies/2");

    const blob = new Blob(["pdf"]);
    vi.mocked(apiGetBytes).mockResolvedValue(blob);
    await expect(testimonyHttpGateway.downloadCopy(2)).resolves.toBe(blob);
  });
});
