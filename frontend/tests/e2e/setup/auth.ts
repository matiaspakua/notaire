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
import fs from "node:fs";
import type { Page } from "@playwright/test";
import { apiPost } from "./api-helpers";

interface LoginResponse {
  valido: boolean;
  token: string;
  idUsuario: number;
  nombre: string;
  tipo: string;
  message?: string;
}

const ADMIN_TOKEN_FILE = "tests/e2e/fixtures/e2e-admin-token.txt";

function readPersistedAdminToken(): string | undefined {
  if (process.env.E2E_ADMIN_TOKEN) {
    return process.env.E2E_ADMIN_TOKEN;
  }
  try {
    if (fs.existsSync(ADMIN_TOKEN_FILE)) {
      const token = fs.readFileSync(ADMIN_TOKEN_FILE, "utf8").trim();
      return token || undefined;
    }
  } catch {
    // ignore missing/unreadable fixture
  }
  return undefined;
}

async function applyAdminSession(
  page: Page,
  token: string,
  user: { nombre: string; tipo: string; valido: boolean; idUsuario: number },
): Promise<void> {
  const role = (user.tipo ?? "ADMIN").toUpperCase();
  process.env.E2E_ADMIN_TOKEN = token;

  await page.context().addCookies([
    { name: "notaire-auth-status", value: "authenticated", domain: "localhost", path: "/" },
    { name: "notaire-auth-role", value: role, domain: "localhost", path: "/" },
  ]);
  // addInitScript may resolve to Disposable in current Playwright typings —
  // await and discard so this helper stays Promise<void>.
  await page.addInitScript(
    ([t, u]) => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: { user: u, token: t, isAuthenticated: true },
          version: 0,
        }),
      );
    },
    [token, user] as const,
  );
}

/**
 * Log in as admin and make the JWT available to both:
 *  - `page.request` helpers (`process.env.E2E_ADMIN_TOKEN`)
 *  - the browser app (`localStorage` `notaire-auth`, same shape as zustand persist)
 *
 * Prefers the JWT written by global-setup (env / fixture file) so parallel
 * workers do not re-login and trip LoginAttemptService 429 lockouts.
 * Falls back to API login only when no shared token is available.
 */
export async function authenticateAsAdmin(
  page: Page,
  nombre: string = "admin",
  contrasenia: string = "admin",
): Promise<void> {
  const fallbackUser = {
    nombre,
    tipo: "ADMIN",
    valido: true,
    idUsuario: 1,
  };

  const persisted = readPersistedAdminToken();
  if (persisted) {
    await applyAdminSession(page, persisted, fallbackUser);
    return;
  }

  const result = await apiPost<LoginResponse>(page, "/usuarios/login", {
    name: nombre,
    password: contrasenia,
  });
  const data = result.ok ? result.data : undefined;
  const token = data?.valido ? data.token : undefined;

  if (token) {
    await applyAdminSession(page, token, {
      nombre: data?.nombre ?? nombre,
      tipo: data?.tipo ?? "ADMIN",
      valido: true,
      idUsuario: data?.idUsuario ?? 1,
    });
    return;
  }

  throw new Error(
    `authenticateAsAdmin login failed (status=${result.status}): ${
      data?.message ?? result.error ?? "no token"
    }`,
  );
}

/**
 * Ensure the browser has a hydrated admin session before list-page assertions.
 * Re-writes localStorage after the first navigation so zustand persist cannot
 * race the init script and leave API fetches unauthenticated (empty tables).
 */
export async function establishAdminBrowserSession(page: Page): Promise<void> {
  await authenticateAsAdmin(page);
  const token = process.env.E2E_ADMIN_TOKEN;
  if (!token) {
    throw new Error("E2E_ADMIN_TOKEN missing after authenticateAsAdmin");
  }

  await page.goto("/dashboard");
  await page.evaluate(
    ([t, u]) => {
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: { user: u, token: t, isAuthenticated: true },
          version: 0,
        }),
      );
    },
    [
      token,
      {
        nombre: "admin",
        tipo: "ADMIN",
        valido: true,
        idUsuario: 1,
      },
    ] as const,
  );
  await page.reload({ waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");
}
