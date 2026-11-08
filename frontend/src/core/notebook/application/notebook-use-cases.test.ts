// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { Notebook, NotebookCreateInput } from "../domain/notebook";
import type { NotebookGateway } from "./ports";
import { createNotebookUseCases } from "./use-cases";

function fakeGateway(initial: Notebook[] = []): NotebookGateway & {
  store: Notebook[];
  lastCreate?: NotebookCreateInput;
} {
  const store = [...initial];
  const gateway: NotebookGateway & { store: Notebook[]; lastCreate?: NotebookCreateInput } = {
    store,
    list: async () => [...store],
    create: async (input) => {
      gateway.lastCreate = input;
      store.push({ idNotebook: store.length + 1, year: input.year, notes: input.notes ?? undefined });
    },
    downloadCover: async () => new Blob(["pdf"], { type: "application/pdf" }),
  };
  return gateway;
}

describe("notebook use cases (#1833)", () => {
  it("lists, creates with idsFolio/idNotary, and downloads cover", async () => {
    const gateway = fakeGateway([{ idNotebook: 1, number: 1, year: 2026 }]);
    const uc = createNotebookUseCases(gateway);
    await expect(uc.list()).resolves.toHaveLength(1);
    await uc.create({ idsFolio: [10, 11], idNotary: 5, year: 2026, notes: "n" });
    expect(gateway.lastCreate).toEqual({ idsFolio: [10, 11], idNotary: 5, year: 2026, notes: "n" });
    await expect(uc.downloadCover(1)).resolves.toBeInstanceOf(Blob);
  });
});
