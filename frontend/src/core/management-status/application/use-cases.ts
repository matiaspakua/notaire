/**
 * Management-status catalog use cases (#1769 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { ManagementStatusGateway, ManagementStatusInput } from "./ports";

export function createManagementStatusUseCases(gateway: ManagementStatusGateway) {
  return {
    list: () => gateway.list(),
    searchByName: (name: string) => gateway.searchByName(name),
    create: (input: ManagementStatusInput) => gateway.create(input),
    update: (id: number, input: ManagementStatusInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    isInUse: (id: number) => gateway.isInUse(id),
  };
}

export type ManagementStatusUseCases = ReturnType<typeof createManagementStatusUseCases>;
