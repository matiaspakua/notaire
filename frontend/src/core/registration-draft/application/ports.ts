import type {
  ObserveRegistrationDraftInput,
  RegisterRegistrationDraftInput,
  RegistrationDraft,
  SubmitRegistrationDraftInput,
} from "../domain/registration-draft";

export interface RegistrationDraftGateway {
  getById(id: number): Promise<RegistrationDraft>;
  generate(idDeed: number): Promise<RegistrationDraft>;
  submit(id: number, input: SubmitRegistrationDraftInput): Promise<RegistrationDraft>;
  observe(id: number, input: ObserveRegistrationDraftInput): Promise<RegistrationDraft>;
  register(id: number, input: RegisterRegistrationDraftInput): Promise<RegistrationDraft>;
  downloadReport(id: number): Promise<Blob>;
}
