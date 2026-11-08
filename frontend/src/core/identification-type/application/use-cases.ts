/**
 * Identification-type catalog use cases (#1732 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { IdentificationTypeGateway, IdentificationTypeInput } from "./ports";

export function createIdentificationTypeUseCases(gateway: IdentificationTypeGateway) {
  return {
    list: () => gateway.list(),
    create: (input: IdentificationTypeInput) => gateway.create(input),
    update: (id: number, input: IdentificationTypeInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
  };
}

export type IdentificationTypeUseCases = ReturnType<typeof createIdentificationTypeUseCases>;
