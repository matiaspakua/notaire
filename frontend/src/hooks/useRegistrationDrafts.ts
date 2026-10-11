import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetBytes, apiPost, apiPut } from "@/lib/api-client";
import type { RegistrationDraft } from "@/types";

export const registrationDraftKeys = {
  detail: (id: number) => ["minutas-inscripcion", id] as const,
};

/** CU82 - Consulta una registrationDraft de inscripción por ID. */
export function useRegistrationDraft(id: number | null) {
  return useQuery({
    queryKey: id ? registrationDraftKeys.detail(id) : ["minutas-inscripcion", "none"],
    queryFn: () => apiGet<RegistrationDraft>(`/registration-drafts/${id}`),
    enabled: id !== null,
  });
}

/** CU82 - Genera la registrationDraft de inscripción para una deed firmada. */
export function useGenerateRegistrationDraft() {
  return useMutation({
    mutationFn: (idDeed: number) =>
      apiPost<RegistrationDraft>("/registration-drafts", { idDeed: idDeed }),
  });
}

/** CU82 - Registra la presentación de la registrationDraft ante el Registro. */
export function useSubmitRegistrationDraft() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      fechaPresentacion,
      numberEntryRegistry,
    }: {
      id: number;
      fechaPresentacion: string;
      numberEntryRegistry: string;
    }) =>
      apiPut<RegistrationDraft>(`/registration-drafts/${id}/submit`, {
        dateSubmission: fechaPresentacion,
        registryEntryNumber: numberEntryRegistry,
      }),
    onSuccess: (_, { id }) => qc.invalidateQueries({ queryKey: registrationDraftKeys.detail(id) }),
  });
}

/** CU82 - Registra una observación formulada por el Registro. */
export function useObserveRegistrationDraft() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      notesRegistration,
      fechaSubsanacion,
    }: {
      id: number;
      notesRegistration: string;
      fechaSubsanacion: string;
    }) =>
      apiPut<RegistrationDraft>(`/registration-drafts/${id}/observe`, {
        registryNotes: notesRegistration,
        dateCorrection: fechaSubsanacion,
      }),
    onSuccess: (_, { id }) => qc.invalidateQueries({ queryKey: registrationDraftKeys.detail(id) }),
  });
}

/** CU82 - Registra la inscripción definitiva de la registrationDraft. */
export function useRegisterRegistrationDraft() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      fechaRecepcion,
      numberRegistrationFinal,
    }: {
      id: number;
      fechaRecepcion: string;
      numberRegistrationFinal: string;
    }) =>
      apiPut<RegistrationDraft>(`/registration-drafts/${id}/register`, {
        dateReception: fechaRecepcion,
        finalRegistrationNumber: numberRegistrationFinal,
      }),
    onSuccess: (_, { id }) => qc.invalidateQueries({ queryKey: registrationDraftKeys.detail(id) }),
  });
}

/** Descarga el formulario normalizado (PDF) de una registrationDraft de inscripción. */
export async function downloadRegistrationDraftReport(idRegistrationDraft: number): Promise<void> {
  const blob = await apiGetBytes(`/reports/registration-draft/${idRegistrationDraft}`);
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `minuta_inscripcion_${idRegistrationDraft}.pdf`;
  link.click();
  URL.revokeObjectURL(link.href);
}
