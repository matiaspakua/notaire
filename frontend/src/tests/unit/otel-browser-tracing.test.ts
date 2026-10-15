import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

describe("browser OTel tracing (#1739)", () => {
  const originalEnv = { ...process.env };

  beforeEach(() => {
    vi.resetModules();
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
});
