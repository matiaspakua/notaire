// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetBytes: vi.fn(),
  apiPost: vi.fn(),
}));

import { apiGet, apiGetBytes, apiPost } from "@/lib/api-client";
import { notebookHttpGateway } from "./notebook-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiGetBytesMock = vi.mocked(apiGetBytes);
const apiPostMock = vi.mocked(apiPost);

describe("notebookHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("lists, creates, and downloads cover", async () => {
    apiGetMock.mockResolvedValue([{ idNotebook: 1, number: 3, year: 2026 }]);
    await expect(notebookHttpGateway.list()).resolves.toEqual([
      { idNotebook: 1, number: 3, year: 2026 },
    ]);

    apiPostMock.mockResolvedValue(undefined);
    await notebookHttpGateway.create({ idsFolio: [1, 2], idNotary: 9, year: 2026, notes: null });
    expect(apiPostMock).toHaveBeenCalledWith("/notebooks", {
      idsFolio: [1, 2],
      idNotary: 9,
      year: 2026,
      notes: null,
    });

    const blob = new Blob(["x"]);
    apiGetBytesMock.mockResolvedValue(blob);
    await expect(notebookHttpGateway.downloadCover(1)).resolves.toBe(blob);
    expect(apiGetBytesMock).toHaveBeenCalledWith("/notebooks/1/cover");
  });
});
