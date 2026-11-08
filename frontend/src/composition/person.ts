/**
 * Composition root for people list/search (#1823 / ADR-030).
 */
import { personHttpGateway } from "@/adapters/http/person/person-http-gateway";
import { createPersonUseCases } from "@/core/person/application/use-cases";

export const personUseCases = createPersonUseCases(personHttpGateway);
