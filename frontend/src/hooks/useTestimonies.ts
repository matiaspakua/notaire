import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiGetBytes } from "@/lib/api-client";
import type { Testimony } from "@/types";

export const testimoniesKeys = {
  all: ["testimonios"] as const,
};

export function useTestimonies() {
  return useQuery({
    queryKey: testimoniesKeys.all,
    queryFn: () => apiGet<Testimony[]>("/testimonies"),
  });
}

/** CU07 - Genera un testimony numerado a partir de una deed "Firmada". */
export function useGenerateTestimony() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idDeed: number) => apiPost<Testimony>(`/testimonies/${idDeed}/generate`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** CU08 - Registra la verificación de un testimony (observado o no, con motivo). */
export function useVerifyTestimony() {
  const qc = useQueryClient();
  return useMutation({
    // Backend body is DtoTestimony(flagged, notes) — not observado/notes.
    mutationFn: ({ id, observado, notes }: { id: number; observado: boolean; notes?: string }) =>
      apiPost<Testimony>(`/testimonies/${id}/verify`, { flagged: observado, notes: notes }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** Descarga la copia impresa (PDF) de un testimony verificado. */
export async function downloadTestimonyCopy(idTestimony: number): Promise<void> {
  const blob = await apiGetBytes(`/reports/testimonies/${idTestimony}/copies`);
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `testimonio_${idTestimony}_copia.pdf`;
  link.click();
  URL.revokeObjectURL(link.href);
}
