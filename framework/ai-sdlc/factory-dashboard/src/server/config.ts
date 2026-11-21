import { readFileSync } from "node:fs";

/** One `aisdlc serve` (and optionally one RepoLens) per workspace: projects, repositories and machines combine
 * without shared storage. The dashboard never reads a workspace's files; it only speaks the two HTTP contracts. */
export interface Workspace {
  id: string;
  label: string;
  kit: string;
  knowledge?: string;
}

export interface DashboardConfig {
  host: string;
  port: number;
  allowRemote: boolean;
  secureCookies: boolean;
  sessionTtlMinutes: number;
  workspaces: Workspace[];
}

const ID = /^[a-z0-9][a-z0-9._-]{0,63}$/;
const LOOPBACK = new Set(["127.0.0.1", "::1", "localhost"]);

export class ConfigError extends Error {}

function httpUrl(value: unknown, where: string): string {
  if (typeof value !== "string") {
    throw new ConfigError(`${where}: expected an http(s) URL`);
  }
  let url: URL;
  try {
    url = new URL(value);
  } catch {
    throw new ConfigError(`${where}: ${JSON.stringify(value)} is not a URL`);
  }
  if (url.protocol !== "http:" && url.protocol !== "https:") {
    throw new ConfigError(`${where}: only http and https are allowed`);
  }
  return url.origin + url.pathname.replace(/\/+$/, "");
}

function workspace(raw: unknown, index: number): Workspace {
  if (typeof raw !== "object" || raw === null) {
    throw new ConfigError(`workspaces[${index}]: expected an object`);
  }
  const entry = raw as Record<string, unknown>;
  const id = String(entry.id ?? "");
  if (!ID.test(id)) {
    throw new ConfigError(`workspaces[${index}].id: lowercase letters, digits, dot, dash or underscore`);
  }
  const knowledge = entry.knowledge === undefined ? undefined : httpUrl(entry.knowledge, `${id}.knowledge`);
  return {
    id,
    label: typeof entry.label === "string" && entry.label.trim() ? entry.label.trim() : id,
    kit: httpUrl(entry.kit, `${id}.kit`),
    ...(knowledge ? { knowledge } : {}),
  };
}

export function parseConfig(raw: unknown, env: NodeJS.ProcessEnv = {}): DashboardConfig {
  const data = (typeof raw === "object" && raw !== null ? raw : {}) as Record<string, unknown>;
  const listen = (data.listen ?? {}) as Record<string, unknown>;
  const list = Array.isArray(data.workspaces) ? data.workspaces : [];
  const workspaces = list.map(workspace);
  if (workspaces.length === 0) {
    workspaces.push(
      workspace(
        {
          id: "local",
          label: env.FACTORY_DASHBOARD_LABEL ?? "Local",
          kit: env.AISDLC_SERVE_URL ?? "http://127.0.0.1:8765",
          ...(env.REPOLENS_URL ? { knowledge: env.REPOLENS_URL } : {}),
        },
        0,
      ),
    );
  }
  const ids = new Set<string>();
  for (const ws of workspaces) {
    if (ids.has(ws.id)) {
      throw new ConfigError(`workspace id ${ws.id} is used twice`);
    }
    ids.add(ws.id);
  }
  const host = String(env.FACTORY_DASHBOARD_HOST ?? listen.host ?? "127.0.0.1");
  const allowRemote = data.allowRemote === true;
  if (!LOOPBACK.has(host) && !allowRemote) {
    throw new ConfigError(`refusing to listen on ${host}: set "allowRemote": true and serve it behind TLS`);
  }
  return {
    host,
    port: Number(env.FACTORY_DASHBOARD_PORT ?? listen.port ?? 8766),
    allowRemote,
    secureCookies: data.secureCookies === true || allowRemote,
    sessionTtlMinutes: Number(data.sessionTtlMinutes ?? 720),
    workspaces,
  };
}

export function loadConfig(env: NodeJS.ProcessEnv = process.env): DashboardConfig {
  const path = env.FACTORY_DASHBOARD_CONFIG;
  if (!path) {
    return parseConfig({}, env);
  }
  let raw: unknown;
  try {
    raw = JSON.parse(readFileSync(path, "utf8"));
  } catch (error) {
    throw new ConfigError(`cannot read ${path}: ${(error as Error).message}`);
  }
  return parseConfig(raw, env);
}
