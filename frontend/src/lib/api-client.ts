/**
 * Centralized HTTP client for Notaire REST API.
 * All API calls go through these helpers — never use fetch() directly in components.
 */
import { logger } from "@/lib/logger";

// Use relative path so requests are proxied by the Next.js server (rewrites in next.config.ts).
// This ensures the browser never needs to resolve internal Docker hostnames like "backend".
const BASE_URL = "/api/v1";

/** Shape of the persisted zustand auth store, as written to localStorage. */
interface PersistedAuthState {
  state?: { user?: { nombre?: string }; token?: string };
}

function readPersistedAuthState(): PersistedAuthState | null {
  if (typeof window === "undefined") {
    return null;
  }
  try {
    const raw = window.localStorage.getItem("notaire-auth");
    return raw ? (JSON.parse(raw) as PersistedAuthState) : null;
  } catch {
    return null;
  }
}

/**
 * JWT issued at login, read from the persisted auth store. Sent as a Bearer
 * token on every request since the backend's /api/** security chain requires
 * authentication for all endpoints except login. The backend's audit aspect
 * attributes the acting user from this token's verified identity, not from
 * any client-supplied header (issue #678).
 */
function authToken(): string | null {
  return readPersistedAuthState()?.state?.token ?? null;
}

/**
 * Builds request headers, including the Bearer token so the backend's
 * security filter chain authenticates the request.
 */
function buildHeaders(base: Record<string, string> = {}): Record<string, string> {
  const headers: Record<string, string> = { ...base };
  const token = authToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
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
