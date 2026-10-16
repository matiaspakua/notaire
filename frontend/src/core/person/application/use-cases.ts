/**
 * Person list/search use cases (#1823 / ADR-030).
 * No React, Next, or api-client — only the gateway port.
 */
import type { PersonGateway, PersonInput, PersonPageRequest, PersonSearchCriteria } from "./ports";

export function createPersonUseCases(gateway: PersonGateway) {
  return {
    getById: (id: number) => gateway.getById(id),
    listPage: (request: PersonPageRequest) => gateway.listPage(request),
    search: (criteria: PersonSearchCriteria) => gateway.search(criteria),
    create: (input: PersonInput) => gateway.create(input),
    update: (id: number, input: PersonInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
  };
}

export type PersonUseCases = ReturnType<typeof createPersonUseCases>;
