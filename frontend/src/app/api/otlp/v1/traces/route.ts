/**
 * Same-origin OTLP/HTTP traces ingress for the browser (#1739).
 * Forwards to OTEL_EXPORTER_OTLP_TRACES_ENDPOINT (collector) at request time.
 */
import { proxyOtlpTraces } from "@/lib/otel/otlp-proxy";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

export async function POST(request: Request): Promise<Response> {
  return proxyOtlpTraces(request);
}
