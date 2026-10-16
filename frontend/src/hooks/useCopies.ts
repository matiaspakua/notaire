/**
 * React Query hooks for copies.
 * Hexagon wiring: #1845 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { copyUseCases } from "@/composition/copy";
import type { Copy as DomainCopy, CopyInput } from "@/core/copy/domain/copy";
import type { Copy } from "@/types";

export const copiesKeys = {
  all: ["copies"] as const,
  detail: (id: number) => ["copies", id] as const,
};

function toWire(c: DomainCopy): Copy {
  return c as Copy;
}

export function useCopies() {
  return useQuery({
    queryKey: copiesKeys.all,
    queryFn: async () => (await copyUseCases.list()).map(toWire),
  });
}

export function useCreateCopy() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Copy>) => copyUseCases.create(data as CopyInput),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiesKeys.all }),
  });
}

export function useUpdateCopy() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Copy> }) =>
      copyUseCases.update(id, data as CopyInput),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiesKeys.all }),
  });
}

export function useDeleteCopy() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => copyUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: copiesKeys.all }),
  });
}
