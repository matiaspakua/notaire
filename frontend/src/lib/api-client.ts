/**
 * Centralized HTTP client for Notaire REST API.
 * All API calls go through these helpers — never use fetch() directly in components.
 *
 * Browser auth uses the HttpOnly session cookie (#1051) via credentials: 'include'
 * through the Next.js same-origin proxy. Do not read JWT from localStorage.
 */
import { logger } from "@/lib/logger";
import { handleAuthenticatedSessionExpiry } from "@/lib/session-expiry";

// Same-origin /api/v1 — proxied at request time by the App Router BFF
// (src/app/api/v1/[...path]) using server-only BACKEND_URL (issue #1055).
// The browser never resolves internal Docker hostnames like "backend".
const BASE_URL = "/api/v1";

const DEFAULT_FETCH_INIT: RequestInit = {
  credentials: "include",
};

/**
 * Builds request headers. Browser sessions authenticate via the HttpOnly cookie
 * forwarded by the Next proxy; Authorization is not attached from script-readable
 * storage (issue #1051). The backend audit aspect attributes the acting user from
 * the verified JWT identity, not from any client-supplied header (issue #678).
 */
function buildHeaders(base: Record<string, string> = {}): Record<string, string> {
  return { ...base };
}

/**
 * Thrown for any non-2xx response, carrying the HTTP status and raw response
 * body so callers can distinguish e.g. a 429 lockout from a generic failure
 * instead of collapsing every error into the same message (issue #756).
 */
export class ApiError extends Error {
  readonly status: number;
  readonly path: string;
  readonly body: string;

  constructor(status: number, path: string, body: string) {
    super(`[${status}] ${path}: ${body}`);
    this.name = "ApiError";
    this.status = status;
    this.path = path;
    this.body = body;
  }
}

function rejectApiFailure(status: number, path: string, body: string): never {
  handleAuthenticatedSessionExpiry(status, path);
  throw new ApiError(status, path, body);
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
    rejectApiFailure(res.status, path, text);
  }
  const text = await res.text();
  return text ? (JSON.parse(text) as T) : ({} as T);
}

export async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...DEFAULT_FETCH_INIT,
    headers: buildHeaders({ "Content-Type": "application/json" }),
    cache: "no-store",
  });
  return handleResponse<T>(res, path, "GET");
}

/** Shape of a Spring Data `Page` response. */
export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

const PAGE_SIZE_ALL = 1000;

/**
 * Fetches a paginated Spring Data endpoint and returns the unwrapped content.
 * Uses a large page size because list screens render the full collection.
 */
export async function apiGetPaged<T>(path: string): Promise<T[]> {
  const separator = path.includes("?") ? "&" : "?";
  const page = await apiGet<SpringPage<T>>(`${path}${separator}size=${PAGE_SIZE_ALL}`);
  return page.content;
}

export async function apiPost<T = void>(
  path: string,
  body: unknown
): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...DEFAULT_FETCH_INIT,
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
    ...DEFAULT_FETCH_INIT,
    method: "PUT",
    headers: buildHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify(body),
  });
  return handleResponse<T>(res, path, "PUT");
}

export async function apiDelete(path: string): Promise<void> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...DEFAULT_FETCH_INIT,
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
    // Throw ApiError so 401 reaches the same session-expiry path as other verbs.
    rejectApiFailure(res.status, path, text);
  }
}

export async function apiGetBytes(path: string): Promise<Blob> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...DEFAULT_FETCH_INIT,
    headers: buildHeaders(),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    logger.error("api_call_failed", {
      method: "GET",
      path,
      status: res.status,
      body: text.slice(0, 500),
    });
    rejectApiFailure(res.status, path, text);
  }
  return res.blob();
}

/** Clears the HttpOnly auth cookie via the backend logout endpoint (#1051). */
export async function apiLogout(): Promise<void> {
  try {
    await fetch(`${BASE_URL}/usuarios/logout`, {
      ...DEFAULT_FETCH_INIT,
      method: "POST",
      headers: buildHeaders({ "Content-Type": "application/json" }),
    });
  } catch {
    // Best-effort: client state is still cleared by the caller.
  }
}
