/**
 * External-entity document domain model (#1841 / ADR-030).
 */

export interface ExternalEntityDocument {
  idSubmittedDocument: number;
  name?: string;
  delivered?: boolean;
  [key: string]: unknown;
}

export interface ManagementExternalEntityDocuments {
  idManagement: number;
  number?: number;
  heading?: string;
  dateStart?: string;
  notary?: string;
  cadastralDesignation?: string;
  documents: ExternalEntityDocument[];
}

export interface ExternalEntityDocumentMovementInput {
  prepared?: boolean;
  cardNumber?: number;
  dateEntry?: string;
  dateExit?: string;
  flagged?: boolean;
  amountToPay?: number;
  datePayment?: string;
  dateReleased?: string;
  notes?: string;
  delivered?: boolean;
}
