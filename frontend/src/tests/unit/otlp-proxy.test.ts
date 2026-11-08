import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

describe("OTLP traces BFF proxy (#1739)", () => {
  const originalEnv = { ...process.env };

  beforeEach(() => {
    vi.resetModules();
    process.env = { ...originalEnv };
    delete process.env.OTEL_EXPORTER_OTLP_TRACES_ENDPOINT;
  });

  afterEach(() => {
    process.env = { ...originalEnv };
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("resolves collector URL from server env only", async () => {
    process.env.OTEL_EXPORTER_OTLP_TRACES_ENDPOINT =
      "http://devsecops-otel-collector:4318/v1/traces/";
    const { resolveOtlpTracesEndpoint } = await import("@/lib/otel/otlp-proxy");
    expect(resolveOtlpTracesEndpoint()).toBe(
      "http://devsecops-otel-collector:4318/v1/traces",
    );
  });

  it("forwards POST body to the collector endpoint", async () => {
    process.env.OTEL_EXPORTER_OTLP_TRACES_ENDPOINT =
      "http://collector.test/v1/traces";
    const fetchMock = vi.fn().mockResolvedValue(
      new Response("{}", { status: 200, headers: { "content-type": "application/json" } }),
    );
    vi.stubGlobal("fetch", fetchMock);

    const { proxyOtlpTraces } = await import("@/lib/otel/otlp-proxy");
    const body = JSON.stringify({ resourceSpans: [] });
    const response = await proxyOtlpTraces(
      new Request("http://localhost:3000/api/otlp/v1/traces", {
        method: "POST",
        headers: { "content-type": "application/json" },
        body,
      }),
    );

    expect(response.status).toBe(200);
    expect(fetchMock).toHaveBeenCalledOnce();
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toBe("http://collector.test/v1/traces");
    expect(init.method).toBe("POST");
  });

  it("returns 503 when the collector is unreachable", async () => {
    process.env.OTEL_EXPORTER_OTLP_TRACES_ENDPOINT = "http://collector.test/v1/traces";
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("ECONNREFUSED")));

    const { proxyOtlpTraces } = await import("@/lib/otel/otlp-proxy");
    const response = await proxyOtlpTraces(
      new Request("http://localhost:3000/api/otlp/v1/traces", {
        method: "POST",
        body: "{}",
      }),
    );
    expect(response.status).toBe(503);
  });
});
