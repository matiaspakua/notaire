/**
 * React Query hooks for concepts.
 * Hexagon wiring: #1781 / ADR-030.
 * Endpoint: GET/POST/PUT/DELETE /api/v1/concepts
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { conceptUseCases } from "@/composition/concept";
import type { Concept as DomainConcept } from "@/core/concept/domain/concept";
import type { Concept } from "@/types";

export const conceptsKeys = { all: ["concepts"] as const };

function toWire(c: DomainConcept): Concept {
  return {
    idConcept: c.id,
    name: c.name,
    value: c.value,
    percentage: c.percentage,
    fixed: c.fixed,
    enabled: c.enabled,
    version: c.version,
  };
}

function toInput(data: Partial<Concept>) {
  return {
    name: data.name?.trim() ?? "",
    value: data.value,
    percentage: data.percentage,
    fixed: data.fixed,
    enabled: data.enabled,
    version: data.version,
  };
}

export function useConcepts() {
  return useQuery({
    queryKey: conceptsKeys.all,
    queryFn: async () => (await conceptUseCases.list()).map(toWire),
  });
}

export function useCreateConcept() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Concept>) => conceptUseCases.create(toInput(data)),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptsKeys.all }),
  });
}

export function useUpdateConcept() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Concept> }) =>
      conceptUseCases.update(id, toInput(data)),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptsKeys.all }),
  });
}

export function useDeleteConcept() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => conceptUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: conceptsKeys.all }),
  });
}
