import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetBytes, apiPost, apiPut } from "@/lib/api-client";
import type { MinutaInscripcion } from "@/types";

export const minutasInscripcionKeys = {
  detail: (id: number) => ["minutas-inscripcion", id] as const,
};

/** CU82 - Consulta una minuta de inscripción por ID. */
export function useMinutaInscripcion(id: number | null) {
  return useQuery({
    queryKey: id ? minutasInscripcionKeys.detail(id) : ["minutas-inscripcion", "none"],
    queryFn: () => apiGet<MinutaInscripcion>(`/minutas-inscripcion/${id}`),
    enabled: id !== null,
  });
}

/** CU82 - Genera la minuta de inscripción para una escritura firmada. */
export function useGenerarMinutaInscripcion() {
  return useMutation({
    mutationFn: (idEscritura: number) =>
      apiPost<MinutaInscripcion>("/minutas-inscripcion", { idEscritura }),
  });
}

/** CU82 - Registra la presentación de la minuta ante el Registro. */
export function usePresentarMinutaInscripcion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      fechaPresentacion,
      numeroEntradaRegistral,
    }: {
      id: number;
      fechaPresentacion: string;
      numeroEntradaRegistral: string;
    }) =>
      apiPut<MinutaInscripcion>(`/minutas-inscripcion/${id}/presentar`, {
        fechaPresentacion,
        numeroEntradaRegistral,
      }),
    onSuccess: (_, { id }) => qc.invalidateQueries({ queryKey: minutasInscripcionKeys.detail(id) }),
  });
}

/** CU82 - Registra una observación formulada por el Registro. */
export function useObservarMinutaInscripcion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      observacionesRegistro,
      fechaSubsanacion,
    }: {
      id: number;
      observacionesRegistro: string;
      fechaSubsanacion: string;
    }) =>
      apiPut<MinutaInscripcion>(`/minutas-inscripcion/${id}/observar`, {
        observacionesRegistro,
        fechaSubsanacion,
      }),
    onSuccess: (_, { id }) => qc.invalidateQueries({ queryKey: minutasInscripcionKeys.detail(id) }),
  });
}

/** CU82 - Registra la inscripción definitiva de la minuta. */
export function useInscribirMinutaInscripcion() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      fechaRecepcion,
      numeroInscripcionDefinitivo,
    }: {
      id: number;
      fechaRecepcion: string;
      numeroInscripcionDefinitivo: string;
    }) =>
      apiPut<MinutaInscripcion>(`/minutas-inscripcion/${id}/inscribir`, {
        fechaRecepcion,
        numeroInscripcionDefinitivo,
      }),
    onSuccess: (_, { id }) => qc.invalidateQueries({ queryKey: minutasInscripcionKeys.detail(id) }),
  });
}

/** Descarga el formulario normalizado (PDF) de una minuta de inscripción. */
export async function descargarReporteMinutaInscripcion(idMinutaInscripcion: number): Promise<void> {
  const blob = await apiGetBytes(`/reportes/minuta-inscripcion/${idMinutaInscripcion}`);
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `minuta_inscripcion_${idMinutaInscripcion}.pdf`;
  link.click();
  URL.revokeObjectURL(link.href);
}
