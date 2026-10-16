// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";
vi.mock("@/lib/api-client", () => ({ apiGet: vi.fn(), apiPost: vi.fn(), apiPut: vi.fn(), apiDelete: vi.fn() }));
import { apiGet } from "@/lib/api-client";
import { itemHttpGateway } from "./item-http-gateway";

describe("itemHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());
  it("lists by budget and discounts-surcharges", async () => {
    vi.mocked(apiGet).mockResolvedValue([]);
    await itemHttpGateway.listByBudget(3);
    await itemHttpGateway.listDiscountsAndSurcharges(3);
    expect(apiGet).toHaveBeenCalledWith("/items/budget/3");
    expect(apiGet).toHaveBeenCalledWith("/items/budget/3/discounts-surcharges");
  });
});
