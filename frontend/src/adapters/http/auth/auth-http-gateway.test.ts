// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiPost: vi.fn(),
  apiLogout: vi.fn(),
}));

import { apiPost, apiLogout } from "@/lib/api-client";
import { authHttpGateway } from "./auth-http-gateway";

describe("authHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("posts login and calls logout", async () => {
    vi.mocked(apiPost).mockResolvedValue({ name: "a", valid: true });
    vi.mocked(apiLogout).mockResolvedValue(undefined);
    await authHttpGateway.login({ name: "a", password: "b" });
    await authHttpGateway.logout();
    expect(apiPost).toHaveBeenCalledWith("/users/login", { name: "a", password: "b" });
    expect(apiLogout).toHaveBeenCalled();
  });
});
