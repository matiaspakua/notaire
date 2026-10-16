/**
 * Composition root for registration drafts (#1843 / ADR-030).
 */
import { registrationDraftHttpGateway } from "@/adapters/http/registration-draft/registration-draft-http-gateway";
import { createRegistrationDraftUseCases } from "@/core/registration-draft/application/use-cases";

export const registrationDraftUseCases = createRegistrationDraftUseCases(registrationDraftHttpGateway);
