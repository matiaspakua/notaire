/**
 * Composition root for testimonies (#1834 / ADR-030).
 */
import { testimonyHttpGateway } from "@/adapters/http/testimony/testimony-http-gateway";
import { createTestimonyUseCases } from "@/core/testimony/application/use-cases";

export const testimonyUseCases = createTestimonyUseCases(testimonyHttpGateway);
