import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost, apiGetBytes } from "@/lib/api-client";
import type { Testimonio } from "@/types";

export const testimoniosKeys = {
  all: ["testimonios"] as const,
};

export function useTestimonios() {
  return useQuery({
    queryKey: testimoniosKeys.all,
    queryFn: () => apiGet<Testimonio[]>("/testimonio"),
  });
}

/** CU07 - Genera un testimonio numerado a partir de una escritura "Firmada". */
export function useGenerarTestimonio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (idEscritura: number) => apiPost<Testimonio>(`/testimonio/${idEscritura}/generar`, {}),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniosKeys.all }),
  });
}

/** CU08 - Registra la verificación de un testimonio (observado o no, con motivo). */
export function useVerificarTestimonio() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, observado, observaciones }: { id: number; observado: boolean; observaciones?: string }) =>
      apiPost<Testimonio>(`/testimonio/${id}/verificar`, { observado, observaciones }),
    onSuccess: () => qc.invalidateQueries({ queryKey: testimoniosKeys.all }),
  });
}

/** Descarga la copia impresa (PDF) de un testimonio verificado. */
export async function descargarCopiaTestimonio(idTestimonio: number): Promise<void> {
  const blob = await apiGetBytes(`/reportes/testimonio/${idTestimonio}/copia`);
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `testimonio_${idTestimonio}_copia.pdf`;
  link.click();
  URL.revokeObjectURL(link.href);
}
