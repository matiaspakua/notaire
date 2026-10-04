/**
 * Shared E2E auth fixture.
 *
 * Browser sessions use the HttpOnly JWT cookie (`notaire-auth-token`) plus
 * non-credential UX cookies (#1052). API helpers still use Bearer via
 * `E2E_ADMIN_TOKEN` for `page.request` seeding (issue #1051).
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
export const AUTH_TOKEN_COOKIE = "notaire-auth-token";

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
    {
      name: AUTH_TOKEN_COOKIE,
      value: token,
      domain: "localhost",
      path: "/",
      httpOnly: true,
      sameSite: "Lax",
    },
    { name: "notaire-auth-status", value: "authenticated", domain: "localhost", path: "/" },
    { name: "notaire-auth-role", value: role, domain: "localhost", path: "/" },
  ]);
  // Persist only non-credential client state — never the JWT (#1051).
  // Gate on the UX status cookie so clearing cookies (logout / role switch)
  // does not re-poison localStorage and trip session-expiry 401 → /login?expired=1.
  await page.addInitScript(
    ([u]) => {
      if (!document.cookie.split(";").some((c) => c.trim().startsWith("notaire-auth-status="))) {
        return;
      }
      localStorage.setItem(
        "notaire-auth",
        JSON.stringify({
          state: { user: u, isAuthenticated: true },
          version: 0,
        }),
      );
    },
    [user] as const,
  );
}

/**
 * Log in as admin and make credentials available to both:
 *  - `page.request` helpers (`process.env.E2E_ADMIN_TOKEN` Bearer)
 *  - the browser app (HttpOnly cookie + UX cookies + zustand persist without token)
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
 */
export async function establishAdminBrowserSession(page: Page): Promise<void> {
  await authenticateAsAdmin(page);
  const token = process.env.E2E_ADMIN_TOKEN;
  if (!token) {
    throw new Error("E2E_ADMIN_TOKEN missing after authenticateAsAdmin");
  }

  await page.goto("/dashboard");
  await page.evaluate((u) => {
    localStorage.setItem(
      "notaire-auth",
      JSON.stringify({
        state: { user: u, isAuthenticated: true },
        version: 0,
      }),
    );
  }, {
    nombre: "admin",
    tipo: "ADMIN",
    valido: true,
    idUsuario: 1,
  });
  await page.reload({ waitUntil: "domcontentloaded" });
  await page.waitForLoadState("networkidle");
}
