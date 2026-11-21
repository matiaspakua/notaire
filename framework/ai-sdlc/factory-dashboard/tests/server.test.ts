import { afterEach, describe, expect, it } from "vitest";
import type { FastifyInstance } from "fastify";

import { buildApp } from "../src/server/app.ts";
import { ConfigError, parseConfig } from "../src/server/config.ts";

interface Seen {
  url: string;
  method: string;
  headers: Record<string, string>;
  body: string | undefined;
}

const TOKENS: Record<string, { identity: string; scope: string }> = {
  "tok-alice": { identity: "alice", scope: "write" },
  "tok-bob": { identity: "bob", scope: "read" },
};

function json(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}

function fakeUpstream(seen: Seen[], { down = false } = {}): typeof fetch {
  return (async (input: string | URL | Request, init?: RequestInit) => {
    const url = String(input);
    const headers = Object.fromEntries(Object.entries((init?.headers ?? {}) as Record<string, string>));
    seen.push({ url, method: init?.method ?? "GET", headers, body: init?.body as string | undefined });
    if (down) {
      throw new TypeError("fetch failed");
    }
    if (url.startsWith("http://lens")) {
      return json(200, [{ id: "n1", kind: "file", name: "a.py", path: "a.py", start: 1, project: "repo-a" }]);
    }
    const token = (headers.Authorization ?? "").replace("Bearer ", "");
    const who = TOKENS[token];
    if (!who) {
      return json(401, { type: "about:blank", title: "unauthorized", status: 401, detail: "invalid token", code: "unauthorized" });
    }
    if (url.endsWith("/factory/session")) {
      return json(200, { ...who, actor: `ui:${who.identity}`, write_enabled: who.scope !== "read" });
    }
    return json(200, { ok: true, path: new URL(url).pathname });
  }) as typeof fetch;
}

const CONFIG = parseConfig({
  workspaces: [
    { id: "alpha", label: "Alpha", kit: "http://kit-a:8765", knowledge: "http://lens-a:8770" },
    { id: "beta", label: "Beta", kit: "http://kit-b:8765" },
  ],
});

let app: FastifyInstance;
let seen: Seen[];

async function start(options: { down?: boolean; secure?: boolean } = {}): Promise<void> {
  seen = [];
  const config = { ...CONFIG, secureCookies: options.secure ?? false };
  app = buildApp({ config, fetcher: fakeUpstream(seen, { down: options.down }) });
  await app.ready();
}

async function signIn(workspace: string, token: string): Promise<string> {
  const reply = await app.inject({ method: "POST", url: `/api/workspaces/${workspace}/session`, payload: { token } });
  expect(reply.statusCode).toBe(200);
  const cookie = String(reply.headers["set-cookie"]).split(";")[0];
  return cookie;
}

async function csrfOf(cookie: string): Promise<string> {
  const reply = await app.inject({ method: "GET", url: "/api/session", headers: { cookie } });
  return reply.json().csrf;
}

afterEach(async () => {
  await app?.close();
});

describe("workspaces", () => {
  it("lists workspaces without upstream URLs or secrets", async () => {
    await start();
    const reply = await app.inject({ method: "GET", url: "/api/workspaces" });
    expect(reply.statusCode).toBe(200);
    expect(reply.json()).toEqual([
      { id: "alpha", label: "Alpha", knowledge: true, reachable: true },
      { id: "beta", label: "Beta", knowledge: false, reachable: true },
    ]);
    expect(reply.body).not.toContain("kit-a");
    expect(seen.every((call) => !call.headers.Authorization)).toBe(true);
  });

  it("marks a workspace whose kit does not answer as unreachable", async () => {
    await start({ down: true });
    const reply = await app.inject({ method: "GET", url: "/api/workspaces" });
    expect(reply.json().map((ws: { reachable: boolean }) => ws.reachable)).toEqual([false, false]);
  });

  it("sends security headers on every answer", async () => {
    await start();
    const reply = await app.inject({ method: "GET", url: "/api/workspaces" });
    expect(reply.headers["content-security-policy"]).toContain("default-src 'self'");
    expect(reply.headers["content-security-policy"]).toContain("frame-ancestors 'none'");
    expect(reply.headers["x-content-type-options"]).toBe("nosniff");
    expect(reply.headers["referrer-policy"]).toBe("no-referrer");
  });
});

describe("sign-in", () => {
  it("checks the token with the kit and keeps it server-side behind an httpOnly cookie", async () => {
    await start({ secure: true });
    const reply = await app.inject({ method: "POST", url: "/api/workspaces/alpha/session", payload: { token: "tok-alice" } });
    expect(reply.statusCode).toBe(200);
    expect(reply.json()).toEqual({ identity: "alice", scope: "write", actor: "ui:alice", write_enabled: true });
    expect(reply.body).not.toContain("tok-alice");
    const cookie = String(reply.headers["set-cookie"]);
    expect(cookie).toMatch(/HttpOnly/i);
    expect(cookie).toMatch(/SameSite=Strict/i);
    expect(cookie).toMatch(/Secure/i);
    expect(seen[0].url).toBe("http://kit-a:8765/factory/session");
    expect(seen[0].headers.Authorization).toBe("Bearer tok-alice");
  });

  it("passes the kit's refusal through and grants nothing", async () => {
    await start();
    const reply = await app.inject({ method: "POST", url: "/api/workspaces/alpha/session", payload: { token: "nope" } });
    expect(reply.statusCode).toBe(401);
    expect(reply.json().code).toBe("unauthorized");
    expect(reply.headers["set-cookie"]).toBeUndefined();
  });

  it("reports grants per workspace and signs out of one", async () => {
    await start();
    const cookie = await signIn("alpha", "tok-alice");
    const view = await app.inject({ method: "GET", url: "/api/session", headers: { cookie } });
    expect(view.json().workspaces).toEqual({
      alpha: { identity: "alice", scope: "write", actor: "ui:alice", write_enabled: true },
    });
    expect(view.body).not.toContain("tok-alice");
    const csrf = view.json().csrf;
    const out = await app.inject({
      method: "DELETE", url: "/api/workspaces/alpha/session", headers: { cookie, "x-dashboard-csrf": csrf },
    });
    expect(out.statusCode).toBe(204);
    const after = await app.inject({ method: "GET", url: "/api/workspaces/alpha/factory/runs", headers: { cookie } });
    expect(after.statusCode).toBe(401);
  });

  it("refuses an unknown workspace", async () => {
    await start();
    const reply = await app.inject({ method: "POST", url: "/api/workspaces/gamma/session", payload: { token: "tok-alice" } });
    expect(reply.statusCode).toBe(404);
    expect(reply.json().code).toBe("unknown_workspace");
  });
});

describe("factory proxy", () => {
  it("needs a sign-in for that workspace", async () => {
    await start();
    const cookie = await signIn("alpha", "tok-alice");
    const reply = await app.inject({ method: "GET", url: "/api/workspaces/beta/factory/runs", headers: { cookie } });
    expect(reply.statusCode).toBe(401);
    expect(reply.json().code).toBe("sign_in_required");
  });

  it("forwards reads with the user's token and the query", async () => {
    await start();
    const cookie = await signIn("alpha", "tok-bob");
    const reply = await app.inject({
      method: "GET", url: "/api/workspaces/alpha/factory/workflows?approved=true", headers: { cookie },
    });
    expect(reply.statusCode).toBe(200);
    const last = seen.at(-1)!;
    expect(last.url).toBe("http://kit-a:8765/factory/workflows?approved=true");
    expect(last.headers.Authorization).toBe("Bearer tok-bob");
  });

  it("requires the dashboard CSRF header on writes and adds the kit's", async () => {
    await start();
    const cookie = await signIn("alpha", "tok-alice");
    const url = "/api/workspaces/alpha/factory/catalogs/providers/openai";
    const refused = await app.inject({ method: "PUT", url, headers: { cookie }, payload: { summary: "x" } });
    expect(refused.statusCode).toBe(403);
    expect(refused.json().code).toBe("csrf_required");
    const csrf = await csrfOf(cookie);
    const reply = await app.inject({
      method: "PUT", url, headers: { cookie, "x-dashboard-csrf": csrf, "if-match": "3" }, payload: { summary: "x" },
    });
    expect(reply.statusCode).toBe(200);
    const last = seen.at(-1)!;
    expect(last.method).toBe("PUT");
    expect(last.headers["X-AISDLC-CSRF"]).toBe("tok-alice");
    expect(last.headers["If-Match"]).toBe("3");
    expect(JSON.parse(last.body!)).toEqual({ summary: "x" });
  });

  it("refuses paths that leave the factory contract", async () => {
    await start();
    const cookie = await signIn("alpha", "tok-alice");
    for (const path of ["factory/../ui", "factory/%2e%2e/ui", "factory/runs%2Fx", "admin"]) {
      const reply = await app.inject({ method: "GET", url: `/api/workspaces/alpha/${path}`, headers: { cookie } });
      expect([400, 404]).toContain(reply.statusCode);
    }
    expect(seen.filter((s) => !s.url.endsWith("/factory/session"))).toEqual([]);
  });

  it("answers kit_unreachable when the kit is down", async () => {
    await start({ down: true });
    const reply = await app.inject({ method: "POST", url: "/api/workspaces/alpha/session", payload: { token: "tok-alice" } });
    expect(reply.statusCode).toBe(502);
    expect(reply.json().code).toBe("kit_unreachable");
  });
});

describe("knowledge", () => {
  it("searches RepoLens for a signed-in user and keeps the repository label", async () => {
    await start();
    const anonymous = await app.inject({ method: "GET", url: "/api/workspaces/alpha/knowledge/search?q=ledger" });
    expect(anonymous.statusCode).toBe(401);
    const cookie = await signIn("alpha", "tok-bob");
    const reply = await app.inject({ method: "GET", url: "/api/workspaces/alpha/knowledge/search?q=ledger&k=5", headers: { cookie } });
    expect(reply.statusCode).toBe(200);
    expect(reply.json()[0].project).toBe("repo-a");
    expect(seen.at(-1)!.url).toBe("http://lens-a:8770/api/search?q=ledger&k=5");
  });

  it("allows only the read endpoints and says when a workspace has no RepoLens", async () => {
    await start();
    const cookie = await signIn("alpha", "tok-bob");
    const index = await app.inject({ method: "GET", url: "/api/workspaces/alpha/knowledge/index", headers: { cookie } });
    expect(index.statusCode).toBe(404);
    const beta = await signIn("beta", "tok-bob");
    const missing = await app.inject({ method: "GET", url: "/api/workspaces/beta/knowledge/stats", headers: { cookie: beta } });
    expect(missing.statusCode).toBe(404);
    expect(missing.json().code).toBe("knowledge_not_configured");
  });
});

describe("config", () => {
  it("falls back to one local workspace from the environment", () => {
    const config = parseConfig({}, { AISDLC_SERVE_URL: "http://127.0.0.1:9000/", REPOLENS_URL: "http://127.0.0.1:8770" });
    expect(config.workspaces).toEqual([
      { id: "local", label: "Local", kit: "http://127.0.0.1:9000", knowledge: "http://127.0.0.1:8770" },
    ]);
    expect(config.host).toBe("127.0.0.1");
  });

  it("refuses remote binds without opt-in, duplicate ids and non-http upstreams", () => {
    expect(() => parseConfig({ listen: { host: "0.0.0.0" } })).toThrow(ConfigError);
    const twice = { workspaces: [{ id: "a", kit: "http://x" }, { id: "a", kit: "http://y" }] };
    expect(() => parseConfig(twice)).toThrow(ConfigError);
    expect(() => parseConfig({ workspaces: [{ id: "a", kit: "file:///etc/passwd" }] })).toThrow(ConfigError);
    expect(parseConfig({ listen: { host: "0.0.0.0" }, allowRemote: true }).secureCookies).toBe(true);
  });
});
