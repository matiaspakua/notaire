// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { DocumentReentryGateway } from "./ports";
import { createDocumentReentryUseCases } from "./use-cases";

describe("createDocumentReentryUseCases", () => {
  it("delegates get and reenter", async () => {
    const calls: unknown[] = [];
    const gw: DocumentReentryGateway = {
      getByManagement: async (id) => {
        calls.push(["getByManagement", id]);
        return { idManagement: id, procedures: [] };
      },
      reenter: async (id, input) => {
        calls.push(["reenter", id, input]);
        return {
          idSubmittedDocument: 1,
          idProcedure: input.idProcedure,
          idDocumentType: input.idDocumentType,
          expires: false,
          reentered: true,
        };
      },
    };
    const uc = createDocumentReentryUseCases(gw);
    await uc.getByManagement(9);
    await uc.reenter(9, { idProcedure: 1, idDocumentType: 5 });
    expect(calls).toEqual([
      ["getByManagement", 9],
      ["reenter", 9, { idProcedure: 1, idDocumentType: 5 }],
    ]);
  });
});
