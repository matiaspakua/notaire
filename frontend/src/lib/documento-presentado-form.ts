import type { DocumentoPresentado, DocumentoPresentadoRequest } from "@/types";

/** State of the Documentos create/edit dialog (CU04 / CU72). */
export interface DocumentoForm {
  tipoId: string;
  fecha: string;
  entregado: boolean;
  gestionId: string;
  tramiteId: string;
}

export const EMPTY_DOCUMENTO_FORM: DocumentoForm = {
  tipoId: "",
  fecha: "",
  entregado: false,
  gestionId: "",
  tramiteId: "",
};

export type DocumentoRequiredField = "tipoId" | "tramiteId";

/**
 * A document is presented for a trámite of a gestión (CU04), so a new one needs
 * its type and its trámite (#655). Editing never forces a trámite: documents
 * stored before this rule may have none and must stay editable.
 */
export function missingDocumentoFields(form: DocumentoForm, isEditing: boolean): DocumentoRequiredField[] {
  if (isEditing) return [];
  const missing: DocumentoRequiredField[] = [];
  if (!form.tipoId) missing.push("tipoId");
  if (!form.tramiteId) missing.push("tramiteId");
  return missing;
}

/** The gestión/trámite selectors appear on create and on documents not yet linked to a trámite. */
export function showsTramiteLink(editing: DocumentoPresentado | null): boolean {
  return !editing || editing.procedureId == null;
}

/** Build a create body; call only after `missingDocumentoFields` is empty (#655 / #1260). */
export function toDocumentoRequest(form: DocumentoForm): DocumentoPresentadoRequest {
  return {
    typeId: Number(form.tipoId),
    date: form.fecha || undefined,
    delivered: form.entregado,
    procedureId: Number(form.tramiteId),
  };
}
