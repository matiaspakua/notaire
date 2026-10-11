/**
 * Person create/update mutation error presentation (issue #945).
 * Non-409: surface extractable backend validation (toast + field errors).
 * 409: keep curated localized duplicate-document UX (do not leak English API text).
 */
import { toast } from "sonner";
import { ApiError } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import type { Person } from "@/types";

export const PERSON_FIELD_NAMES = [
  "firstName",
  "lastName",
  "identificationNumber",
  "email",
  "phone",
  "address",
  "taxId",
];

export type PresentPersonSaveErrorOptions = {
  fallback: string;
  duplicateDocument: string;
  viewExistingLabel: string;
  people: Person[];
  /** Loads the existing person when it is not among `people` (e.g. on another page, #1340). */
  loadPerson?: (id: number) => Promise<Person>;
  onViewExisting: (person: Person) => void;
  setFieldErrors: (errors: Record<string, string>) => void;
};

export type PresentPersonSaveErrorResult = {
  message: string;
  fieldErrors: Record<string, string>;
  usedDuplicatePath: boolean;
};

function extractDuplicatePersonId(err: ApiError): number | undefined {
  try {
    return (JSON.parse(err.body) as { existingPersonId?: number }).existingPersonId;
  } catch {
    return undefined;
  }
}

export function presentPersonSaveError(
  err: unknown,
  options: PresentPersonSaveErrorOptions
): PresentPersonSaveErrorResult {
  if (err instanceof ApiError && err.status === 409) {
    const existingId = extractDuplicatePersonId(err);
    const existing = options.people.find((p) => p.personId === existingId);
    const { loadPerson } = options;
    const onClick = existing
      ? () => options.onViewExisting(existing)
      : existingId != null && loadPerson
        ? () => {
            loadPerson(existingId)
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
    fieldNames: PERSON_FIELD_NAMES,
    setFieldErrors: options.setFieldErrors,
  });
  return {
    message: result.message,
    fieldErrors: result.fieldErrors,
    usedDuplicatePath: false,
  };
}
