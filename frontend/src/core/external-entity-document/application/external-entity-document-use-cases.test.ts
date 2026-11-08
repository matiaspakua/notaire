// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { ExternalEntityDocumentGateway } from "./ports";
import { createExternalEntityDocumentUseCases } from "./use-cases";

describe("createExternalEntityDocumentUseCases", () => {
  it("delegates get and registerMovement", async () => {
    const calls: unknown[] = [];
    const gw: ExternalEntityDocumentGateway = {
      getByManagement: async (id) => {
        calls.push(["getByManagement", id]);
        return { idManagement: id, documents: [] };
      },
      registerMovement: async (managementId, idSubmittedDocument, movement) => {
        calls.push(["registerMovement", managementId, idSubmittedDocument, movement]);
        return { idSubmittedDocument, ...movement };
      },
    };
    const uc = createExternalEntityDocumentUseCases(gw);
    await uc.getByManagement(9);
    await uc.registerMovement(9, 1, { delivered: true });
    expect(calls).toEqual([
      ["getByManagement", 9],
      ["registerMovement", 9, 1, { delivered: true }],
    ]);
  });
});
