/**
 * Folio instance admin use cases (#1785 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { FolioGateway } from "./ports";
import type { FolioWriteInput } from "../domain/folio";

export function createFolioUseCases(gateway: FolioGateway) {
  return {
    list: () => gateway.list(),
    searchByStatus: (status: string) => gateway.searchByStatus(status),
    create: (input: FolioWriteInput) => gateway.create(input),
    update: (id: number, input: FolioWriteInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    listAvailableNotaries: () => gateway.listAvailableNotaries(),
  };
}

export type FolioUseCases = ReturnType<typeof createFolioUseCases>;
