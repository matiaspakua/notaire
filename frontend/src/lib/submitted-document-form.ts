import type { SubmittedDocument, SubmittedDocumentRequest } from "@/types";

/** State of the Documents create/edit dialog (CU04 / CU72). */
export interface DocumentForm {
  typeId: string;
  date: string;
  delivered: boolean;
  managementId: string;
  procedureId: string;
}

export const EMPTY_DOCUMENT_FORM: DocumentForm = {
  typeId: "",
  date: "",
  delivered: false,
  managementId: "",
  procedureId: "",
};

export type DocumentRequiredField = "typeId" | "procedureId";

/**
 * A document is presented for a procedure of a management (CU04), so a new one needs
 * its type and its procedure (#655). Editing never forces a procedure: documents
 * stored before this rule may have none and must stay editable.
 */
export function missingDocumentFields(form: DocumentForm, isEditing: boolean): DocumentRequiredField[] {
  if (isEditing) return [];
  const missing: DocumentRequiredField[] = [];
  if (!form.typeId) missing.push("typeId");
  if (!form.procedureId) missing.push("procedureId");
  return missing;
}

/** The management and procedure selectors appear on create and on documents not yet linked to a procedure. */
export function showsProcedureLink(editing: SubmittedDocument | null): boolean {
  return !editing || editing.procedureId == null;
}

/** Build a create body; call only after `missingDocumentFields` is empty (#655 / #1260). */
export function toDocumentRequest(form: DocumentForm): SubmittedDocumentRequest {
  return {
    typeId: Number(form.typeId),
    date: form.date || undefined,
    delivered: form.delivered,
    procedureId: Number(form.procedureId),
  };
}
