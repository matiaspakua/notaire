/**
 * Composition root for testimony movements (#1834 / ADR-030).
 */
import { testimonyMovementHttpGateway } from "@/adapters/http/testimony-movement/testimony-movement-http-gateway";
import { createTestimonyMovementUseCases } from "@/core/testimony-movement/application/use-cases";

export const testimonyMovementUseCases = createTestimonyMovementUseCases(testimonyMovementHttpGateway);
