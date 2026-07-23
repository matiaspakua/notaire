/**
 * Shared E2E auth fixture.
 *
 * The backend's security chain requires a JWT Bearer token on every endpoint
 * except login. Several spec files used to inject a *fake* localStorage
 * auth state (no token) to skip the UI login form — that made every
 * subsequent API write silently 401 (create/edit/delete never persisted,
 * dialogs never closed) while assertions that didn't check status codes
 * kept passing. This performs a real login and persists the resulting JWT
 * in the same shape the app's own auth store uses.
 */
import type { Page } from "@playwright/test";
import { apiPost } from "./api-helpers";

interface LoginResponse {
  valido: boolean;
  token: string;
  idUsuario: number;
  nombre: string;
  tipo: string;
}

export async function authenticateAsAdmin(
  page: Page,
  nombre: string = "admin",
  contrasenia: string = "admin",
): Promise<void> {
  await page.context().addCookies([
    { name: "notaire-auth-status", value: "authenticated", domain: "localhost", path: "/" },
  ]);

  const result = await apiPost<LoginResponse>(page, "/usuarios/login", { nombre, contrasenia });
  const data = result.ok ? result.data : undefined;

  await page.addInitScript(
    ([token, user]) => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: { user, token, isAuthenticated: true },
          version: 0,
        }),
      );
    },
    [
      data?.token ?? "",
      {
        nombre: data?.nombre ?? nombre,
        tipo: data?.tipo ?? "ADMIN",
        valido: true,
        idUsuario: data?.idUsuario ?? 1,
      },
    ] as const,
  );

  // Visible to page.request-based helper calls (api-helpers.ts) made after this point.
  if (data?.token) {
    process.env.E2E_ADMIN_TOKEN = data.token;
  }
}
