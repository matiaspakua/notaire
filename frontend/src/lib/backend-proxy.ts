/**
 * Request-time BFF proxy to the Spring backend (issue #1055 / ADR-005).
 *
 * Reads server-only BACKEND_URL at request handling time so a single standalone
 * image can retarget environments without baking next.config rewrites at build.
 * Forwards Cookie / Set-Cookie for HttpOnly JWT sessions (#1051).
 */

const HOP_BY_HOP_HEADERS = new Set([
  "connection",
  "keep-alive",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailers",
  "transfer-encoding",
  "upgrade",
  "host",
  "content-length",
]);

/**
 * Resolves the upstream API base from server env only.
 * Does not fall back to NEXT_PUBLIC_API_URL (client-visible; must not drive infra).
 */
export function resolveBackendBaseUrl(
  env: NodeJS.ProcessEnv = process.env,
): string | null {
  const raw = env.BACKEND_URL?.trim();
  if (!raw) {
    return null;
  }
  return raw.replace(/\/+$/, "");
}

/** Join BACKEND_URL with path segments (already decoded by the App Router). */
export function joinUpstreamUrl(base: string, pathSegments: string[]): string {
  const cleaned = pathSegments
    .map((segment) => segment.replace(/^\/+|\/+$/g, ""))
    .filter(Boolean)
    .join("/");
  const normalizedBase = base.replace(/\/+$/, "");
  return cleaned ? `${normalizedBase}/${cleaned}` : normalizedBase;
}

function copyRequestHeaders(request: Request): Headers {
  const headers = new Headers();
  request.headers.forEach((value, key) => {
    if (HOP_BY_HOP_HEADERS.has(key.toLowerCase())) {
      return;
    }
    headers.set(key, value);
  });
  return headers;
}

function copyResponseHeaders(upstream: Response): Headers {
  const headers = new Headers();
  upstream.headers.forEach((value, key) => {
    const lower = key.toLowerCase();
    if (HOP_BY_HOP_HEADERS.has(lower) || lower === "set-cookie") {
      return;
    }
    headers.set(key, value);
  });

  const setCookies =
    typeof upstream.headers.getSetCookie === "function"
      ? upstream.headers.getSetCookie()
      : [];
  if (setCookies.length > 0) {
    for (const cookie of setCookies) {
      headers.append("set-cookie", cookie);
    }
  } else {
    const single = upstream.headers.get("set-cookie");
    if (single) {
      headers.append("set-cookie", single);
    }
  }
  return headers;
}

/**
 * Proxies an incoming App Router request to `{BACKEND_URL}/…path`.
 * Returns 503 when BACKEND_URL is unset (fail closed; no silent public-env host).
 */
export async function proxyApiRequest(
  request: Request,
  pathSegments: string[],
): Promise<Response> {
  const base = resolveBackendBaseUrl();
  if (!base) {
    console.error("[backend-proxy] BACKEND_URL is not configured");
    return Response.json(
      { error: "Backend proxy is not configured" },
      { status: 503 },
    );
  }

  const upstreamUrl = new URL(joinUpstreamUrl(base, pathSegments));
  upstreamUrl.search = new URL(request.url).search;

  const init: RequestInit = {
    method: request.method,
    headers: copyRequestHeaders(request),
    redirect: "manual",
  };

  if (request.method !== "GET" && request.method !== "HEAD") {
    init.body = await request.arrayBuffer();
  }

  let upstream: Response;
  try {
    upstream = await fetch(upstreamUrl.toString(), init);
  } catch (error) {
    const message = error instanceof Error ? error.message : "unknown error";
    console.error("[backend-proxy] upstream fetch failed:", message);
    return Response.json({ error: "Upstream unavailable" }, { status: 502 });
  }

  return new Response(upstream.body, {
    status: upstream.status,
    statusText: upstream.statusText,
    headers: copyResponseHeaders(upstream),
  });
}
