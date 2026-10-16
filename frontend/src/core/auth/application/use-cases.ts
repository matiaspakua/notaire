import type { AuthGateway } from "./ports";
import type { LoginInput } from "../domain/auth";

export function createAuthUseCases(gateway: AuthGateway) {
  return {
    login: (input: LoginInput) => gateway.login(input),
    logout: () => gateway.logout(),
  };
}

export type AuthUseCases = ReturnType<typeof createAuthUseCases>;
