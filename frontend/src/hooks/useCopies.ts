import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Copy } from "@/types";

export const copiasKeys = {
  all: ["copias"] as const,
  detail: (id: number) => ["copias", id] as const,
};

export function useCopies() {
  return useQuery({
    queryKey: copiasKeys.all,
    queryFn: () => apiGet<Copy[]>("/copies"),
  });
}

export function useCreateCopia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Copy>) => apiPost<void>("/copies", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiasKeys.all }),
  });
}

export function useUpdateCopia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Copy> }) =>
      apiPut<void>(`/copies/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiasKeys.all }),
  });
}

export function useDeleteCopia() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/copies/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiasKeys.all }),
  });
}
