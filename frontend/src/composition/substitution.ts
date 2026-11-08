/**
 * Composition root for substitutions (#1845 / ADR-030).
 */
import { substitutionHttpGateway } from "@/adapters/http/substitution/substitution-http-gateway";
import { createSubstitutionUseCases } from "@/core/substitution/application/use-cases";

export const substitutionUseCases = createSubstitutionUseCases(substitutionHttpGateway);
