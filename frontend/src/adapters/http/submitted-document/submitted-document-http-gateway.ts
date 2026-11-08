/**
 * HTTP adapter for submitted documents (#1841 / ADR-030).
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type {
  SubmittedDocument,
  SubmittedDocumentCreateInput,
  SubmittedDocumentUpdateInput,
  UpcomingExpiration,
} from "@/core/submitted-document/domain/submitted-document";
import type { SubmittedDocumentGateway } from "@/core/submitted-document/application/ports";

export const submittedDocumentHttpGateway: SubmittedDocumentGateway = {
  list: () => apiGet<SubmittedDocument[]>("/submitted-documents"),
  create: (input: SubmittedDocumentCreateInput) =>
    apiPost<SubmittedDocument>("/submitted-documents", input),
  update: async (id: number, input: SubmittedDocumentUpdateInput) => {
    await apiPut<void>(`/submitted-documents/${id}`, input);
  },
  delete: async (id: number) => {
    await apiDelete(`/submitted-documents/${id}`);
  },
  listUpcomingExpirations: (days: number) =>
    apiGet<UpcomingExpiration[]>(`/submitted-documents/upcoming-expirations?days=${days}`),
};
