/**
 * Management use cases (#1839 / ADR-030).
 */
import type { ManagementGateway } from "./ports";
import type { CreateCompleteManagementInput, ManagementInput } from "../domain/management";

export function createManagementUseCases(gateway: ManagementGateway) {
  return {
    listPage: (request: Parameters<ManagementGateway["listPage"]>[0]) => gateway.listPage(request),
    getById: (id: number) => gateway.getById(id),
    findByNumber: (number: number) => gateway.findByNumber(number),
    listByClient: (idPerson: number) => gateway.listByClient(idPerson),
    create: (input: ManagementInput) => gateway.create(input),
    createCompleteCase: (input: CreateCompleteManagementInput) => gateway.createCompleteCase(input),
    getCompleteCase: (id: number) => gateway.getCompleteCase(id),
    updateCompleteCase: (id: number, input: CreateCompleteManagementInput) =>
      gateway.updateCompleteCase(id, input),
    delete: (id: number) => gateway.delete(id),
    getOutstandingBalance: (id: number) => gateway.getOutstandingBalance(id),
    archive: (id: number, confirmado = false) => gateway.archive(id, confirmado),
    transition: (id: number, statusDestination: string) => gateway.transition(id, statusDestination),
    getCurrentStatus: (id: number) => gateway.getCurrentStatus(id),
    listHistory: (id: number) => gateway.listHistory(id),
    listFolders: (managementId: number) => gateway.listFolders(managementId),
    putFolderOnHold: (idFolder: number, reason: string) => gateway.putFolderOnHold(idFolder, reason),
    getCaseSummary: (id: number) => gateway.getCaseSummary(id),
    getFinancialSummary: (id: number) => gateway.getFinancialSummary(id),
  };
}

export type ManagementUseCases = ReturnType<typeof createManagementUseCases>;
