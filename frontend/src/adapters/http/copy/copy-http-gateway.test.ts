// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";
vi.mock("@/lib/api-client", () => ({ apiGet: vi.fn(), apiPost: vi.fn(), apiPut: vi.fn(), apiDelete: vi.fn() }));
import { apiGet, apiPost } from "@/lib/api-client";
import { copyHttpGateway } from "./copy-http-gateway";

describe("copyHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());
  it("lists and creates", async () => {
    vi.mocked(apiGet).mockResolvedValue([]);
    vi.mocked(apiPost).mockResolvedValue(undefined);
    await copyHttpGateway.list();
    await copyHttpGateway.create({});
    expect(apiGet).toHaveBeenCalledWith("/copies");
    expect(apiPost).toHaveBeenCalledWith("/copies", {});
  });
});
