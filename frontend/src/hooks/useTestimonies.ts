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

/** CU07 - Genera un testimonio numerado a partir de una escritura "Firmada". */
export function useGenerateTestimony() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idEscritura: number) => apiPost<Testimony>(`/testimonies/${idEscritura}/generate`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** CU08 - Registra la verificación de un testimonio (observado o no, con motivo). */
export function useVerifyTestimony() {
  const qc = useQueryClient();
  return useMutation({
    // Backend body is DtoTestimony(flagged, notes) — not observado/observaciones.
    mutationFn: ({ id, observado, observaciones }: { id: number; observado: boolean; observaciones?: string }) =>
      apiPost<Testimony>(`/testimonies/${id}/verify`, { flagged: observado, notes: observaciones }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniesKeys.all }),
  });
}

/** Descarga la copia impresa (PDF) de un testimonio verificado. */
export async function downloadTestimonyCopy(idTestimonio: number): Promise<void> {
  const blob = await apiGetBytes(`/reports/testimonies/${idTestimonio}/copies`);
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `testimonio_${idTestimonio}_copia.pdf`;
  link.click();
  URL.revokeObjectURL(link.href);
}
