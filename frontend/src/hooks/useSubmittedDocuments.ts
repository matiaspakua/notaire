import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type {
  SubmittedDocument,
  SubmittedDocumentRequest,
  SubmittedDocumentUpdateRequest,
} from "@/types/api";

export const documentosPresentadosKeys = {
  all: ["documentosPresentados"] as const,
  detail: (id: number) => ["documentosPresentados", id] as const,
};

export function useSubmittedDocuments() {
  return useQuery({
    queryKey: documentosPresentadosKeys.all,
    queryFn: () => apiGet<SubmittedDocument[]>("/submitted-documents"),
  });
}

export function useCreateDocumentoPresentado() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: SubmittedDocumentRequest) => apiPost<SubmittedDocument>("/submitted-documents", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: documentosPresentadosKeys.all }),
  });
}

export function useUpdateDocumentoPresentado() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: SubmittedDocumentUpdateRequest }) =>
      apiPut<void>(`/submitted-documents/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: documentosPresentadosKeys.all }),
  });
}

export function useDeleteDocumentoPresentado() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/submitted-documents/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: documentosPresentadosKeys.all }),
  });
}
