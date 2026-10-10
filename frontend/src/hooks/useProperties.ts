import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Property } from "@/types";

export const inmueblesKeys = {
  all: ["inmuebles"] as const,
  detail: (id: number) => ["inmuebles", id] as const,
};

export function useProperties() {
  return useQuery({
    queryKey: inmueblesKeys.all,
    queryFn: () => apiGet<Property[]>("/properties"),
  });
}

export function useCreateInmueble() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Property>) => apiPost<void>("/properties", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: inmueblesKeys.all }),
  });
}

export function useUpdateInmueble() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Property> }) =>
      apiPut<void>(`/properties/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: inmueblesKeys.all }),
  });
}

export function useDeleteInmueble() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/properties/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: inmueblesKeys.all }),
  });
}
