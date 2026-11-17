/**
 * React Query hooks for copies.
 * Hexagon wiring: #1845 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient, keepPreviousData } from "@tanstack/react-query";
import { copyUseCases } from "@/composition/copy";
import type { CopyInput } from "@/core/copy/domain/copy";
import type { Copy } from "@/types";

export const copiesKeys = {
  all: ["copies"] as const,
  detail: (id: number) => ["copies", id] as const,
};

export const COPIES_SORT = "idCopy,desc";

export function useCopiesPage(params: { page: number; size: number }) {
  return useQuery({
    queryKey: [...copiesKeys.all, "page", params.page, params.size],
    queryFn: () =>
      copyUseCases.listPage({
        page: params.page,
        size: params.size,
        sort: COPIES_SORT,
      }),
    placeholderData: keepPreviousData,
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
