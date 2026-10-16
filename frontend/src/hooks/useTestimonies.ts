/**
 * React Query hooks for testimonies.
 * Hexagon wiring: #1834 / ADR-030.
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { testimonyUseCases } from "@/composition/testimony";
import type {
  Testimony as DomainTestimony,
  TestimonyCreateInput,
  TestimonyUpdateInput,
} from "@/core/testimony/domain/testimony";
import type { Testimony } from "@/types";

export const testimoniesKeys = {
  all: ["testimonies"] as const,
};

/** Body of the bare POST /testimonies (#1555): the deed is required, the rest as typed. */
export type TestimonyCreate = TestimonyCreateInput;

/** Body of PUT /testimonies/{id}: number, flagged, verified and version are required (#655). */
export type TestimonyUpdate = TestimonyUpdateInput;

function toWire(t: DomainTestimony): Testimony {
  return {
    idTestimony: t.idTestimony,
    number: t.number,
    flagged: t.flagged,
    verified: t.verified,
    notes: t.notes,
    version: t.version,
    deed: t.deed as Testimony["deed"],
    testimonyMovements: t.testimonyMovements as Testimony["testimonyMovements"],
  };
}

export function useTestimonies() {
  return useQuery({
    queryKey: testimoniesKeys.all,
    queryFn: async () => (await testimonyUseCases.list()).map(toWire),
  });
}

/** CU07 - Creates a numbered testimony from a deed in status Firmada. */
export function useGenerateTestimony() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (idDeed: number) => toWire(await testimonyUseCases.generate(idDeed)),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** CU07 (#1555) - Registers a testimony manually with chosen values (e.g. a historic one). */
export function useCreateTestimony() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (body: TestimonyCreate) => toWire(await testimonyUseCases.create(body)),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** CU08 - Records testimony verification (flagged or not, with a reason). */
export function useVerifyTestimony() {
  const qc = useQueryClient();
  return useMutation({
    // Backend body is DtoTestimony(flagged, notes).
    mutationFn: async ({ id, flagged, notes }: { id: number; flagged: boolean; notes?: string }) =>
      toWire(await testimonyUseCases.verify(id, { flagged, notes })),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** Downloads the printed copy (PDF) of a verified testimony. */
export async function downloadTestimonyCopy(idTestimony: number): Promise<void> {
  const blob = await testimonyUseCases.downloadCopy(idTestimony);
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `testimonio_${idTestimony}_copia.pdf`;
  link.click();
  URL.revokeObjectURL(link.href);
}

/** #1364 - Corrects a mistaken testimony (number, notes); a stale version answers 409. */
export function useUpdateTestimony() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: TestimonyUpdate }) =>
      testimonyUseCases.update(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** #1364 - Deletes a testimony; one with registry movements or copies answers 409. */
export function useDeleteTestimony() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => testimonyUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}
