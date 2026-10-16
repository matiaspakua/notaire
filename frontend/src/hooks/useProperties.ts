/**
 * React Query hooks for properties.
 * Hexagon wiring: #1845 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { propertyUseCases } from "@/composition/property";
import type { Property as DomainProperty, PropertyInput } from "@/core/property/domain/property";
import type { Property } from "@/types";

export const propertiesKeys = {
  all: ["properties"] as const,
  detail: (id: number) => ["properties", id] as const,
};

function toWire(p: DomainProperty): Property {
  return p as Property;
}

export function useProperties() {
  return useQuery({
    queryKey: propertiesKeys.all,
    queryFn: async () => (await propertyUseCases.list()).map(toWire),
  });
}

export function useCreateProperty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Property>) => propertyUseCases.create(data as PropertyInput),
    onSuccess: () => qc.invalidateQueries({ queryKey: propertiesKeys.all }),
  });
}

export function useUpdateProperty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Property> }) =>
      propertyUseCases.update(id, data as PropertyInput),
    onSuccess: () => qc.invalidateQueries({ queryKey: propertiesKeys.all }),
  });
}

export function useDeleteProperty() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => propertyUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: propertiesKeys.all }),
  });
}
