/**
 * Centralized HTTP client for Notaire REST API.
 * All API calls go through these helpers — never use fetch() directly in components.
 */

// Use relative path so requests are proxied by the Next.js server (rewrites in next.config.ts).
// This ensures the browser never needs to resolve internal Docker hostnames like "backend".
const BASE_URL = "/api/v1";

async function handleResponse<T>(res: Response, path: string): Promise<T> {
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`[${res.status}] ${path}: ${text}`);
  }
  const text = await res.text();
  return text ? (JSON.parse(text) as T) : ({} as T);
}

export async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });
  return handleResponse<T>(res, path);
}

export async function apiPost<T = void>(
  path: string,
  body: unknown
): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return handleResponse<T>(res, path);
}

export async function apiPut<T = void>(
  path: string,
  body: unknown
): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return handleResponse<T>(res, path);
}

export async function apiDelete(path: string): Promise<void> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: "DELETE",
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`[${res.status}] DELETE ${path}: ${text}`);
  }
}

export async function apiGetBytes(path: string): Promise<Blob> {
  const res = await fetch(`${BASE_URL}${path}`);
  if (!res.ok) throw new Error(`[${res.status}] GET ${path}`);
  return res.blob();
}
