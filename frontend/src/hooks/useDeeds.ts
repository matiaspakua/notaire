/**
 * React Query hooks for deeds.
 * Hexagon wiring: #1831 / ADR-030.
 */
import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { deedUseCases } from "@/composition/deed";
import type { Deed as DomainDeed } from "@/core/deed/domain/deed";
import type { Deed } from "@/types";

export const deedsKeys = {
  all: ["deeds"] as const,
  detail: (id: number) => ["deeds", id] as const,
  page: (params: { page: number; size: number }) => ["deeds", "page", params] as const,
};

/** Sort for the deeds list: newest first (#1340). */
export const DEEDS_SORT = "idDeed,desc";

function toWire(d: DomainDeed): Deed {
  return {
    idDeed: d.idDeed,
    number: d.number,
    dateDeedrecording: d.dateDeedrecording,
    body: d.body,
    notes: d.notes,
    registrationEntryNumber: d.registrationEntryNumber,
    status: d.status,
    idFolio: d.idFolio,
  };
}

/** One server page of GET /deeds (#1340); the list is never loaded whole. */
export function useDeedsPage(params: { page: number; size: number }, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: deedsKeys.page(params),
    queryFn: async () => {
      const page = await deedUseCases.listPage({
        page: params.page,
        size: params.size,
        sort: DEEDS_SORT,
      });
      return {
        ...page,
        content: page.content.map(toWire),
      };
    },
    placeholderData: keepPreviousData,
    enabled: options.enabled ?? true,
  });
}

export function useCreateDeed() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<Deed>) => deedUseCases.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: deedsKeys.all }),
  });
}

export function useUpdateDeed() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Deed> }) => deedUseCases.update(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: deedsKeys.all }),
  });
}

export function useDeleteDeed() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => deedUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: deedsKeys.all }),
  });
}

/** CU06 - Moves a deed from "Sin Firmar" to "Firmada" when a folio is assigned. */
export function useSignDeed() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => toWire(await deedUseCases.sign(id)),
    onSuccess: () => qc.invalidateQueries({ queryKey: deedsKeys.all }),
  });
}
