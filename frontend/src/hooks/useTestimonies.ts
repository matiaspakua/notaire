import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiGetBytes } from "@/lib/api-client";
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
