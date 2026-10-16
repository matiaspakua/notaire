// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiGet, apiPost } from "@/lib/api-client";
import { userHttpGateway } from "./user-http-gateway";

describe("userHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("lists and creates users", async () => {
    vi.mocked(apiGet).mockResolvedValue([]);
    vi.mocked(apiPost).mockResolvedValue({ idUser: 1, name: "a" });
    await userHttpGateway.list();
    await userHttpGateway.create({ name: "a" });
    expect(apiGet).toHaveBeenCalledWith("/users");
    expect(apiPost).toHaveBeenCalledWith("/users", { name: "a" });
  });
});
