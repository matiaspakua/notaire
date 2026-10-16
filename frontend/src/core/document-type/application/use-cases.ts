/**
 * Document-type catalog use cases (#1777 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { DocumentTypeGateway, DocumentTypeInput } from "./ports";

export function createDocumentTypeUseCases(gateway: DocumentTypeGateway) {
  return {
    list: () => gateway.list(),
    searchByName: (name: string) => gateway.searchByName(name),
    create: (input: DocumentTypeInput) => gateway.create(input),
    update: (id: number, input: DocumentTypeInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    isInUse: (id: number) => gateway.isInUse(id),
  };
}

export type DocumentTypeUseCases = ReturnType<typeof createDocumentTypeUseCases>;
