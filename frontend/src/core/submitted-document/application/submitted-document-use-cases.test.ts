// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { SubmittedDocumentGateway } from "./ports";
import { createSubmittedDocumentUseCases } from "./use-cases";

describe("createSubmittedDocumentUseCases", () => {
  it("delegates list/create/update/delete/upcoming-expirations", async () => {
    const calls: unknown[] = [];
    const gw: SubmittedDocumentGateway = {
      list: async () => {
        calls.push(["list"]);
        return [];
      },
      create: async (input) => {
        calls.push(["create", input]);
        return { idSubmittedDocument: 1, ...input };
      },
      update: async (id, input) => {
        calls.push(["update", id, input]);
      },
      delete: async (id) => {
        calls.push(["delete", id]);
      },
      listUpcomingExpirations: async (days) => {
        calls.push(["listUpcomingExpirations", days]);
        return [];
      },
    };
    const uc = createSubmittedDocumentUseCases(gw);
    await uc.list();
    await uc.create({ typeId: 3, procedureId: 9, date: "2026-06-01", delivered: true });
    await uc.update(5, { typeId: 3, date: "2026-06-01", delivered: false });
    await uc.delete(5);
    await uc.listUpcomingExpirations(30);
    expect(calls).toEqual([
      ["list"],
      ["create", { typeId: 3, procedureId: 9, date: "2026-06-01", delivered: true }],
      ["update", 5, { typeId: 3, date: "2026-06-01", delivered: false }],
      ["delete", 5],
      ["listUpcomingExpirations", 30],
    ]);
  });
});
