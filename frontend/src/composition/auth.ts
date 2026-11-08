/**
 * Composition root for auth (#1843 / ADR-030).
 */
import { authHttpGateway } from "@/adapters/http/auth/auth-http-gateway";
import { createAuthUseCases } from "@/core/auth/application/use-cases";

export const authUseCases = createAuthUseCases(authHttpGateway);
