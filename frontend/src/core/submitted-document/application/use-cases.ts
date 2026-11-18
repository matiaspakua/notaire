/**
 * Submitted-document use cases (#1841 / ADR-030).
 */
import type { SubmittedDocumentGateway, SubmittedDocumentPageRequest } from "./ports";
import type {
  SubmittedDocumentCreateInput,
  SubmittedDocumentUpdateInput,
} from "../domain/submitted-document";

export function createSubmittedDocumentUseCases(gateway: SubmittedDocumentGateway) {
  return {
    list: () => gateway.list(),
    listPage: (request: SubmittedDocumentPageRequest) => gateway.listPage(request),
    create: (input: SubmittedDocumentCreateInput) => gateway.create(input),
    update: (id: number, input: SubmittedDocumentUpdateInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    listUpcomingExpirations: (days: number) => gateway.listUpcomingExpirations(days),
  };
}

export type SubmittedDocumentUseCases = ReturnType<typeof createSubmittedDocumentUseCases>;
