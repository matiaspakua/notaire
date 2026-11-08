// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { AuthGateway } from "./ports";
import { createAuthUseCases } from "./use-cases";

describe("createAuthUseCases", () => {
  it("delegates login and logout", async () => {
    const calls: unknown[] = [];
    const gw: AuthGateway = {
      login: async (input) => {
        calls.push(["login", input]);
        return { name: input.name, valid: true };
      },
      logout: async () => {
        calls.push(["logout"]);
      },
    };
    const uc = createAuthUseCases(gw);
    await uc.login({ name: "a", password: "b" });
    await uc.logout();
    expect(calls).toEqual([
      ["login", { name: "a", password: "b" }],
      ["logout"],
    ]);
  });
});
