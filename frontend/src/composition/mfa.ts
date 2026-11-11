/**
 * Composition root for MFA (#1640 / ADR-030).
 */
import { mfaHttpGateway } from "@/adapters/http/mfa/mfa-http-gateway";
import { createMfaUseCases } from "@/core/mfa/application/use-cases";

export const mfaUseCases = createMfaUseCases(mfaHttpGateway);
