/**
 * Deed list/search/mutation use cases (#1831 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { DeedGateway, DeedInput, DeedPageRequest } from "./ports";

export function createDeedUseCases(gateway: DeedGateway) {
  return {
    getById: (id: number) => gateway.getById(id),
    listPage: (request: DeedPageRequest) => gateway.listPage(request),
    searchByNumber: (number: string) => gateway.searchByNumber(number),
    create: (input: DeedInput) => gateway.create(input),
    update: (id: number, input: DeedInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    sign: (id: number) => gateway.sign(id),
  };
}

export type DeedUseCases = ReturnType<typeof createDeedUseCases>;
