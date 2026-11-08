/**
 * Registration-draft domain (#1843 / ADR-030).
 */
export interface RegistrationDraft {
  idRegistrationDraft?: number;
  number?: number;
  status?: string;
  dateGeneration?: string;
  dateSubmission?: string;
  registryEntryNumber?: string;
  dateReception?: string;
  finalRegistrationNumber?: string;
  registryNotes?: string;
  dateCorrection?: string;
  idDeed?: number;
}

export interface SubmitRegistrationDraftInput {
  dateSubmission: string;
  registryEntryNumber: string;
}

export interface ObserveRegistrationDraftInput {
  registryNotes: string;
  dateCorrection: string;
}

export interface RegisterRegistrationDraftInput {
  dateReception: string;
  finalRegistrationNumber: string;
}
