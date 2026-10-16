/**
 * Composition root for users (#1843 / ADR-030).
 */
import { userHttpGateway } from "@/adapters/http/user/user-http-gateway";
import { createUserUseCases } from "@/core/user/application/use-cases";

export const userUseCases = createUserUseCases(userHttpGateway);
