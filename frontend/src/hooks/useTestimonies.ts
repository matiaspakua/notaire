import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiDelete, apiGet, apiPost, apiPut, apiGetBytes } from "@/lib/api-client";
import type { Testimony } from "@/types";

export const testimoniesKeys = {
  all: ["testimonies"] as const,
};

export function useTestimonies() {
  return useQuery({
    queryKey: testimoniesKeys.all,
    queryFn: () => apiGet<Testimony[]>("/testimonies"),
  });
}

/** CU07 - Creates a numbered testimony from a deed in status Firmada. */
export function useGenerateTestimony() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idDeed: number) => apiPost<Testimony>(`/testimonies/${idDeed}/generate`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** CU08 - Records testimony verification (flagged or not, with a reason). */
export function useVerifyTestimony() {
  const qc = useQueryClient();
  return useMutation({
    // Backend body is DtoTestimony(flagged, notes).
    mutationFn: ({ id, flagged, notes }: { id: number; flagged: boolean; notes?: string }) =>
      apiPost<Testimony>(`/testimonies/${id}/verify`, { flagged, notes }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** Downloads the printed copy (PDF) of a verified testimony. */
export async function downloadTestimonyCopy(idTestimony: number): Promise<void> {
  const blob = await apiGetBytes(`/reports/testimonies/${idTestimony}/copies`);
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `testimonio_${idTestimony}_copia.pdf`;
  link.click();
  URL.revokeObjectURL(link.href);
}

/** Body of PUT /testimonies/{id}: number, flagged, verified and version are required (#655). */
export interface TestimonyUpdate {
  number: number;
  notes: string;
  flagged: boolean;
  verified: boolean;
  version: number;
}

/** #1364 - Corrects a mistaken testimony (number, notes); a stale version answers 409. */
export function useUpdateTestimony() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: TestimonyUpdate }) => apiPut(`/testimonies/${id}`, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** #1364 - Deletes a testimony; one with registry movements or copies answers 409. */
export function useDeleteTestimony() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => apiDelete(`/testimonies/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}
