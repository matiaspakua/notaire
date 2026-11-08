/**
 * Composition root for the concept catalog (#1781 / ADR-030).
 */
import { conceptHttpGateway } from "@/adapters/http/concept/concept-http-gateway";
import { createConceptUseCases } from "@/core/concept/application/use-cases";

export const conceptUseCases = createConceptUseCases(conceptHttpGateway);
