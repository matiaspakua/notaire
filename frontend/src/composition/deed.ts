/**
 * Composition root for deeds (#1831 / ADR-030).
 * Wires the HTTP gateway to core use cases once for the app.
 */
import { deedHttpGateway } from "@/adapters/http/deed/deed-http-gateway";
import { createDeedUseCases } from "@/core/deed/application/use-cases";

export const deedUseCases = createDeedUseCases(deedHttpGateway);
