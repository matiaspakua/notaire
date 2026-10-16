// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { TestimonyMovementGateway } from "./ports";
import { createTestimonyMovementUseCases } from "./use-cases";

describe("testimony movement use cases (#1834)", () => {
  it("delegates enter/register/withdraw/reenter with cardNumber", async () => {
    const calls: unknown[] = [];
    const gateway: TestimonyMovementGateway = {
      enterRegistration: async (id) => {
        calls.push(["enter", id]);
        return { idTestimonyMovement: 1 };
      },
      registerInscription: async (id) => {
        calls.push(["register", id]);
        return { idTestimonyMovement: 1 };
      },
      withdraw: async (input) => {
        calls.push(["withdraw", input]);
        return { idTestimonyMovement: 1, cardNumber: input.cardNumber };
      },
      reenter: async (input) => {
        calls.push(["reenter", input]);
        return { idTestimonyMovement: 2, cardNumber: input.cardNumber };
      },
    };
    const uc = createTestimonyMovementUseCases(gateway);
    await uc.enterRegistration(9);
    await uc.registerInscription(9);
    await uc.withdraw({ idTestimony: 9, cardNumber: 12 });
    await uc.reenter({ idTestimony: 9, cardNumber: 12, observedByRegistry: true, notes: "n" });
    expect(calls).toEqual([
      ["enter", 9],
      ["register", 9],
      ["withdraw", { idTestimony: 9, cardNumber: 12 }],
      ["reenter", { idTestimony: 9, cardNumber: 12, observedByRegistry: true, notes: "n" }],
    ]);
  });
});
