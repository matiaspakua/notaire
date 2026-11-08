import type { RegistrationDraftGateway } from "./ports";
import type {
  ObserveRegistrationDraftInput,
  RegisterRegistrationDraftInput,
  SubmitRegistrationDraftInput,
} from "../domain/registration-draft";

export function createRegistrationDraftUseCases(gateway: RegistrationDraftGateway) {
  return {
    getById: (id: number) => gateway.getById(id),
    generate: (idDeed: number) => gateway.generate(idDeed),
    submit: (id: number, input: SubmitRegistrationDraftInput) => gateway.submit(id, input),
    observe: (id: number, input: ObserveRegistrationDraftInput) => gateway.observe(id, input),
    register: (id: number, input: RegisterRegistrationDraftInput) => gateway.register(id, input),
    downloadReport: (id: number) => gateway.downloadReport(id),
  };
}

export type RegistrationDraftUseCases = ReturnType<typeof createRegistrationDraftUseCases>;
