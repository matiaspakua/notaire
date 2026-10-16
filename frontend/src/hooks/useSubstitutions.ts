/**
 * React Query hooks for notary substitutions (CU12, CU42, CU59).
 * Hexagon wiring: #1845 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { substitutionUseCases } from "@/composition/substitution";
import type {
  Substitution as DomainSubstitution,
  SubstitutionInput,
} from "@/core/substitution/domain/substitution";
import type { Substitution } from "@/types";

export const substitutionsKeys = {
  all: ["substitutions"] as const,
  detail: (id: number) => ["substitutions", id] as const,
};

function toWire(s: DomainSubstitution): Substitution {
  return s as Substitution;
}

export function useSubstitutions() {
  return useQuery({
    queryKey: substitutionsKeys.all,
    queryFn: async () => (await substitutionUseCases.list()).map(toWire),
  });
}

export function useCreateSubstitution() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Substitution>) =>
      substitutionUseCases.create(data as SubstitutionInput),
    onSuccess: () => qc.invalidateQueries({ queryKey: substitutionsKeys.all }),
  });
}

export function useUpdateSubstitution() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Substitution> }) =>
      substitutionUseCases.update(id, data as SubstitutionInput),
    onSuccess: () => qc.invalidateQueries({ queryKey: substitutionsKeys.all }),
  });
}

export function useDeleteSubstitution() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => substitutionUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: substitutionsKeys.all }),
  });
}
