/**
 * Concept catalog use cases (#1781 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { ConceptGateway, ConceptInput } from "./ports";

export function createConceptUseCases(gateway: ConceptGateway) {
  return {
    list: () => gateway.list(),
    searchByName: (name: string) => gateway.searchByName(name),
    create: (input: ConceptInput) => gateway.create(input),
    update: (id: number, input: ConceptInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    isInUse: (id: number) => gateway.isInUse(id),
  };
}

export type ConceptUseCases = ReturnType<typeof createConceptUseCases>;
