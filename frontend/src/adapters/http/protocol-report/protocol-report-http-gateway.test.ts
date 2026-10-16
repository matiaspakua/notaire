// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGetBytes: vi.fn(),
}));

import { apiGetBytes } from "@/lib/api-client";
import { protocolReportHttpGateway } from "./protocol-report-http-gateway";

const apiGetBytesMock = vi.mocked(apiGetBytes);

describe("protocolReportHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("downloads bytes for the report path", async () => {
    const blob = new Blob(["pdf"]);
    apiGetBytesMock.mockResolvedValue(blob);
    await expect(protocolReportHttpGateway.download("/reports/index-book?year=2026")).resolves.toBe(
      blob,
    );
    expect(apiGetBytesMock).toHaveBeenCalledWith("/reports/index-book?year=2026");
  });
});
