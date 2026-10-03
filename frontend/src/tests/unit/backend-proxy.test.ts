/**
 * Unit tests for request-time BFF proxy helper (issue #1055 / CU78).
 * TDD: these fail until backend-proxy.ts + Route Handler land.
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

describe("backend-proxy helper (issue #1055)", () => {
  const originalEnv = { ...process.env };

  beforeEach(() => {
    vi.resetModules();
    process.env = { ...originalEnv };
    delete process.env.BACKEND_URL;
    delete process.env.NEXT_PUBLIC_API_URL;
  });

  afterEach(() => {
    process.env = { ...originalEnv };
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("resolves upstream from runtime BACKEND_URL (not NEXT_PUBLIC_API_URL)", async () => {
    process.env.BACKEND_URL = "http://backend:8080/api/v1";
    process.env.NEXT_PUBLIC_API_URL = "http://leaked.example/api/v1";

    const { resolveBackendBaseUrl, joinUpstreamUrl } = await import(
      "@/lib/backend-proxy"
    );

    expect(resolveBackendBaseUrl()).toBe("http://backend:8080/api/v1");
    expect(joinUpstreamUrl(resolveBackendBaseUrl()!, ["usuarios", "login"])).toBe(
      "http://backend:8080/api/v1/usuarios/login",
    );
  });

  it("fails safely when BACKEND_URL is missing (no NEXT_PUBLIC fallback)", async () => {
    process.env.NEXT_PUBLIC_API_URL = "http://backend:8080/api/v1";

    const { resolveBackendBaseUrl, proxyApiRequest } = await import(
      "@/lib/backend-proxy"
    );

    expect(resolveBackendBaseUrl()).toBeNull();

    const response = await proxyApiRequest(
      new Request("http://localhost:3000/api/v1/people", { method: "GET" }),
      ["people"],
    );
    expect(response.status).toBeGreaterThanOrEqual(500);
    expect(response.status).toBeLessThan(600);
  });

  it("retargets when runtime BACKEND_URL changes without rebuild", async () => {
    const { resolveBackendBaseUrl, joinUpstreamUrl } = await import(
      "@/lib/backend-proxy"
    );

    process.env.BACKEND_URL = "http://env-a:8080/api/v1";
    expect(joinUpstreamUrl(resolveBackendBaseUrl()!, ["health"])).toBe(
      "http://env-a:8080/api/v1/health",
    );

    process.env.BACKEND_URL = "http://env-b:9090/api/v1";
    expect(joinUpstreamUrl(resolveBackendBaseUrl()!, ["health"])).toBe(
      "http://env-b:9090/api/v1/health",
    );
  });

  it("forwards Cookie header upstream", async () => {
    process.env.BACKEND_URL = "http://upstream.test/api/v1";
    const fetchMock = vi.fn().mockResolvedValue(
      new Response("{}", {
        status: 200,
        headers: { "content-type": "application/json" },
      }),
    );
    vi.stubGlobal("fetch", fetchMock);

    const { proxyApiRequest } = await import("@/lib/backend-proxy");
    await proxyApiRequest(
      new Request("http://localhost:3000/api/v1/gestiones", {
        method: "GET",
        headers: { cookie: "notaire-auth-token=secret-jwt; other=1" },
      }),
      ["gestiones"],
    );

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const upstreamInit = fetchMock.mock.calls[0][1] as RequestInit;
    const headers = new Headers(upstreamInit.headers);
    expect(headers.get("cookie")).toBe("notaire-auth-token=secret-jwt; other=1");
    expect(fetchMock.mock.calls[0][0]).toBe("http://upstream.test/api/v1/gestiones");
  });

  it("relays Set-Cookie from upstream to the browser response", async () => {
    process.env.BACKEND_URL = "http://upstream.test/api/v1";
    const upstream = new Response('{"valido":true}', {
      status: 200,
      headers: {
        "content-type": "application/json",
        "set-cookie":
          "notaire-auth-token=abc; Path=/; HttpOnly; SameSite=Lax",
      },
    });
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(upstream));

    const { proxyApiRequest } = await import("@/lib/backend-proxy");
    const response = await proxyApiRequest(
      new Request("http://localhost:3000/api/v1/usuarios/login", {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ name: "admin", password: "admin" }),
      }),
      ["usuarios", "login"],
    );

    const setCookies =
      typeof response.headers.getSetCookie === "function"
        ? response.headers.getSetCookie()
        : [response.headers.get("set-cookie")].filter(Boolean);
    expect(setCookies.some((c) => c?.includes("notaire-auth-token=abc"))).toBe(
      true,
    );
    expect(setCookies.some((c) => c?.toLowerCase().includes("httponly"))).toBe(
      true,
    );
  });
});
