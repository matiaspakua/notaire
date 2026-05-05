import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { DocumentoPresentado } from "@/types";

export const documentosPresentadosKeys = {
  all: ["documentosPresentados"] as const,
  detail: (id: number) => ["documentosPresentados", id] as const,
};

export function useDocumentosPresentados() {
  return useQuery({
    queryKey: documentosPresentadosKeys.all,
    queryFn: () => apiGet<DocumentoPresentado[]>("/documento-presentado"),
  });
}

export function useCreateDocumentoPresentado() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<DocumentoPresentado>) => apiPost<void>("/documento-presentado", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: documentosPresentadosKeys.all }),
  });
}

export function useUpdateDocumentoPresentado() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<DocumentoPresentado> }) =>
      apiPut<void>(`/documento-presentado/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: documentosPresentadosKeys.all }),
  });
}

export function useDeleteDocumentoPresentado() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/documento-presentado/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: documentosPresentadosKeys.all }),
  });
}
