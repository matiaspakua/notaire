import type { SubmittedDocument, SubmittedDocumentRequest } from "@/types";

/** State of the Documents create/edit dialog (CU04 / CU72). */
export interface DocumentForm {
  tipoId: string;
  fecha: string;
  entregado: boolean;
  managementId: string;
  procedureId: string;
}

export const EMPTY_DOCUMENT_FORM: DocumentForm = {
  tipoId: "",
  fecha: "",
  entregado: false,
  managementId: "",
  procedureId: "",
};

export type DocumentRequiredField = "tipoId" | "tramiteId";

/**
 * A document is presented for a trámite of a gestión (CU04), so a new one needs
 * its type and its trámite (#655). Editing never forces a trámite: documents
 * stored before this rule may have none and must stay editable.
 */
export function missingDocumentFields(form: DocumentForm, isEditing: boolean): DocumentRequiredField[] {
  if (isEditing) return [];
  const missing: DocumentRequiredField[] = [];
  if (!form.tipoId) missing.push("tipoId");
  if (!form.procedureId) missing.push("tramiteId");
  return missing;
}

/** The gestión/trámite selectors appear on create and on documents not yet linked to a trámite. */
export function showsProcedureLink(editing: SubmittedDocument | null): boolean {
  return !editing || editing.procedureId == null;
}

/** Build a create body; call only after `missingDocumentFields` is empty (#655 / #1260). */
export function toDocumentRequest(form: DocumentForm): SubmittedDocumentRequest {
  return {
    typeId: Number(form.tipoId),
    date: form.fecha || undefined,
    delivered: form.entregado,
    procedureId: Number(form.procedureId),
  };
}
