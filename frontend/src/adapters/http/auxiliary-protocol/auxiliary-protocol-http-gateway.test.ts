// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
}));

import { apiGet, apiPost } from "@/lib/api-client";
import { auxiliaryProtocolHttpGateway } from "./auxiliary-protocol-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiPostMock = vi.mocked(apiPost);

describe("auxiliaryProtocolHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("lists available folios and starts a deed", async () => {
    apiGetMock.mockResolvedValue([{ idFolio: 2, number: 8 }]);
    await expect(auxiliaryProtocolHttpGateway.listAvailableFolios()).resolves.toEqual([
      { idFolio: 2, number: 8 },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/auxiliary-protocol/available-folios");

    apiPostMock.mockResolvedValue(undefined);
    await auxiliaryProtocolHttpGateway.startDeed({ idFolio: 2, body: "text" });
    expect(apiPostMock).toHaveBeenCalledWith("/auxiliary-protocol/deeds", {
      idFolio: 2,
      body: "text",
    });
  });
});
