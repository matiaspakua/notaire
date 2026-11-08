// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { RoleGateway } from "./ports";
import { createRoleUseCases } from "./use-cases";

describe("createRoleUseCases", () => {
  it("delegates CRUD and assign/unassign", async () => {
    const calls: unknown[] = [];
    const gw: RoleGateway = {
      list: async () => {
        calls.push(["list"]);
        return [];
      },
      create: async (input) => {
        calls.push(["create", input]);
        return { idRole: 1, ...input };
      },
      update: async (id, input) => {
        calls.push(["update", id, input]);
        return { idRole: id, ...input };
      },
      delete: async (id) => {
        calls.push(["delete", id]);
      },
      assignToUser: async (roleId, userId) => {
        calls.push(["assign", roleId, userId]);
      },
      unassignFromUser: async (userId) => {
        calls.push(["unassign", userId]);
      },
    };
    const uc = createRoleUseCases(gw);
    await uc.list();
    await uc.assignToUser(2, 3);
    await uc.unassignFromUser(3);
    expect(calls).toEqual([["list"], ["assign", 2, 3], ["unassign", 3]]);
  });
});
