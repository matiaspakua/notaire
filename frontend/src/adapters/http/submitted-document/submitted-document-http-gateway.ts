/**
 * HTTP adapter for submitted documents (#1841 / ADR-030).
 */
import { apiDelete, apiGet, apiGetPage, apiPost, apiPut } from "@/lib/api-client";
import type {
  SubmittedDocument,
  SubmittedDocumentCreateInput,
  SubmittedDocumentUpdateInput,
  UpcomingExpiration,
} from "@/core/submitted-document/domain/submitted-document";
import type {
  SubmittedDocumentGateway,
  SubmittedDocumentPage,
  SubmittedDocumentPageRequest,
} from "@/core/submitted-document/application/ports";

export const submittedDocumentHttpGateway: SubmittedDocumentGateway = {
  list: () => apiGet<SubmittedDocument[]>("/submitted-documents"),
  listPage: async (request: SubmittedDocumentPageRequest): Promise<SubmittedDocumentPage> => {
    const page = await apiGetPage<SubmittedDocument>("/submitted-documents", {
      page: request.page,
      size: request.size,
      sort: request.sort,
    });
    return {
      content: page.content,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      number: page.number,
      size: page.size,
    };
  },
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
