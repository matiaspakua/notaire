import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Inmueble } from "@/types";

export const inmueblesKeys = {
  all: ["inmuebles"] as const,
  detail: (id: number) => ["inmuebles", id] as const,
};

export function useInmuebles() {
  return useQuery({
    queryKey: inmueblesKeys.all,
    queryFn: () => apiGet<Inmueble[]>("/inmueble"),
  });
}

export function useCreateInmueble() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Inmueble>) => apiPost<void>("/inmueble", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: inmueblesKeys.all }),
  });
}

export function useUpdateInmueble() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Inmueble> }) =>
      apiPut<void>(`/inmueble/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: inmueblesKeys.all }),
  });
}

export function useDeleteInmueble() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/inmueble/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: inmueblesKeys.all }),
  });
}
