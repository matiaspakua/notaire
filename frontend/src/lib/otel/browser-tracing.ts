/**
 * Browser OpenTelemetry tracing (#1739 / CU77).
 * Opt-in via NEXT_PUBLIC_OTEL_TRACES_ENABLED=true. Exports to same-origin
 * /api/otlp/v1/traces (BFF → collector). No PII attributes — only resource
 * service.name / service.version and standard fetch span names.
 */

export const FRONTEND_SERVICE_NAME = "notaire-frontend";
export const BROWSER_OTLP_PATH = "/api/otlp/v1/traces";

let started = false;

export function isBrowserOtelEnabled(
  env: NodeJS.ProcessEnv | Record<string, string | undefined> = typeof process !==
  "undefined"
    ? process.env
    : {},
): boolean {
  return env.NEXT_PUBLIC_OTEL_TRACES_ENABLED === "true";
}

export function browserOtlpUrl(): string {
  if (typeof window === "undefined") {
    return BROWSER_OTLP_PATH;
  }
  return `${window.location.origin}${BROWSER_OTLP_PATH}`;
}

/**
 * Starts WebTracerProvider + fetch instrumentation once. Safe to call from a
 * client component; no-ops when disabled or already started.
 *
 * Prefer `enabled` from a server layout prop so Docker Compose can toggle
 * `NEXT_PUBLIC_OTEL_TRACES_ENABLED` at runtime without rebuilding the image
 * (client bundles otherwise inline NEXT_PUBLIC_* at build time).
 */
export async function startBrowserTracing(options?: {
  enabled?: boolean;
}): Promise<boolean> {
  if (typeof window === "undefined") {
    return false;
  }
  const enabled = options?.enabled ?? isBrowserOtelEnabled();
  if (!enabled) {
    return false;
  }
  if (started) {
    return true;
  }

  const { WebTracerProvider } = await import("@opentelemetry/sdk-trace-web");
  const { BatchSpanProcessor } = await import("@opentelemetry/sdk-trace-base");
  const { OTLPTraceExporter } = await import(
    "@opentelemetry/exporter-trace-otlp-http"
  );
  const { resourceFromAttributes } = await import("@opentelemetry/resources");
  const { ATTR_SERVICE_NAME, ATTR_SERVICE_VERSION } = await import(
    "@opentelemetry/semantic-conventions"
  );
  const { registerInstrumentations } = await import(
    "@opentelemetry/instrumentation"
  );
  const { FetchInstrumentation } = await import(
    "@opentelemetry/instrumentation-fetch"
  );

  const version =
    process.env.NEXT_PUBLIC_APP_VERSION?.trim() ||
    process.env.npm_package_version ||
    "0.0.0";

  const exporter = new OTLPTraceExporter({
    url: browserOtlpUrl(),
  });

  const provider = new WebTracerProvider({
    resource: resourceFromAttributes({
      [ATTR_SERVICE_NAME]: FRONTEND_SERVICE_NAME,
      [ATTR_SERVICE_VERSION]: version,
    }),
    spanProcessors: [new BatchSpanProcessor(exporter)],
  });

  provider.register();

  registerInstrumentations({
    instrumentations: [
      new FetchInstrumentation({
        // Propagate W3C traceparent on same-origin API calls for FE↔BE links.
        propagateTraceHeaderCorsUrls: [/.*/],
        clearTimingResources: true,
        ignoreUrls: [/\/api\/otlp\//, /\/api\/client-errors/, /\/api\/metrics/],
      }),
    ],
  });

  started = true;
  return true;
}

/** Test-only reset. */
export function __resetBrowserTracingForTests(): void {
  started = false;
}
