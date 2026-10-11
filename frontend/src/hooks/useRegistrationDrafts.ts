import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiGetBytes, apiPost, apiPut } from "@/lib/api-client";
import type { RegistrationDraft } from "@/types";

export const registrationDraftKeys = {
  detail: (id: number) => ["registration-drafts", id] as const,
};

/** CU82 - Loads one registration draft by id. */
export function useRegistrationDraft(id: number | null) {
  return useQuery({
    queryKey: id ? registrationDraftKeys.detail(id) : ["registration-drafts", "none"],
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
      dateSubmission,
      registryEntryNumber,
    }: {
      id: number;
      dateSubmission: string;
      registryEntryNumber: string;
    }) =>
      apiPut<RegistrationDraft>(`/registration-drafts/${id}/submit`, {
        dateSubmission,
        registryEntryNumber,
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
      registryNotes,
      dateCorrection,
    }: {
      id: number;
      registryNotes: string;
      dateCorrection: string;
    }) =>
      apiPut<RegistrationDraft>(`/registration-drafts/${id}/observe`, {
        registryNotes,
        dateCorrection,
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
      dateReception,
      finalRegistrationNumber,
    }: {
      id: number;
      dateReception: string;
      finalRegistrationNumber: string;
    }) =>
      apiPut<RegistrationDraft>(`/registration-drafts/${id}/register`, {
        dateReception,
        finalRegistrationNumber,
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
