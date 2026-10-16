/**
 * React Query hooks for registration drafts.
 * Hexagon wiring: #1843 / ADR-030.
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { registrationDraftUseCases } from "@/composition/registration-draft";
import type { RegistrationDraft as DomainDraft } from "@/core/registration-draft/domain/registration-draft";
import type { RegistrationDraft } from "@/types";

export const registrationDraftKeys = {
  detail: (id: number) => ["registration-drafts", id] as const,
};

function toWire(d: DomainDraft): RegistrationDraft {
  return d as RegistrationDraft;
}

/** CU82 - Loads one registration draft by id. */
export function useRegistrationDraft(id: number | null) {
  return useQuery({
    queryKey: id ? registrationDraftKeys.detail(id) : ["registration-drafts", "none"],
    queryFn: async () => toWire(await registrationDraftUseCases.getById(id!)),
    enabled: id !== null,
  });
}

/** CU82 - Creates the registration draft for a deed in status Firmada. */
export function useGenerateRegistrationDraft() {
  return useMutation({
    mutationFn: async (idDeed: number) => toWire(await registrationDraftUseCases.generate(idDeed)),
  });
}

/** CU82 - Records that the registration draft was submitted to the registry. */
export function useSubmitRegistrationDraft() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({
      id,
      dateSubmission,
      registryEntryNumber,
    }: {
      id: number;
      dateSubmission: string;
      registryEntryNumber: string;
    }) =>
      toWire(
        await registrationDraftUseCases.submit(id, { dateSubmission, registryEntryNumber }),
      ),
    onSuccess: (_, { id }) => qc.invalidateQueries({ queryKey: registrationDraftKeys.detail(id) }),
  });
}

/** CU82 - Records a note raised by the registry. */
export function useObserveRegistrationDraft() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({
      id,
      registryNotes,
      dateCorrection,
    }: {
      id: number;
      registryNotes: string;
      dateCorrection: string;
    }) =>
      toWire(await registrationDraftUseCases.observe(id, { registryNotes, dateCorrection })),
    onSuccess: (_, { id }) => qc.invalidateQueries({ queryKey: registrationDraftKeys.detail(id) }),
  });
}

/** CU82 - Records the final registration of the draft. */
export function useRegisterRegistrationDraft() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({
      id,
      dateReception,
      finalRegistrationNumber,
    }: {
      id: number;
      dateReception: string;
      finalRegistrationNumber: string;
    }) =>
      toWire(
        await registrationDraftUseCases.register(id, { dateReception, finalRegistrationNumber }),
      ),
    onSuccess: (_, { id }) => qc.invalidateQueries({ queryKey: registrationDraftKeys.detail(id) }),
  });
}

/** Downloads the normalized registration-draft form (PDF). */
export async function downloadRegistrationDraftReport(idRegistrationDraft: number): Promise<void> {
  const blob = await registrationDraftUseCases.downloadReport(idRegistrationDraft);
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `minuta_inscripcion_${idRegistrationDraft}.pdf`;
  link.click();
  URL.revokeObjectURL(link.href);
}
