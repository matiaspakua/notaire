/**
 * Server-only OTLP trace proxy (#1739 / CU77).
 * Browser posts to same-origin /api/otlp/v1/traces; this forwards to the collector
 * so clients never resolve Docker hostnames (same pattern as BACKEND_URL BFF).
 */

import { logger } from "@/lib/logger";

const DEFAULT_COLLECTOR =
  "http://devsecops-otel-collector:4318/v1/traces";

/**
 * Resolves the collector OTLP/HTTP traces URL from server env only.
 * Not exposed as NEXT_PUBLIC_* (browser uses the same-origin BFF path).
 */
export function resolveOtlpTracesEndpoint(
  env: NodeJS.ProcessEnv = process.env,
): string | null {
  const raw = env.OTEL_EXPORTER_OTLP_TRACES_ENDPOINT?.trim();
  if (!raw) {
    return null;
  }
  return raw.replace(/\/+$/, "");
}

export function defaultOtlpTracesEndpointForCompose(): string {
  return DEFAULT_COLLECTOR;
}

export async function proxyOtlpTraces(
  request: Request,
  env: NodeJS.ProcessEnv = process.env,
): Promise<Response> {
  const upstream =
    resolveOtlpTracesEndpoint(env) ?? defaultOtlpTracesEndpointForCompose();

  const body = await request.arrayBuffer();
  const contentType =
    request.headers.get("content-type") ?? "application/json";

  try {
    const upstreamResponse = await fetch(upstream, {
      method: "POST",
      headers: {
        "content-type": contentType,
      },
      body,
      // Node fetch against Docker DNS — no browser credentials.
      cache: "no-store",
    });
    return new Response(upstreamResponse.body, {
      status: upstreamResponse.status,
      headers: {
        "content-type":
          upstreamResponse.headers.get("content-type") ?? "application/json",
      },
    });
  } catch (error) {
    logger.warn("otlp_proxy_unreachable", {
      upstream,
      reason: error instanceof Error ? error.message : String(error),
    });
    return new Response(JSON.stringify({ error: "otlp_collector_unreachable" }), {
      status: 503,
      headers: { "content-type": "application/json" },
    });
  }
}
