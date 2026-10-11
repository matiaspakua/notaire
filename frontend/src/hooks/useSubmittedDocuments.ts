import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type {
  SubmittedDocument,
  SubmittedDocumentRequest,
  SubmittedDocumentUpdateRequest,
} from "@/types/api";

export const submittedDocumentsKeys = {
  all: ["submittedDocuments"] as const,
  detail: (id: number) => ["submittedDocuments", id] as const,
};

export function useSubmittedDocuments() {
  return useQuery({
    queryKey: submittedDocumentsKeys.all,
    queryFn: () => apiGet<SubmittedDocument[]>("/submitted-documents"),
  });
}

export function useCreateSubmittedDocument() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: SubmittedDocumentRequest) => apiPost<SubmittedDocument>("/submitted-documents", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: submittedDocumentsKeys.all }),
  });
}

export function useUpdateSubmittedDocument() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: SubmittedDocumentUpdateRequest }) =>
      apiPut<void>(`/submitted-documents/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: submittedDocumentsKeys.all }),
  });
}

export function useDeleteSubmittedDocument() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/submitted-documents/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: submittedDocumentsKeys.all }),
  });
}
