import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGetPage, apiGetPaged, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Escritura } from "@/types";

export const escriturasKeys = {
  all: ["escrituras"] as const,
  detail: (id: number) => ["escrituras", id] as const,
  page: (params: { page: number; size: number }) => ["escrituras", "page", params] as const,
};

/** Sort for the deeds list: newest first (#1340). */
export const ESCRITURAS_SORT = "idDeed,desc";

/** One server page of GET /escrituras (#1340); the list is never loaded whole. */
export function useEscriturasPage(params: { page: number; size: number }, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: escriturasKeys.page(params),
    queryFn: () =>
      apiGetPage<Escritura>("/escrituras", { page: params.page, size: params.size, sort: ESCRITURAS_SORT }),
    placeholderData: keepPreviousData,
    enabled: options.enabled ?? true,
  });
}

export function useEscrituras() {
  return useQuery({
    queryKey: escriturasKeys.all,
    queryFn: () => apiGetPaged<Escritura>("/escrituras"),
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

/** CU06 - Transiciona una escritura "Sin Firmar" con folio asignado al estado "Firmada". */
export function useFirmarEscritura() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiPost<Escritura>(`/escrituras/${id}/firmar`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: escriturasKeys.all }),
  });
}
