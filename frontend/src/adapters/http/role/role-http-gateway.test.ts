// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiPut, apiDelete } from "@/lib/api-client";
import { roleHttpGateway } from "./role-http-gateway";

describe("roleHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("assigns and unassigns roles", async () => {
    vi.mocked(apiPut).mockResolvedValue(undefined);
    vi.mocked(apiDelete).mockResolvedValue(undefined);
    await roleHttpGateway.assignToUser(2, 3);
    await roleHttpGateway.unassignFromUser(3);
    expect(apiPut).toHaveBeenCalledWith("/roles/2/users/3", {});
    expect(apiDelete).toHaveBeenCalledWith("/roles/users/3");
  });
});
