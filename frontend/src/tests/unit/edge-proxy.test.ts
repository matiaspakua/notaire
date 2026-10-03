import { describe, expect, it } from "vitest";
import { NextRequest } from "next/server";
import {
  AUTH_ROLE_COOKIE,
  AUTH_STATUS_COOKIE,
  forbiddenDashboardPath,
} from "@/lib/admin-access";

/**
 * Edge redirect matrix against the Next 16 `proxy` export (issue #1056 / CU84).
 */
async function loadProxy() {
  const mod = await import("@/proxy");
  expect(typeof mod.proxy).toBe("function");
  return mod.proxy as (req: NextRequest) => Response;
}

function request(path: string, cookies: Record<string, string> = {}): NextRequest {
  const headers = new Headers();
  const cookieHeader = Object.entries(cookies)
    .map(([name, value]) => `${name}=${value}`)
    .join("; ");
  if (cookieHeader) {
    headers.set("cookie", cookieHeader);
  }
  return new NextRequest(new URL(path, "http://localhost:3000"), { headers });
}

function locationOf(response: Response): string {
  return response.headers.get("location") ?? "";
}

describe("edge proxy route guards (issue #1056 / CU84)", () => {
  it("redirects unauthenticated protected routes to /login", async () => {
    const proxy = await loadProxy();
    const response = proxy(request("/dashboard"));
    expect(response.status).toBeGreaterThanOrEqual(300);
    expect(response.status).toBeLessThan(400);
    expect(locationOf(response)).toContain("/login");
    expect(response.headers.get("Content-Security-Policy")).toBeTruthy();
  });

  it("redirects authenticated /login to /dashboard", async () => {
    const proxy = await loadProxy();
    const response = proxy(
      request("/login", { [AUTH_STATUS_COOKIE]: "1", [AUTH_ROLE_COOKIE]: "ESCRIBANO" }),
    );
    expect(locationOf(response)).toContain("/dashboard");
  });

  it("denies non-admin administración routes at the edge", async () => {
    const proxy = await loadProxy();
    const response = proxy(
      request("/dashboard/administracion/usuarios", {
        [AUTH_STATUS_COOKIE]: "1",
        [AUTH_ROLE_COOKIE]: "EMPLEADO",
      }),
    );
    expect(locationOf(response)).toContain(forbiddenDashboardPath());
  });

  it("does not redirect /api/ paths (BFF / HttpOnly cookie path)", async () => {
    const proxy = await loadProxy();
    const response = proxy(request("/api/v1/auth/login"));
    expect(response.status).toBeLessThan(300);
    expect(locationOf(response)).toBe("");
  });

  it("continues authenticated dashboard navigations with CSP nonce", async () => {
    const proxy = await loadProxy();
    const response = proxy(
      request("/dashboard", { [AUTH_STATUS_COOKIE]: "1", [AUTH_ROLE_COOKIE]: "ESCRIBANO" }),
    );
    expect(response.status).toBeLessThan(300);
    // continueWithNonce sets CSP on the response; x-nonce is forwarded on the
    // request headers for RSC (readable via next/headers), not as a response header.
    expect(response.headers.get("Content-Security-Policy")).toMatch(/nonce-/);
  });
});

