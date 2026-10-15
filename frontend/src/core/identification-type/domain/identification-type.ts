/**
 * Identification type catalog domain model (#1732 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface IdentificationType {
  id: number;
  name: string;
  characters?: string | null;
  version?: number;
}

/** Name of the type a new person starts with (Owner decision on #1361). */
export const DEFAULT_IDENTIFICATION_TYPE = "DNI";

/** The id of the default type (DNI, any letter case) in the catalog, if present. */
export function defaultIdentificationTypeId(
  types: IdentificationType[] | undefined,
): number | undefined {
  return types?.find((t) => t.name?.trim().toUpperCase() === DEFAULT_IDENTIFICATION_TYPE)?.id;
}
