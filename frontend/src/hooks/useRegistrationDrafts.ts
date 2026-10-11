import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetBytes, apiPost, apiPut } from "@/lib/api-client";
import type { RegistrationDraft } from "@/types";

export const registrationDraftKeys = {
  detail: (id: number) => ["minutas-inscripcion", id] as const,
};

/** CU82 - Loads one registration draft by id. */
export function useRegistrationDraft(id: number | null) {
  return useQuery({
    queryKey: id ? registrationDraftKeys.detail(id) : ["minutas-inscripcion", "none"],
    queryFn: () => apiGet<RegistrationDraft>(`/registration-drafts/${id}`),
    enabled: id !== null,
  });
}

/** CU82 - Creates the registration draft for a deed in status Firmada. */
export function useGenerateRegistrationDraft() {
  return useMutation({
    mutationFn: (idDeed: number) =>
      apiPost<RegistrationDraft>("/registration-drafts", { idDeed: idDeed }),
  });
}

/** CU82 - Records that the registration draft was submitted to the registry. */
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

/** CU82 - Records a note raised by the registry. */
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

/** CU82 - Records the final registration of the draft. */
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

/** Downloads the normalized registration-draft form (PDF). */
export async function downloadRegistrationDraftReport(idRegistrationDraft: number): Promise<void> {
  const blob = await apiGetBytes(`/reports/registration-draft/${idRegistrationDraft}`);
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `minuta_inscripcion_${idRegistrationDraft}.pdf`;
  link.click();
  URL.revokeObjectURL(link.href);
}
