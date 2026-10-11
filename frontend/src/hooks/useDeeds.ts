import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGetPage, apiGetPaged, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Deed } from "@/types";

export const deedsKeys = {
  all: ["deeds"] as const,
  detail: (id: number) => ["deeds", id] as const,
  page: (params: { page: number; size: number }) => ["deeds", "page", params] as const,
};

/** Sort for the deeds list: newest first (#1340). */
export const DEEDS_SORT = "idDeed,desc";

/** One server page of GET /deeds (#1340); the list is never loaded whole. */
export function useDeedsPage(params: { page: number; size: number }, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: deedsKeys.page(params),
    queryFn: () =>
      apiGetPage<Deed>("/deeds", { page: params.page, size: params.size, sort: DEEDS_SORT }),
    placeholderData: keepPreviousData,
    enabled: options.enabled ?? true,
  });
}

export function useDeeds() {
  return useQuery({
    queryKey: deedsKeys.all,
    queryFn: () => apiGetPaged<Deed>("/deeds"),
  });
}

export function useCreateDeed() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Deed>) => apiPost<void>("/deeds", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: deedsKeys.all }),
  });
}

export function useUpdateDeed() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Deed> }) =>
      apiPut<void>(`/deeds/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: deedsKeys.all }),
  });
}

export function useDeleteDeed() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/deeds/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: deedsKeys.all }),
  });
}

/** CU06 - Moves a deed from "Sin Firmar" to "Firmada" when a folio is assigned. */
export function useSignDeed() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiPost<Deed>(`/deeds/${id}/sign`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: deedsKeys.all }),
  });
}
