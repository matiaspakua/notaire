// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { AuditGateway } from "./ports";
import { createAuditUseCases } from "./use-cases";

describe("createAuditUseCases", () => {
  it("delegates listPage", async () => {
    const gw: AuditGateway = {
      listPage: async (request) => ({
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: request.page,
        size: request.size,
      }),
    };
    const page = await createAuditUseCases(gw).listPage({ page: 0, size: 20, module: "People" });
    expect(page.number).toBe(0);
    expect(page.size).toBe(20);
  });
});
