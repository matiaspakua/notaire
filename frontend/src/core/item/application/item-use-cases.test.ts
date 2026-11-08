// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { ItemGateway } from "./ports";
import { createItemUseCases } from "./use-cases";

describe("createItemUseCases", () => {
  it("delegates list and budget variants", async () => {
    const calls: unknown[] = [];
    const gw: ItemGateway = {
      list: async () => { calls.push(["list"]); return []; },
      listByBudget: async (id) => { calls.push(["byBudget", id]); return []; },
      listDiscountsAndSurcharges: async (id) => { calls.push(["ds", id]); return []; },
      create: async (input) => { calls.push(["create", input]); },
      update: async (id, input) => { calls.push(["update", id, input]); },
      delete: async (id) => { calls.push(["delete", id]); },
    };
    const uc = createItemUseCases(gw);
    await uc.listByBudget(3);
    await uc.listDiscountsAndSurcharges(3);
    expect(calls).toEqual([["byBudget", 3], ["ds", 3]]);
  });
});
