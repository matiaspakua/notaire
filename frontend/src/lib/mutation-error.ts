/**
 * Shared presentation of API mutation failures (issue #1054).
 * Maps ApiError bodies into toasts and optional FormField errors.
 */
import { toast } from "sonner";
import { ApiError } from "@/lib/api-client";
import { extractApiError } from "@/lib/utils";

export type PresentMutationErrorOptions = {
  /** Shown when the API body has no parseable business message. */
  fallback: string;
  /**
   * When true, toast `fallback` even if `extractApiError` finds a body message.
   * Use for curated UX copy that must win over a terse/English server string
   * (e.g. CU39 400 → items.errorNoPlantilla).
   */
  preferFallback?: boolean;
  /** Form control names that may receive inline errors from `field: msg` bodies. */
  fieldNames?: string[];
  setFieldErrors?: (errors: Record<string, string>) => void;
};

export type PresentMutationErrorResult = {
  message: string;
  fieldErrors: Record<string, string>;
  toasted: boolean;
};

/**
 * Parse bean-validation style joined messages (`field: message; other: …`)
 * and keep only entries that match known form field names.
 */
export function parseFieldErrors(
  message: string,
  fieldNames?: string[]
): Record<string, string> {
  if (!fieldNames?.length || !message.includes(":")) {
    return {};
  }
  const allowed = new Set(fieldNames);
  const result: Record<string, string> = {};
  for (const segment of message.split(";")) {
    const trimmed = segment.trim();
    const colon = trimmed.indexOf(":");
    if (colon <= 0) {
      continue;
    }
    const field = trimmed.slice(0, colon).trim();
    const detail = trimmed.slice(colon + 1).trim();
    if (allowed.has(field) && detail) {
      result[field] = detail;
    }
  }
  return result;
}

/**
 * Present a mutation failure: toast the server message (or fallback), and
 * optionally wire field-level errors. Authenticated 401 is left to the
 * session-expiry handler (#1053) and must not become a generic mutation toast.
 */
export function presentMutationError(
  err: unknown,
  options: PresentMutationErrorOptions
): PresentMutationErrorResult {
  if (err instanceof ApiError && err.status === 401) {
    return { message: "", fieldErrors: {}, toasted: false };
  }

  const message = options.preferFallback
    ? options.fallback
    : (extractApiError(err) ?? options.fallback);
  const fieldErrors = parseFieldErrors(message, options.fieldNames);

  if (options.setFieldErrors && Object.keys(fieldErrors).length > 0) {
    options.setFieldErrors(fieldErrors);
  }

  toast.error(message);
  return { message, fieldErrors, toasted: true };
}

export type PresentDeleteErrorOptions = {
  /** Generic copy when the failure has no usable reason (e.g. `errorDelete`). */
  fallback: string;
  /** Translated "still in use" copy for 409 (`common.errors.inUse`). */
  inUse: string;
  /** Translated "no longer exists" copy for 404 (`common.errors.notFound`). */
  notFound: string;
};

export type PresentDeleteErrorResult = {
  kind: "in-use" | "not-found" | "error" | "ignored";
  message: string;
  detail?: string;
};

/** Short plain-text bodies (e.g. PersonController's 409) are a reason too; HTML is not. */
function plainTextReason(body: string): string | null {
  const text = body.trim();
  if (!text || text.startsWith("<") || text.startsWith("{") || text.length > 300) {
    return null;
  }
  return text;
}

/**
 * Present a delete failure (issue #1345). A 409 means the record is still
 * referenced by others: it is a warning, not an error, and keeps the
 * backend's reason as the toast description. 404 says the record is gone;
 * anything else shows the server message or the fallback. 401 is left to
 * the session-expiry handler (#1053).
 */
export function presentDeleteError(
  err: unknown,
  options: PresentDeleteErrorOptions
): PresentDeleteErrorResult {
  if (err instanceof ApiError) {
    if (err.status === 401) {
      return { kind: "ignored", message: "" };
    }
    if (err.status === 409) {
      const detail = extractApiError(err) ?? plainTextReason(err.body) ?? undefined;
      toast.warning(options.inUse, detail ? { description: detail } : undefined);
      return { kind: "in-use", message: options.inUse, detail };
    }
    if (err.status === 404) {
      toast.error(options.notFound);
      return { kind: "not-found", message: options.notFound };
    }
  }
  const message = extractApiError(err) ?? options.fallback;
  toast.error(message);
  return { kind: "error", message };
}
