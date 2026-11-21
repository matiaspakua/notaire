import { randomBytes, timingSafeEqual } from "node:crypto";

/** What the browser may know about a workspace sign-in. The kit token itself never leaves the server. */
export interface Grant {
  identity: string;
  scope: "read" | "write" | "owner";
  actor: string;
  write_enabled: boolean;
}

interface Session {
  id: string;
  csrf: string;
  expires: number;
  grants: Map<string, Grant & { token: string }>;
}

export const COOKIE = "fd_sid";
const MAX_SESSIONS = 1_000;

function secret(): string {
  return randomBytes(32).toString("base64url");
}

export function sameSecret(a: string | undefined, b: string): boolean {
  if (!a) {
    return false;
  }
  const left = Buffer.from(a);
  const right = Buffer.from(b);
  return left.length === right.length && timingSafeEqual(left, right);
}

export class SessionStore {
  private readonly sessions = new Map<string, Session>();

  constructor(
    private readonly ttlMs: number,
    private readonly now: () => number = Date.now,
  ) {}

  get(id: string | undefined): Session | undefined {
    if (!id) {
      return undefined;
    }
    const session = this.sessions.get(id);
    if (!session) {
      return undefined;
    }
    if (session.expires <= this.now()) {
      this.sessions.delete(id);
      return undefined;
    }
    session.expires = this.now() + this.ttlMs;
    return session;
  }

  open(id: string | undefined): Session {
    const existing = this.get(id);
    if (existing) {
      return existing;
    }
    this.evict();
    const session: Session = { id: secret(), csrf: secret(), expires: this.now() + this.ttlMs, grants: new Map() };
    this.sessions.set(session.id, session);
    return session;
  }

  grant(session: Session, workspace: string, grant: Grant, token: string): void {
    session.grants.set(workspace, { ...grant, token });
  }

  revoke(session: Session, workspace: string): void {
    session.grants.delete(workspace);
    if (session.grants.size === 0) {
      this.sessions.delete(session.id);
    }
  }

  token(session: Session | undefined, workspace: string): string | undefined {
    return session?.grants.get(workspace)?.token;
  }

  view(session: Session | undefined): { csrf: string | null; workspaces: Record<string, Grant> } {
    const workspaces: Record<string, Grant> = {};
    for (const [id, { token: _token, ...grant }] of session?.grants ?? []) {
      workspaces[id] = grant;
    }
    return { csrf: session?.csrf ?? null, workspaces };
  }

  private evict(): void {
    for (const [id, session] of this.sessions) {
      if (session.expires <= this.now()) {
        this.sessions.delete(id);
      }
    }
    while (this.sessions.size >= MAX_SESSIONS) {
      const oldest = this.sessions.keys().next().value;
      if (oldest === undefined) {
        break;
      }
      this.sessions.delete(oldest);
    }
  }
}
