/** HTTP clients for the two contracts the dashboard speaks: `aisdlc serve` (factory control) and RepoLens. */

export interface Problem {
  type: string;
  title: string;
  status: number;
  detail: string;
  code: string;
}

export interface UpstreamReply {
  status: number;
  body: unknown;
}

export type Fetch = typeof fetch;

export function problem(status: number, code: string, detail: string): UpstreamReply {
  return { status, body: { type: "about:blank", title: code.replaceAll("_", " "), status, detail, code } };
}

const TIMEOUT_MS = 15_000;
const CSRF_HEADER = "X-AISDLC-CSRF";

async function readBody(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch {
    return { detail: text.slice(0, 500) };
  }
}

export interface KitCall {
  method: "GET" | "POST" | "PUT";
  path: string;
  query?: string;
  token: string;
  body?: unknown;
  ifMatch?: string;
}

/** The kit enforces scope, CSRF and the actor; the dashboard only adds the user's token server-side. */
export async function callKit(fetcher: Fetch, base: string, call: KitCall): Promise<UpstreamReply> {
  const headers: Record<string, string> = { Authorization: `Bearer ${call.token}`, Accept: "application/json" };
  if (call.method !== "GET") {
    headers[CSRF_HEADER] = call.token;
    headers["Content-Type"] = "application/json";
  }
  if (call.ifMatch) {
    headers["If-Match"] = call.ifMatch;
  }
  const url = `${base}${call.path}${call.query ? `?${call.query}` : ""}`;
  try {
    const response = await fetcher(url, {
      method: call.method,
      headers,
      body: call.method === "GET" ? undefined : JSON.stringify(call.body ?? {}),
      signal: AbortSignal.timeout(TIMEOUT_MS),
      redirect: "error",
    });
    return { status: response.status, body: await readBody(response) };
  } catch {
    return problem(502, "kit_unreachable", "aisdlc serve did not answer; is it running for this workspace?");
  }
}

/** Reachability without credentials: any HTTP answer, a 401 included, proves the kit is up. */
export async function probe(fetcher: Fetch, base: string): Promise<boolean> {
  try {
    const response = await fetcher(`${base}/factory/capabilities`, {
      headers: { Accept: "application/json" },
      signal: AbortSignal.timeout(1_500),
      redirect: "error",
    });
    await response.body?.cancel();
    return response.status < 500;
  } catch {
    return false;
  }
}

export async function callKnowledge(fetcher: Fetch, base: string, path: string, query: string): Promise<UpstreamReply> {
  try {
    const response = await fetcher(`${base}${path}${query ? `?${query}` : ""}`, {
      headers: { Accept: "application/json" },
      signal: AbortSignal.timeout(TIMEOUT_MS),
      redirect: "error",
    });
    const body = await readBody(response);
    if (!response.ok) {
      const detail = typeof body === "object" && body && "detail" in body ? String(body.detail) : "RepoLens error";
      return problem(response.status, "knowledge_error", detail);
    }
    return { status: 200, body };
  } catch {
    return problem(502, "knowledge_unreachable", "RepoLens did not answer; start `repolens serve` for this workspace");
  }
}
