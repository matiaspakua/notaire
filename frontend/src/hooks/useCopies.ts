import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Copy } from "@/types";

export const copiesKeys = {
  all: ["copies"] as const,
  detail: (id: number) => ["copies", id] as const,
};

export function useCopies() {
  return useQuery({
    queryKey: copiesKeys.all,
    queryFn: () => apiGet<Copy[]>("/copies"),
  });
}

export function useCreateCopy() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Copy>) => apiPost<void>("/copies", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiesKeys.all }),
  });
}

export function useUpdateCopy() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Copy> }) =>
      apiPut<void>(`/copies/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiesKeys.all }),
  });
}

export function useDeleteCopy() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/copies/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiesKeys.all }),
  });
}
