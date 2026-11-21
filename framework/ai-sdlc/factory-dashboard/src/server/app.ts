import { existsSync } from "node:fs";
import { join } from "node:path";

import fastifyStatic from "@fastify/static";
import Fastify, { type FastifyInstance, type FastifyReply, type FastifyRequest } from "fastify";

import type { DashboardConfig, Workspace } from "./config.ts";
import { COOKIE, type Grant, SessionStore, sameSecret } from "./sessions.ts";
import { callKit, callKnowledge, type Fetch, problem, type UpstreamReply } from "./upstream.ts";

export interface AppOptions {
  config: DashboardConfig;
  fetcher?: Fetch;
  staticRoot?: string;
}

const CSRF_HEADER = "x-dashboard-csrf";
const FACTORY_PATH = /^\/factory(\/[A-Za-z0-9_~-][A-Za-z0-9._~-]*)*$/;
const KNOWLEDGE: Record<string, string> = {
  search: "/api/search",
  orphans: "/api/orphans",
  metrics: "/api/metrics",
  stats: "/api/stats",
  usage: "/api/metrics/usage",
};
const CSP = [
  "default-src 'self'",
  "script-src 'self'",
  "style-src 'self'",
  "img-src 'self' data:",
  "connect-src 'self'",
  "base-uri 'none'",
  "form-action 'self'",
  "frame-ancestors 'none'",
].join("; ");

function cookieValue(header: string | undefined, name: string): string | undefined {
  for (const part of (header ?? "").split(";")) {
    const [key, ...rest] = part.trim().split("=");
    if (key === name) {
      return rest.join("=");
    }
  }
  return undefined;
}

function send(reply: FastifyReply, upstream: UpstreamReply): FastifyReply {
  const failed = upstream.status >= 400;
  return reply
    .code(upstream.status)
    .header("Content-Type", failed ? "application/problem+json" : "application/json")
    .send(JSON.stringify(upstream.body ?? null));
}

function split(rawUrl: string, prefix: string): { path: string; query: string } {
  const [path, query = ""] = rawUrl.split("?", 2);
  return { path: path.slice(prefix.length), query };
}

function asGrant(body: unknown): Grant | undefined {
  if (typeof body !== "object" || body === null) {
    return undefined;
  }
  const data = body as Record<string, unknown>;
  if (typeof data.identity !== "string" || !["read", "write", "owner"].includes(String(data.scope))) {
    return undefined;
  }
  return {
    identity: data.identity,
    scope: data.scope as Grant["scope"],
    actor: String(data.actor ?? data.identity),
    write_enabled: data.write_enabled === true,
  };
}

export function buildApp({ config, fetcher = fetch, staticRoot }: AppOptions): FastifyInstance {
  const app = Fastify({ logger: false, bodyLimit: 256 * 1024 });
  const sessions = new SessionStore(config.sessionTtlMinutes * 60_000);
  const byId = new Map(config.workspaces.map((ws) => [ws.id, ws]));
  const cookieFlags = `Path=/; HttpOnly; SameSite=Strict${config.secureCookies ? "; Secure" : ""}`;

  app.addHook("onSend", async (_request, reply) => {
    reply.header("Content-Security-Policy", CSP);
    reply.header("X-Content-Type-Options", "nosniff");
    reply.header("Referrer-Policy", "no-referrer");
    reply.header("Cache-Control", "no-store");
  });

  const sessionOf = (request: FastifyRequest) => sessions.get(cookieValue(request.headers.cookie, COOKIE));

  function workspaceOf(request: FastifyRequest, reply: FastifyReply): Workspace | undefined {
    const ws = byId.get((request.params as { ws: string }).ws);
    if (!ws) {
      send(reply, problem(404, "unknown_workspace", "no workspace with that id is configured"));
    }
    return ws;
  }

  function csrfOk(request: FastifyRequest, reply: FastifyReply): boolean {
    const session = sessionOf(request);
    const presented = request.headers[CSRF_HEADER];
    if (!session || !sameSecret(typeof presented === "string" ? presented : undefined, session.csrf)) {
      send(reply, problem(403, "csrf_required", `writes require the ${CSRF_HEADER} header from /api/session`));
      return false;
    }
    return true;
  }

  function tokenFor(request: FastifyRequest, reply: FastifyReply, ws: Workspace): string | undefined {
    const token = sessions.token(sessionOf(request), ws.id);
    if (!token) {
      send(reply, problem(401, "sign_in_required", `sign in to ${ws.label} with an aisdlc serve token`));
    }
    return token;
  }

  app.get("/api/workspaces", async () =>
    config.workspaces.map((ws) => ({ id: ws.id, label: ws.label, knowledge: Boolean(ws.knowledge) })),
  );

  app.get("/api/session", async (request) => sessions.view(sessionOf(request)));

  app.post("/api/workspaces/:ws/session", async (request, reply) => {
    const ws = workspaceOf(request, reply);
    if (!ws) {
      return reply;
    }
    const token = String((request.body as { token?: unknown } | null)?.token ?? "").trim();
    if (!token) {
      return send(reply, problem(400, "token_required", "paste the token that `aisdlc serve-token add` printed"));
    }
    const answer = await callKit(fetcher, ws.kit, { method: "GET", path: "/factory/session", token });
    const grant = answer.status === 200 ? asGrant(answer.body) : undefined;
    if (!grant) {
      return send(reply, answer.status === 200 ? problem(502, "kit_incompatible", "kit has no /factory/session") : answer);
    }
    const session = sessions.open(cookieValue(request.headers.cookie, COOKIE));
    sessions.grant(session, ws.id, grant, token);
    reply.header("Set-Cookie", `${COOKIE}=${session.id}; ${cookieFlags}; Max-Age=${config.sessionTtlMinutes * 60}`);
    return grant;
  });

  app.delete("/api/workspaces/:ws/session", async (request, reply) => {
    const ws = workspaceOf(request, reply);
    if (!ws || !csrfOk(request, reply)) {
      return reply;
    }
    const session = sessionOf(request);
    if (session) {
      sessions.revoke(session, ws.id);
    }
    return reply.code(204).send();
  });

  const proxy = async (request: FastifyRequest, reply: FastifyReply) => {
    const ws = workspaceOf(request, reply);
    if (!ws) {
      return reply;
    }
    const { path, query } = split(request.raw.url ?? "", `/api/workspaces/${ws.id}`);
    if (!FACTORY_PATH.test(path)) {
      return send(reply, problem(400, "invalid_path", "only /factory paths of the kit contract are proxied"));
    }
    const method = request.method as "GET" | "POST" | "PUT";
    if (method !== "GET" && !csrfOk(request, reply)) {
      return reply;
    }
    const token = tokenFor(request, reply, ws);
    if (!token) {
      return reply;
    }
    const ifMatch = request.headers["if-match"];
    return send(
      reply,
      await callKit(fetcher, ws.kit, {
        method, path, query, token, body: request.body, ifMatch: typeof ifMatch === "string" ? ifMatch : undefined,
      }),
    );
  };
  app.route({ method: ["GET", "POST", "PUT"], url: "/api/workspaces/:ws/factory/*", handler: proxy });

  app.get("/api/workspaces/:ws/knowledge/:endpoint", async (request, reply) => {
    const ws = workspaceOf(request, reply);
    if (!ws) {
      return reply;
    }
    const target = KNOWLEDGE[(request.params as { endpoint: string }).endpoint];
    if (!target) {
      return send(reply, problem(404, "not_found", "knowledge offers search, orphans, metrics, stats and usage"));
    }
    if (!tokenFor(request, reply, ws)) {
      return reply;
    }
    if (!ws.knowledge) {
      return send(reply, problem(404, "knowledge_not_configured", `${ws.label} has no RepoLens configured`));
    }
    const { query } = split(request.raw.url ?? "", "");
    return send(reply, await callKnowledge(fetcher, ws.knowledge, target, query));
  });

  const root = staticRoot ?? join(import.meta.dirname, "../../dist/ui");
  if (existsSync(join(root, "index.html"))) {
    app.register(fastifyStatic, { root, wildcard: false });
    app.setNotFoundHandler((request, reply) => {
      if (request.method === "GET" && !request.url.startsWith("/api/")) {
        return reply.type("text/html").sendFile("index.html");
      }
      return send(reply, problem(404, "not_found", "no such route"));
    });
  } else {
    app.setNotFoundHandler((_request, reply) => send(reply, problem(404, "not_found", "no such route")));
  }
  return app;
}
