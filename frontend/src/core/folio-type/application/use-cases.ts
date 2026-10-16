/**
 * Folio-type catalog use cases (#1785 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { FolioTypeGateway, FolioTypeInput } from "./ports";

export function createFolioTypeUseCases(gateway: FolioTypeGateway) {
  return {
    list: () => gateway.list(),
    searchByName: (name: string) => gateway.searchByName(name),
    create: (input: FolioTypeInput) => gateway.create(input),
    update: (id: number, input: FolioTypeInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    isInUse: (id: number) => gateway.isInUse(id),
  };
}

export type FolioTypeUseCases = ReturnType<typeof createFolioTypeUseCases>;
