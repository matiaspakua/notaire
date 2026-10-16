import type { LoginInput, LoginResult } from "../domain/auth";

export interface AuthGateway {
  login(input: LoginInput): Promise<LoginResult>;
  logout(): Promise<void>;
}
