import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { Property } from "@/types";

export const propertiesKeys = {
  all: ["properties"] as const,
  detail: (id: number) => ["properties", id] as const,
};

export function useProperties() {
  return useQuery({
    queryKey: propertiesKeys.all,
    queryFn: () => apiGet<Property[]>("/properties"),
  });
}

export function useCreateProperty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Property>) => apiPost<void>("/properties", data),
    onSuccess: () => qc.invalidateQueries({ queryKey: propertiesKeys.all }),
  });
}

export function useUpdateProperty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Property> }) =>
      apiPut<void>(`/properties/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: propertiesKeys.all }),
  });
}

export function useDeleteProperty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/properties/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: propertiesKeys.all }),
  });
}
