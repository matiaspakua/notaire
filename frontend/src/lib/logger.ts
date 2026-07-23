/**
 * Structured logger for the Next.js frontend.
 *
 * Emits one JSON object per log line so Promtail can scrape Docker stdout
 * with the same pipeline used for the backend. The schema mirrors the
 * Logback LogstashEncoder fields:
 *
 *  - service:     "notaire-frontend"
 *  - environment: from NEXT_PUBLIC_ENVIRONMENT
 *  - timestamp:   ISO-8601 string
 *  - level:       DEBUG | INFO | WARN | ERROR
 *  - message:     human-readable text
 *  - path:        current URL (browser only)
 *  - context:     any additional structured fields
 *
 * Usage:
 *   import { logger } from "@/lib/logger";
 *   logger.info("user_logged_in", { userId: 1 });
 *   logger.error("api_call_failed", { endpoint, status }, error);
 */

export type LogLevel = "DEBUG" | "INFO" | "WARN" | "ERROR";

export interface LogEntry {
  service: string;
  environment: string;
  timestamp: string;
  level: LogLevel;
  message: string;
  path?: string;
  context?: Record<string, unknown>;
  stack?: string;
}

const SERVICE = "notaire-frontend";
const ENVIRONMENT =
  (typeof process !== "undefined" && process.env?.NEXT_PUBLIC_ENVIRONMENT) ||
  "development";

function currentPath(): string | undefined {
  if (typeof window === "undefined") {
    return undefined;
  }
  try {
    return window.location?.pathname;
  } catch {
    return undefined;
  }
}

function emit(entry: LogEntry): void {
  const line = JSON.stringify(entry);
  switch (entry.level) {
    case "ERROR":
       
      console.error(line);
      break;
    case "WARN":
       
      console.warn(line);
      break;
    case "DEBUG":
       
      console.debug(line);
      break;
    case "INFO":
    default:
       
      console.log(line);
      break;
  }
}

function buildEntry(
  level: LogLevel,
  message: string,
  context?: Record<string, unknown>,
  error?: unknown
): LogEntry {
  const entry: LogEntry = {
    service: SERVICE,
    environment: ENVIRONMENT,
    timestamp: new Date().toISOString(),
    level,
    message,
  };
  const path = currentPath();
  if (path) {
    entry.path = path;
  }
  if (context && Object.keys(context).length > 0) {
    entry.context = context;
  }
  if (error instanceof Error) {
    entry.stack = error.stack;
    if (!entry.context) {
      entry.context = {};
    }
    entry.context.errorName = error.name;
    entry.context.errorMessage = error.message;
  } else if (error !== undefined) {
    if (!entry.context) {
      entry.context = {};
    }
    entry.context.error = String(error);
  }
  return entry;
}

export const logger = {
  debug(message: string, context?: Record<string, unknown>): void {
    emit(buildEntry("DEBUG", message, context));
  },
  info(message: string, context?: Record<string, unknown>): void {
    emit(buildEntry("INFO", message, context));
  },
  warn(message: string, context?: Record<string, unknown>): void {
    emit(buildEntry("WARN", message, context));
  },
  error(message: string, context?: Record<string, unknown>, error?: unknown): void {
    emit(buildEntry("ERROR", message, context, error));
  },
};
