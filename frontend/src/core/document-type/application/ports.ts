/**
 * Outbound port for the document-type catalog (#1777 / ADR-030).
 */
import type { DocumentType } from "../domain/document-type";

/** Write payload for create/update. */
export interface DocumentTypeInput {
  name: string;
  expires?: boolean;
  dueDays?: number | null;
  deliveredBy?: string | null;
  enabled?: boolean;
  returned?: boolean;
}

export interface DocumentTypeGateway {
  list(): Promise<DocumentType[]>;
  searchByName(name: string): Promise<DocumentType[]>;
  create(input: DocumentTypeInput): Promise<DocumentType>;
  update(id: number, input: DocumentTypeInput): Promise<DocumentType>;
  delete(id: number): Promise<void>;
  isInUse(id: number): Promise<boolean>;
}
