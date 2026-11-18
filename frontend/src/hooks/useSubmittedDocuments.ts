/**
 * React Query hooks for submitted documents.
 * Hexagon wiring: #1841 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient, keepPreviousData } from "@tanstack/react-query";
import { submittedDocumentUseCases } from "@/composition/submitted-document";
import type {
  SubmittedDocument as DomainSubmittedDocument,
  SubmittedDocumentCreateInput,
  SubmittedDocumentUpdateInput,
} from "@/core/submitted-document/domain/submitted-document";
import type {
  SubmittedDocument,
  SubmittedDocumentRequest,
  SubmittedDocumentUpdateRequest,
} from "@/types/api";

export const submittedDocumentsKeys = {
  all: ["submittedDocuments"] as const,
  detail: (id: number) => ["submittedDocuments", id] as const,
};

function toWire(d: DomainSubmittedDocument): SubmittedDocument {
  return d as SubmittedDocument;
}

export const SUBMITTED_DOCUMENTS_SORT = "idSubmittedDocument,desc";

export function useSubmittedDocumentsPage(params: { page: number; size: number }) {
  return useQuery({
    queryKey: [...submittedDocumentsKeys.all, "page", params.page, params.size],
    queryFn: async () => {
      const page = await submittedDocumentUseCases.listPage({
        page: params.page,
        size: params.size,
        sort: SUBMITTED_DOCUMENTS_SORT,
      });
      return { ...page, content: page.content.map(toWire) };
    },
    placeholderData: keepPreviousData,
  });
}

export function useCreateSubmittedDocument() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (data: SubmittedDocumentRequest) =>
      toWire(await submittedDocumentUseCases.create(data as SubmittedDocumentCreateInput)),
    onSuccess: () => qc.invalidateQueries({ queryKey: submittedDocumentsKeys.all }),
  });
}

export function useUpdateSubmittedDocument() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: SubmittedDocumentUpdateRequest }) =>
      submittedDocumentUseCases.update(id, data as SubmittedDocumentUpdateInput),
    onSuccess: () => qc.invalidateQueries({ queryKey: submittedDocumentsKeys.all }),
  });
}

export function useDeleteSubmittedDocument() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => submittedDocumentUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: submittedDocumentsKeys.all }),
  });
}
