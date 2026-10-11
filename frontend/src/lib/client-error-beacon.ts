/**
 * Safe client-error beacon for CU77 / #1483.
 *
 * Unauthenticated POST sink: allow-listed fields, size caps, and a per-client
 * in-memory rate limit. Accepted entries go through {@link logger} so Promtail
 * ships them to Loki as service=notaire-frontend.
 */
import { logger } from "@/lib/logger";

export const MAX_RAW_BODY_BYTES = 8192;
export const MAX_MESSAGE_CHARS = 1024;
export const MAX_NAME_CHARS = 128;
export const MAX_PATH_CHARS = 256;
export const MAX_STACK_CHARS = 4096;
export const RATE_LIMIT_MAX = 30;
export const RATE_LIMIT_WINDOW_MS = 60_000;

const ALLOWED_LEVELS = new Set(["ERROR", "WARN"] as const);
const ALLOWED_KEYS = new Set(["level", "message", "name", "path", "stack"]);

type BeaconLevel = "ERROR" | "WARN";

interface RateBucket {
  count: number;
  windowStart: number;
}

const rateBuckets = new Map<string, RateBucket>();

export function __resetClientErrorBeaconForTests(): void {
  rateBuckets.clear();
}

function clientKey(request: Request): string {
  const forwarded = request.headers.get("x-forwarded-for");
  if (forwarded) {
    const first = forwarded.split(",")[0]?.trim();
    if (first) {
      return first;
    }
  }
  const realIp = request.headers.get("x-real-ip")?.trim();
  if (realIp) {
    return realIp;
  }
  return "unknown";
}

function isRateLimited(key: string): boolean {
  const now = Date.now();
  const bucket = rateBuckets.get(key);
  if (!bucket || now - bucket.windowStart >= RATE_LIMIT_WINDOW_MS) {
    rateBuckets.set(key, { count: 1, windowStart: now });
    return false;
  }
  bucket.count += 1;
  return bucket.count > RATE_LIMIT_MAX;
}

function sanitizePath(raw: string): string | undefined {
  const trimmed = raw.trim();
  if (!trimmed) {
    return undefined;
  }
  const withoutQuery = trimmed.split("?", 1)[0] ?? "";
  if (!withoutQuery.startsWith("/")) {
    return undefined;
  }
  return withoutQuery.slice(0, MAX_PATH_CHARS);
}

function truncate(value: string, max: number): string {
  return value.length <= max ? value : value.slice(0, max);
}

interface ValidatedPayload {
  level: BeaconLevel;
  message: string;
  name?: string;
  path?: string;
  stack?: string;
}

function validatePayload(raw: unknown): ValidatedPayload | { error: number } {
  if (raw === null || typeof raw !== "object" || Array.isArray(raw)) {
    return { error: 400 };
  }
  const obj = raw as Record<string, unknown>;
  for (const key of Object.keys(obj)) {
    if (!ALLOWED_KEYS.has(key)) {
      return { error: 400 };
    }
  }

  const level = obj.level;
  if (typeof level !== "string" || !ALLOWED_LEVELS.has(level as BeaconLevel)) {
    return { error: 400 };
  }
  if (typeof obj.message !== "string" || obj.message.trim().length === 0) {
    return { error: 400 };
  }
  if (obj.message.length > MAX_MESSAGE_CHARS) {
    return { error: 400 };
  }

  const result: ValidatedPayload = {
    level: level as BeaconLevel,
    message: truncate(obj.message.trim(), MAX_MESSAGE_CHARS),
  };

  if (obj.name !== undefined) {
    if (typeof obj.name !== "string") {
      return { error: 400 };
    }
    result.name = truncate(obj.name, MAX_NAME_CHARS);
  }
  if (obj.path !== undefined) {
    if (typeof obj.path !== "string") {
      return { error: 400 };
    }
    const path = sanitizePath(obj.path);
    if (!path) {
      return { error: 400 };
    }
    result.path = path;
  }
  if (obj.stack !== undefined) {
    if (typeof obj.stack !== "string") {
      return { error: 400 };
    }
    result.stack = truncate(obj.stack, MAX_STACK_CHARS);
  }

  return result;
}

export async function handleClientErrorBeacon(request: Request): Promise<Response> {
  if (request.method !== "POST") {
    return new Response(null, { status: 405 });
  }

  const rawText = await request.text();
  if (rawText.length > MAX_RAW_BODY_BYTES) {
    return new Response(null, { status: 413 });
  }

  let parsed: unknown;
  try {
    parsed = JSON.parse(rawText);
  } catch {
    return new Response(null, { status: 400 });
  }

  const validated = validatePayload(parsed);
  if ("error" in validated) {
    return new Response(null, { status: validated.error });
  }

  if (isRateLimited(clientKey(request))) {
    return new Response(null, { status: 429 });
  }

  const pathPart = validated.path ?? "-";
  const logMessage = `client_error level=${validated.level} path=${pathPart} msg=${validated.message}`;
  const context: Record<string, unknown> = {
    source: "client_beacon",
    level: validated.level,
  };
  if (validated.name) {
    context.name = validated.name;
  }
  if (validated.path) {
    context.path = validated.path;
  }

  if (validated.level === "WARN") {
    logger.warn(logMessage, context);
  } else {
    const stackError =
      validated.stack !== undefined
        ? Object.assign(new Error(validated.message), {
            name: validated.name ?? "ClientError",
            stack: validated.stack,
          })
        : undefined;
    logger.error(logMessage, context, stackError);
  }

  return new Response(null, { status: 204 });
}
