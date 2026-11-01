/**
 * The demo's fetch shim (#1985): GETs replay recorded fixtures, writes are refused without a
 * network call, and nothing ever answers 401 (a 401 would trigger the app's session-expiry
 * redirect to /login, outside the /notaire/demo base path).
 */
import { readFileSync } from "node:fs";
import path from "node:path";
import vm from "node:vm";
import { beforeEach, describe, expect, it, vi } from "vitest";

type DemoFetch = (input: string | URL | Request, init?: RequestInit) => Promise<Response>;
type Shim = { createDemoFetch: (load: () => Promise<unknown>, real: typeof fetch) => DemoFetch };

const SRC = readFileSync(path.resolve(__dirname, "../demo/demo-shim.js"), "utf8");
const FIXTURES = {
  recordedAt: "2026-11-01T10:00:00Z",
  responses: {
    "GET /people?page=0&size=20": { status: 200, body: { content: [{ idPerson: 1 }] } },
    "GET /people": { status: 200, body: [{ idPerson: 9 }] },
    "GET /concepts": { status: 200, body: [{ id: 3 }] },
    "POST /users/login": { status: 200, body: { idUser: 1, name: "admin", token: "demo" } },
  },
};

function loadShim(): Shim {
  const sandbox: Record<string, unknown> = { Response, URL, Request, Headers, location: { origin: "https://example.test" } };
  vm.runInNewContext(SRC, sandbox);
  return sandbox.NotaireDemo as Shim;
}

let real: ReturnType<typeof vi.fn>;
let demoFetch: DemoFetch;
beforeEach(() => {
  real = vi.fn(async () => new Response("static"));
  demoFetch = loadShim().createDemoFetch(async () => FIXTURES, real as unknown as typeof fetch);
});

describe("demo fetch shim", () => {
  it("replays a GET with its exact query", async () => {
    const res = await demoFetch("/api/v1/people?page=0&size=20");
    expect(res.status).toBe(200);
    expect(await res.json()).toEqual({ content: [{ idPerson: 1 }] });
  });

  it("falls back to the path when the query was not recorded", async () => {
    const res = await demoFetch("/api/v1/people?page=3&size=20");
    expect(await res.json()).toEqual([{ idPerson: 9 }]);
  });

  it("answers an unrecorded GET with 404, never 401", async () => {
    const res = await demoFetch("/api/v1/nothing-here");
    expect(res.status).toBe(404);
  });

  it("signs in with any credentials", async () => {
    const res = await demoFetch("/api/v1/users/login", { method: "POST", body: JSON.stringify({ name: "x", password: "y" }) });
    expect(res.status).toBe(200);
    expect((await res.json()).name).toBe("admin");
  });

  it.each(["POST", "PUT", "PATCH", "DELETE"])("refuses %s without a network call", async (method) => {
    const res = await demoFetch("/api/v1/people/1", { method, body: "{}" });
    expect(res.status).toBe(403);
    expect((await res.json()).message).toMatch(/read-only/i);
    expect(real).not.toHaveBeenCalled();
  });

  it("treats logout and client error beacons as no-ops", async () => {
    expect((await demoFetch("/api/v1/users/logout", { method: "POST" })).ok).toBe(true);
    expect((await demoFetch("/api/client-errors", { method: "POST", body: "{}" })).ok).toBe(true);
    expect(real).not.toHaveBeenCalled();
  });

  it("reads the method from a Request object", async () => {
    const res = await demoFetch(new Request("https://example.test/api/v1/concepts", { method: "DELETE" }));
    expect(res.status).toBe(403);
  });

  it("passes non-API requests to the network", async () => {
    await demoFetch("/notaire/demo/_next/static/chunk.js");
    expect(real).toHaveBeenCalledTimes(1);
  });
});
