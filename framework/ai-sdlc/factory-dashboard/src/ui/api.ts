/** Browser side of the dashboard API. It never sees a kit token: the server holds it per workspace. */

export type Scope = "read" | "write" | "owner";

export interface Grant {
  identity: string;
  scope: Scope;
  actor: string;
  write_enabled: boolean;
}

export interface WorkspaceInfo {
  id: string;
  label: string;
  knowledge: boolean;
}

export interface SessionInfo {
  csrf: string | null;
  workspaces: Record<string, Grant>;
}

export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    readonly detail: string,
  ) {
    super(detail);
  }
}

let csrf: string | null = null;

export function rememberCsrf(value: string | null): void {
  csrf = value;
}

interface Options {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
  ifMatch?: string | number;
}

export async function api<T>(path: string, { method = "GET", body, ifMatch }: Options = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (method !== "GET") {
    if (csrf) {
      headers["X-Dashboard-CSRF"] = csrf;
    }
    if (body !== undefined) {
      headers["Content-Type"] = "application/json";
    }
  }
  if (ifMatch !== undefined) {
    headers["If-Match"] = String(ifMatch);
  }
  let response: Response;
  try {
    response = await fetch(path, {
      method,
      headers,
      credentials: "same-origin",
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, "dashboard_unreachable", "The dashboard server is not answering.");
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  const data = text ? (JSON.parse(text) as unknown) : null;
  if (!response.ok) {
    const problem = (data ?? {}) as { code?: string; detail?: string };
    throw new ApiError(response.status, problem.code ?? `http_${response.status}`, problem.detail ?? response.statusText);
  }
  return data as T;
}

export const factory = (ws: string, path: string) => `/api/workspaces/${encodeURIComponent(ws)}/factory${path}`;
export const knowledge = (ws: string, endpoint: string, query = "") =>
  `/api/workspaces/${encodeURIComponent(ws)}/knowledge/${endpoint}${query ? `?${query}` : ""}`;
