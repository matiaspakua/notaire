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
