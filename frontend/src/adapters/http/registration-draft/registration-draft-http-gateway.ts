/**
 * HTTP adapter for registration drafts (#1843 / ADR-030).
 */
import { apiGet, apiGetBytes, apiPost, apiPut } from "@/lib/api-client";
import type {
  ObserveRegistrationDraftInput,
  RegisterRegistrationDraftInput,
  RegistrationDraft,
  SubmitRegistrationDraftInput,
} from "@/core/registration-draft/domain/registration-draft";
import type { RegistrationDraftGateway } from "@/core/registration-draft/application/ports";

export const registrationDraftHttpGateway: RegistrationDraftGateway = {
  getById: (id) => apiGet<RegistrationDraft>(`/registration-drafts/${id}`),
  generate: (idDeed) => apiPost<RegistrationDraft>("/registration-drafts", { idDeed }),
  submit: (id, input: SubmitRegistrationDraftInput) =>
    apiPut<RegistrationDraft>(`/registration-drafts/${id}/submit`, input),
  observe: (id, input: ObserveRegistrationDraftInput) =>
    apiPut<RegistrationDraft>(`/registration-drafts/${id}/observe`, input),
  register: (id, input: RegisterRegistrationDraftInput) =>
    apiPut<RegistrationDraft>(`/registration-drafts/${id}/register`, input),
  downloadReport: (id) => apiGetBytes(`/reports/registration-draft/${id}`),
};
