/**
 * HTTP adapter for auth (#1843 / ADR-030).
 */
import { apiLogout, apiPost } from "@/lib/api-client";
import type { LoginInput, LoginResult } from "@/core/auth/domain/auth";
import type { AuthGateway } from "@/core/auth/application/ports";

export const authHttpGateway: AuthGateway = {
  login: (input: LoginInput) => apiPost<LoginResult>("/users/login", input),
  logout: () => apiLogout(),
};
