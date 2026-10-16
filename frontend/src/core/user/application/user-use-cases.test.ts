// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { UserGateway } from "./ports";
import { createUserUseCases } from "./use-cases";

describe("createUserUseCases", () => {
  it("delegates list/create/update/delete", async () => {
    const calls: unknown[] = [];
    const gw: UserGateway = {
      list: async () => {
        calls.push(["list"]);
        return [];
      },
      create: async (input) => {
        calls.push(["create", input]);
        return { idUser: 1, ...input };
      },
      update: async (id, input) => {
        calls.push(["update", id, input]);
      },
      delete: async (id) => {
        calls.push(["delete", id]);
      },
    };
    const uc = createUserUseCases(gw);
    await uc.list();
    await uc.create({ name: "a" });
    await uc.update(1, { active: false });
    await uc.delete(1);
    expect(calls).toEqual([
      ["list"],
      ["create", { name: "a" }],
      ["update", 1, { active: false }],
      ["delete", 1],
    ]);
  });
});
