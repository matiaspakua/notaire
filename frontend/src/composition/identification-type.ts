/**
 * Composition root for the identification-type catalog (#1732 / ADR-030).
 * Wires the HTTP gateway to core use cases once for the app.
 */
import { identificationTypeHttpGateway } from "@/adapters/http/identification-type/identification-type-http-gateway";
import { createIdentificationTypeUseCases } from "@/core/identification-type/application/use-cases";

export const identificationTypeUseCases = createIdentificationTypeUseCases(
  identificationTypeHttpGateway,
);
