import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetPaged, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Escritura } from "@/types";

export const escriturasKeys = {
  all: ["escrituras"] as const,
  detail: (id: number) => ["escrituras", id] as const,
};

export function useEscrituras() {
  return useQuery({
    queryKey: escriturasKeys.all,
    queryFn: () => apiGetPaged<Escritura>("/escrituras"),
  });
}

export function useEscritura(id: number) {
  return useQuery({
    queryKey: escriturasKeys.detail(id),
    queryFn: () => apiGet<Escritura>(`/escrituras/${id}`),
    enabled: id > 0,
  });
}

export function useCreateEscritura() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Escritura>) => apiPost<void>("/escrituras", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: escriturasKeys.all }),
  });
}

export function useUpdateEscritura() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Escritura> }) =>
      apiPut<void>(`/escrituras/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: escriturasKeys.all }),
  });
}

export function useDeleteEscritura() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/escrituras/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: escriturasKeys.all }),
  });
}
