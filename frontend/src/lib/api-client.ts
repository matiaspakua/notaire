/**
 * Centralized HTTP client for Notaire REST API.
 * All API calls go through these helpers — never use fetch() directly in components.
 */
import { logger } from "@/lib/logger";

// Use relative path so requests are proxied by the Next.js server (rewrites in next.config.ts).
// This ensures the browser never needs to resolve internal Docker hostnames like "backend".
const BASE_URL = "/api/v1";

/**
 * Name of the currently logged-in application user, read from the persisted
 * auth store (zustand `persist` writes `{ state: { user } }` to localStorage).
 * Sent to the backend on every request so business operations can be
 * attributed to a user in the audit module.
 */
function actingUser(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  try {
    const raw = window.localStorage.getItem("notaire-auth");
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw) as { state?: { user?: { nombre?: string } } };
    return parsed?.state?.user?.nombre ?? null;
  } catch {
    return null;
  }
}

/**
 * Builds request headers, always including the acting-user header when a user
 * is logged in so the backend audit aspect can record who performed the action.
 */
function buildHeaders(base: Record<string, string> = {}): Record<string, string> {
  const headers: Record<string, string> = { ...base };
  const user = actingUser();
  if (user) {
    headers["X-Notaire-User"] = user;
  }
  return headers;
}

async function handleResponse<T>(res: Response, path: string, method: string): Promise<T> {
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    logger.error("api_call_failed", {
      method,
      path,
      status: res.status,
      body: text.slice(0, 500),
    });
    throw new Error(`[${res.status}] ${path}: ${text}`);
  }
  const text = await res.text();
  return text ? (JSON.parse(text) as T) : ({} as T);
}

export async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: buildHeaders({ "Content-Type": "application/json" }),
    cache: "no-store",
  });
  return handleResponse<T>(res, path, "GET");
}

export async function apiPost<T = void>(
  path: string,
  body: unknown
): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: "POST",
    headers: buildHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(body),
  });
  return handleResponse<T>(res, path, "POST");
}

export async function apiPut<T = void>(
  path: string,
  body: unknown
): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: "PUT",
    headers: buildHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(body),
  });
  return handleResponse<T>(res, path, "PUT");
}

export async function apiDelete(path: string): Promise<void> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: "DELETE",
    headers: buildHeaders(),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    logger.error("api_call_failed", {
      method: "DELETE",
      path,
      status: res.status,
      body: text.slice(0, 500),
    });
    throw new Error(`[${res.status}] DELETE ${path}: ${text}`);
  }
}

export async function apiGetBytes(path: string): Promise<Blob> {
  const res = await fetch(`${BASE_URL}${path}`, { headers: buildHeaders() });
  if (!res.ok) {
    logger.error("api_call_failed", { method: "GET", path, status: res.status });
    throw new Error(`[${res.status}] GET ${path}`);
  }
  return res.blob();
}
