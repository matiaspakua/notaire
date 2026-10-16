// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";
vi.mock("@/lib/api-client", () => ({ apiGet: vi.fn(), apiPost: vi.fn(), apiPut: vi.fn(), apiDelete: vi.fn() }));
import { apiGet, apiPost } from "@/lib/api-client";
import { substitutionHttpGateway } from "./substitution-http-gateway";

describe("substitutionHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());
  it("lists and creates", async () => {
    vi.mocked(apiGet).mockResolvedValue([]);
    vi.mocked(apiPost).mockResolvedValue(undefined);
    await substitutionHttpGateway.list();
    await substitutionHttpGateway.create({});
    expect(apiGet).toHaveBeenCalledWith("/substitutions");
    expect(apiPost).toHaveBeenCalledWith("/substitutions", {});
  });
});
