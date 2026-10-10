/**
 * Persona create/update mutation error presentation (issue #945).
 * Non-409: surface extractable backend validation (toast + field errors).
 * 409: keep curated localized duplicate-document UX (do not leak English API text).
 */
import { toast } from "sonner";
import { ApiError } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import type { Persona } from "@/types";

export const PERSONA_FIELD_NAMES = [
  "firstName",
  "lastName",
  "identificationNumber",
  "email",
  "phone",
  "address",
  "taxId",
];

export type PresentPersonaSaveErrorOptions = {
  fallback: string;
  duplicateDocument: string;
  viewExistingLabel: string;
  personas: Persona[];
  /** Loads the existing person when it is not among `personas` (e.g. on another page, #1340). */
  loadPersona?: (id: number) => Promise<Persona>;
  onViewExisting: (persona: Persona) => void;
  setFieldErrors: (errors: Record<string, string>) => void;
};

export type PresentPersonaSaveErrorResult = {
  message: string;
  fieldErrors: Record<string, string>;
  usedDuplicatePath: boolean;
};

function extractDuplicatePersonaId(err: ApiError): number | undefined {
  try {
    return (JSON.parse(err.body) as { existingPersonId?: number }).existingPersonId;
  } catch {
    return undefined;
  }
}

export function presentPersonaSaveError(
  err: unknown,
  options: PresentPersonaSaveErrorOptions
): PresentPersonaSaveErrorResult {
  if (err instanceof ApiError && err.status === 409) {
    const existingId = extractDuplicatePersonaId(err);
    const existing = options.personas.find((p) => p.personId === existingId);
    const { loadPersona } = options;
    const onClick = existing
      ? () => options.onViewExisting(existing)
      : existingId != null && loadPersona
        ? () => {
            loadPersona(existingId)
              .then(options.onViewExisting)
              .catch(() => toast.error(options.fallback));
          }
        : undefined;
    toast.error(options.duplicateDocument, {
      action: onClick ? { label: options.viewExistingLabel, onClick } : undefined,
    });
    return {
      message: options.duplicateDocument,
      fieldErrors: {},
      usedDuplicatePath: true,
    };
  }

  const result = presentMutationError(err, {
    fallback: options.fallback,
    fieldNames: PERSONA_FIELD_NAMES,
    setFieldErrors: options.setFieldErrors,
  });
  return {
    message: result.message,
    fieldErrors: result.fieldErrors,
    usedDuplicatePath: false,
  };
}
