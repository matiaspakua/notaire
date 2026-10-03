/**
 * Ends the local session when an authenticated API call returns HTTP 401.
 * Issue #1053 / CU84 — redirect to login with an expired-session signal.
 * Issue #1051 — also best-effort clear the HttpOnly auth cookie.
 */
import { useAuthStore } from "@/store/auth-store";

const LOGIN_API_PATH = "/usuarios/login";
export const SESSION_EXPIRED_LOGIN_URL = "/login?expired=1";

let handlingSessionExpiry = false;

/** Test-only: reset the re-entrancy guard between cases. */
export function resetSessionExpiryGuardForTests(): void {
  handlingSessionExpiry = false;
}

export function isLoginApiPath(path: string): boolean {
  return path === LOGIN_API_PATH || path.startsWith(`${LOGIN_API_PATH}?`);
}

function hasLocalSession(): boolean {
  if (useAuthStore.getState().isAuthenticated) {
    return true;
  }
  if (typeof window === "undefined") {
    return false;
  }
  try {
    const raw = window.localStorage.getItem("notaire-auth");
    if (!raw) {
      return false;
    }
    const parsed = JSON.parse(raw) as { state?: { isAuthenticated?: boolean } };
    return Boolean(parsed.state?.isAuthenticated);
  } catch {
    return false;
  }
}

function clearHttpOnlyAuthCookieBestEffort(): void {
  if (typeof window === "undefined" || typeof fetch !== "function") {
    return;
  }
  try {
    const result = fetch("/api/v1/usuarios/logout", {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    });
    if (result && typeof (result as Promise<unknown>).catch === "function") {
      void (result as Promise<unknown>).catch(() => {
        // Best-effort; client state is still cleared below.
      });
    }
  } catch {
    // Best-effort; client state is still cleared below.
  }
}

/**
 * If this is an authenticated-session 401, clear auth and navigate to login.
 * @returns true when the expiry path was taken (or is already in progress)
 */
export function handleAuthenticatedSessionExpiry(status: number, path: string): boolean {
  if (status !== 401) {
    return false;
  }
  if (isLoginApiPath(path)) {
    return false;
  }
  if (typeof window === "undefined") {
    return false;
  }
  if (!hasLocalSession()) {
    return false;
  }
  if (handlingSessionExpiry) {
    return true;
  }
  handlingSessionExpiry = true;
  clearHttpOnlyAuthCookieBestEffort();
  useAuthStore.getState().logout();
  window.location.assign(SESSION_EXPIRED_LOGIN_URL);
  return true;
}

/** Safety-net entry point for React Query `onError` (and similar). */
export function handleSessionExpiryFromUnknown(error: unknown): boolean {
  if (
    error &&
    typeof error === "object" &&
    "status" in error &&
    "path" in error &&
    typeof (error as { status: unknown }).status === "number" &&
    typeof (error as { path: unknown }).path === "string"
  ) {
    const { status, path } = error as { status: number; path: string };
    return handleAuthenticatedSessionExpiry(status, path);
  }
  return false;
}
