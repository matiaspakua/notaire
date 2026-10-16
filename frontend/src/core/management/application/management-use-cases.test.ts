// @vitest-environment node
/**
 * Core use cases for managements (#1839 / ADR-030).
 */
import { describe, expect, it } from "vitest";
import type { ManagementGateway } from "./ports";
import type { CreateCompleteManagementInput, Management } from "../domain/management";
import { createManagementUseCases } from "./use-cases";

function fakeGateway(): ManagementGateway & {
  calls: unknown[];
} {
  const calls: unknown[] = [];
  const management: Management = { idManagement: 1, number: 10 };
  return {
    calls,
    listPage: async (request) => {
      calls.push(["listPage", request]);
      return { content: [management], totalElements: 1, totalPages: 1, number: 0, size: 20 };
    },
    getById: async (id) => {
      calls.push(["getById", id]);
      return { ...management, idManagement: id };
    },
    findByNumber: async (number) => {
      calls.push(["findByNumber", number]);
      return number === 10 ? management : null;
    },
    listByClient: async (idPerson) => {
      calls.push(["listByClient", idPerson]);
      return [management];
    },
    create: async (input) => {
      calls.push(["create", input]);
    },
    createCompleteCase: async (input: CreateCompleteManagementInput) => {
      calls.push(["createCompleteCase", input]);
      return management;
    },
    getCompleteCase: async (id) => {
      calls.push(["getCompleteCase", id]);
      return { idManagement: id, number: 10, budgetId: 1, notaryId: 2, statusManagementId: 3, typeProcedureId: 4 };
    },
    updateCompleteCase: async (id, input) => {
      calls.push(["updateCompleteCase", id, input]);
      return management;
    },
    delete: async (id) => {
      calls.push(["delete", id]);
    },
    getOutstandingBalance: async (id) => {
      calls.push(["getOutstandingBalance", id]);
      return { pendingBalance: 0 };
    },
    archive: async (id, confirmado) => {
      calls.push(["archive", id, confirmado]);
      return { idManagement: id, pendingBalance: 0, pendingDebtAtArchiving: false };
    },
    transition: async (id, statusDestination) => {
      calls.push(["transition", id, statusDestination]);
      return management;
    },
    getCurrentStatus: async (id) => {
      calls.push(["getCurrentStatus", id]);
      return { managementId: id, statusManagementName: "Open" };
    },
    listHistory: async (id) => {
      calls.push(["listHistory", id]);
      return [];
    },
    listFolders: async (managementId) => {
      calls.push(["listFolders", managementId]);
      return [];
    },
    putFolderOnHold: async (idFolder, reason) => {
      calls.push(["putFolderOnHold", idFolder, reason]);
      return { idFolder, waitReason: reason };
    },
    getCaseSummary: async (id) => {
      calls.push(["getCaseSummary", id]);
      return { managementId: id, managementNumber: 10, deeds: [], documents: [] };
    },
    getFinancialSummary: async (id) => {
      calls.push(["getFinancialSummary", id]);
      return { idManagement: id, totalBudgeted: 0, totalCollected: 0, pendingBalance: 0 };
    },
  };
}

describe("createManagementUseCases", () => {
  it("delegates list/create/complete-case/archive/transition/summaries", async () => {
    const gw = fakeGateway();
    const uc = createManagementUseCases(gw);
    const input: CreateCompleteManagementInput = {
      number: 10,
      budgetId: 1,
      notaryId: 2,
      statusManagementId: 3,
      procedureTypeId: 4,
    };

    await uc.listPage({ page: 0, size: 20, sort: "idManagement,desc" });
    await uc.createCompleteCase(input);
    await uc.archive(1, true);
    await uc.transition(1, "CLOSED");
    await uc.getCaseSummary(1);
    await uc.getFinancialSummary(1);
    await uc.findByNumber(10);
    await uc.putFolderOnHold(9, "waiting");

    expect(gw.calls).toEqual([
      ["listPage", { page: 0, size: 20, sort: "idManagement,desc" }],
      ["createCompleteCase", input],
      ["archive", 1, true],
      ["transition", 1, "CLOSED"],
      ["getCaseSummary", 1],
      ["getFinancialSummary", 1],
      ["findByNumber", 10],
      ["putFolderOnHold", 9, "waiting"],
    ]);
  });
});
