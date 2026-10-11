/**
 * Frontend admin-route access helpers (issue #1052, CU78).
 *
 * Edge proxy cannot read Zustand/localStorage, so login sets a companion
 * non-credential role cookie. Real API authorization remains backend RBAC (#559).
 * Session JWT is HttpOnly cookie `notaire-auth-token` (issue #1051).
 */

export const AUTH_STATUS_COOKIE = "notaire-auth-status";
export const AUTH_ROLE_COOKIE = "notaire-auth-role";
export const FORBIDDEN_QUERY = "forbidden=1";
export const ADMIN_ROUTE_PREFIXES = ["/dashboard/administration", "/dashboard/audit"] as const;

const ADMIN_TYPES = new Set(["ADMIN", "ADMINISTRADOR", "ESCRIBANO"]);

export function isAdminType(userType?: string | null): boolean {
  if (!userType) {
    return false;
  }
  return ADMIN_TYPES.has(userType.trim().toUpperCase());
}

export function isAdminRoute(pathname: string): boolean {
  return ADMIN_ROUTE_PREFIXES.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`),
  );
}

/**
 * Edge decision: deny administration when the session marker is present but
 * the role cookie is missing or not admin-capable.
 */
export function shouldDenyAdminRoute(
  pathname: string,
  authStatus?: string | null,
  role?: string | null,
): boolean {
  if (!isAdminRoute(pathname)) {
    return false;
  }
  if (!authStatus) {
    return false;
  }
  return !isAdminType(role);
}

export function setAuthCookies(userType: string): void {
  if (typeof document === "undefined") {
    return;
  }
  const normalized = (userType || "").trim().toUpperCase() || "UNKNOWN";
  document.cookie = `${AUTH_STATUS_COOKIE}=1; path=/; SameSite=Lax`;
  document.cookie = `${AUTH_ROLE_COOKIE}=${encodeURIComponent(normalized)}; path=/; SameSite=Lax`;
}

export function clearAuthCookies(): void {
  if (typeof document === "undefined") {
    return;
  }
  document.cookie = `${AUTH_STATUS_COOKIE}=; path=/; SameSite=Lax; Max-Age=0`;
  document.cookie = `${AUTH_ROLE_COOKIE}=; path=/; SameSite=Lax; Max-Age=0`;
}

export function forbiddenDashboardPath(): string {
  return `/dashboard?${FORBIDDEN_QUERY}`;
}
