// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { ProcedureTemplate } from "../domain/procedure-template";
import type { ProcedureTemplateGateway } from "./ports";
import { createProcedureTemplateUseCases } from "./use-cases";

function fake(seed: ProcedureTemplate[] = []): ProcedureTemplateGateway {
  const store = [...seed];
  return {
    listAll: async () => [...store],
    listByProcedureType: async (id) => store.filter((r) => r.procedureTypeId === id),
    create: async (input) => {
      const row = { ...input, notes: input.notes ?? "" };
      store.push(row);
      return row;
    },
    update: async (input) => {
      const idx = store.findIndex(
        (r) => r.procedureTypeId === input.procedureTypeId && r.documentTypeId === input.documentTypeId,
      );
      store[idx] = { ...store[idx], notes: input.notes };
      return store[idx];
    },
    delete: async (procedureTypeId, documentTypeId) => {
      const idx = store.findIndex(
        (r) => r.procedureTypeId === procedureTypeId && r.documentTypeId === documentTypeId,
      );
      store.splice(idx, 1);
    },
  };
}

describe("procedure-template use cases (#1788)", () => {
  it("lists by procedure and mutates", async () => {
    const gateway = fake([{ procedureTypeId: 7, documentTypeId: 1, notes: "" }]);
    const uc = createProcedureTemplateUseCases(gateway);
    await expect(uc.listByProcedureType(7)).resolves.toHaveLength(1);
    await uc.create({ procedureTypeId: 7, documentTypeId: 2, notes: "x" });
    await expect(uc.listAll()).resolves.toHaveLength(2);
    await uc.delete(7, 2);
    await expect(uc.listAll()).resolves.toHaveLength(1);
  });
});
