/**
 * Collect FormField errors for required create/edit values (#1951).
 * Empty string / whitespace, null, undefined, and NaN count as missing.
 * `0` and `false` are present.
 */
export function requiredFieldErrors(
  values: Record<string, unknown>,
  message: string,
): Record<string, string> {
  const errors: Record<string, string> = {};
  for (const [key, value] of Object.entries(values)) {
    if (value === null || value === undefined) {
      errors[key] = message;
      continue;
    }
    if (typeof value === "string" && value.trim() === "") {
      errors[key] = message;
      continue;
    }
    if (typeof value === "number" && Number.isNaN(value)) {
      errors[key] = message;
    }
  }
  return errors;
}
