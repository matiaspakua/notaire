// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";
vi.mock("@/lib/api-client", () => ({ apiGet: vi.fn(), apiPost: vi.fn(), apiPut: vi.fn(), apiDelete: vi.fn() }));
import { apiGet, apiPost } from "@/lib/api-client";
import { propertyHttpGateway } from "./property-http-gateway";

describe("propertyHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());
  it("lists and creates", async () => {
    vi.mocked(apiGet).mockResolvedValue([]);
    vi.mocked(apiPost).mockResolvedValue(undefined);
    await propertyHttpGateway.list();
    await propertyHttpGateway.create({});
    expect(apiGet).toHaveBeenCalledWith("/properties");
    expect(apiPost).toHaveBeenCalledWith("/properties", {});
  });
});
