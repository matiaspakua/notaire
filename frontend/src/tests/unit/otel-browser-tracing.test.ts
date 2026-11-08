import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const otelMocks = vi.hoisted(() => {
  const contextManager = { enable: vi.fn() };
  contextManager.enable.mockReturnValue(contextManager);

  const provider = {};
  const processor = {};
  const exporter = {};
  const resource = {};
  const fetchInstrumentation = {};
  const propagator = {};

  return {
    batchSpanProcessor: vi.fn(function () {
      return processor;
    }),
    compositePropagator: vi.fn(function () {
      return propagator;
    }),
    contextManager,
    fetchInstrumentation: vi.fn(function () {
      return fetchInstrumentation;
    }),
    registerInstrumentations: vi.fn(),
    resourceFromAttributes: vi.fn(function () {
      return resource;
    }),
    setGlobalContextManager: vi.fn(),
    setGlobalPropagator: vi.fn(),
    setGlobalTracerProvider: vi.fn(),
    traceExporter: vi.fn(function () {
      return exporter;
    }),
    tracerProvider: vi.fn(function () {
      return provider;
    }),
    w3cBaggagePropagator: vi.fn(function () {
      return {};
    }),
    w3cTraceContextPropagator: vi.fn(function () {
      return {};
    }),
  };
});

vi.mock("@opentelemetry/api", () => ({
  context: { setGlobalContextManager: otelMocks.setGlobalContextManager },
  propagation: { setGlobalPropagator: otelMocks.setGlobalPropagator },
  trace: { setGlobalTracerProvider: otelMocks.setGlobalTracerProvider },
}));

vi.mock("@opentelemetry/core", () => ({
  CompositePropagator: otelMocks.compositePropagator,
  W3CBaggagePropagator: otelMocks.w3cBaggagePropagator,
  W3CTraceContextPropagator: otelMocks.w3cTraceContextPropagator,
}));

vi.mock("@opentelemetry/exporter-trace-otlp-http", () => ({
  OTLPTraceExporter: otelMocks.traceExporter,
}));

vi.mock("@opentelemetry/instrumentation", () => ({
  registerInstrumentations: otelMocks.registerInstrumentations,
}));

vi.mock("@opentelemetry/instrumentation-fetch", () => ({
  FetchInstrumentation: otelMocks.fetchInstrumentation,
}));

vi.mock("@opentelemetry/resources", () => ({
  resourceFromAttributes: otelMocks.resourceFromAttributes,
}));

vi.mock("@opentelemetry/sdk-trace", () => ({
  BatchSpanProcessor: otelMocks.batchSpanProcessor,
  StackContextManager: vi.fn(function () {
    return otelMocks.contextManager;
  }),
  TracerProvider: otelMocks.tracerProvider,
}));

vi.mock("@opentelemetry/semantic-conventions", () => ({
  ATTR_SERVICE_NAME: "service.name",
  ATTR_SERVICE_VERSION: "service.version",
}));

describe("browser OTel tracing (#1739)", () => {
  const originalEnv = { ...process.env };

  beforeEach(() => {
    vi.resetModules();
    vi.clearAllMocks();
    process.env = { ...originalEnv };
    delete process.env.NEXT_PUBLIC_OTEL_TRACES_ENABLED;
  });

  afterEach(() => {
    process.env = { ...originalEnv };
  });

  it("is disabled unless NEXT_PUBLIC_OTEL_TRACES_ENABLED=true", async () => {
    const {
      isBrowserOtelEnabled,
      FRONTEND_SERVICE_NAME,
      BROWSER_OTLP_PATH,
      startBrowserTracing,
      __resetBrowserTracingForTests,
    } = await import("@/lib/otel/browser-tracing");

    __resetBrowserTracingForTests();
    expect(isBrowserOtelEnabled({})).toBe(false);
    expect(isBrowserOtelEnabled({ NEXT_PUBLIC_OTEL_TRACES_ENABLED: "true" })).toBe(
      true,
    );
    expect(FRONTEND_SERVICE_NAME).toBe("notaire-frontend");
    expect(BROWSER_OTLP_PATH).toBe("/api/otlp/v1/traces");

    process.env.NEXT_PUBLIC_OTEL_TRACES_ENABLED = "false";
    await expect(startBrowserTracing()).resolves.toBe(false);
    await expect(startBrowserTracing({ enabled: false })).resolves.toBe(false);
  });

  it("registers the v3 browser tracing globals once when enabled", async () => {
    const { __resetBrowserTracingForTests, startBrowserTracing } = await import(
      "@/lib/otel/browser-tracing"
    );

    __resetBrowserTracingForTests();

    await expect(startBrowserTracing({ enabled: true })).resolves.toBe(true);
    await expect(startBrowserTracing({ enabled: true })).resolves.toBe(true);

    expect(otelMocks.batchSpanProcessor).toHaveBeenCalledWith({
      exporter: expect.any(Object),
    });
    expect(otelMocks.tracerProvider).toHaveBeenCalledWith({
      resource: expect.any(Object),
      spanProcessors: [expect.any(Object)],
    });
    expect(otelMocks.setGlobalTracerProvider).toHaveBeenCalledTimes(1);
    expect(otelMocks.contextManager.enable).toHaveBeenCalledTimes(1);
    expect(otelMocks.setGlobalContextManager).toHaveBeenCalledWith(
      otelMocks.contextManager,
    );
    expect(otelMocks.compositePropagator).toHaveBeenCalledTimes(1);
    expect(otelMocks.setGlobalPropagator).toHaveBeenCalledTimes(1);
    expect(otelMocks.registerInstrumentations).toHaveBeenCalledTimes(1);
  });
});
