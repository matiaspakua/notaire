/**
 * Submitted-document domain model (#1841 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface SubmittedDocument {
  idSubmittedDocument?: number;
  name?: string;
  typeId?: number;
  procedureId?: number;
  date?: string;
  delivered?: boolean;
  deliveredBy?: string;
  [key: string]: unknown;
}

/** POST /submitted-documents body. */
export interface SubmittedDocumentCreateInput {
  typeId: number;
  procedureId: number;
  date?: string;
  delivered?: boolean;
  deliveredBy?: string;
  name?: string;
}

/** PUT /submitted-documents/{id} body. */
export interface SubmittedDocumentUpdateInput {
  typeId?: number;
  procedureId?: number;
  date?: string;
  delivered?: boolean;
  deliveredBy?: string;
  name?: string;
}

/** CU42 — upcoming expirations row. */
export interface UpcomingExpiration {
  idSubmittedDocument: number;
  documentName?: string;
  managementNumber?: number;
  managementHeading?: string;
  prepared: boolean;
  dateEntry?: string;
  dateExit?: string;
  cardNumber?: number;
  observed: boolean;
  amountToPay?: number;
  datePayment?: string;
  dateReleased?: string;
  notes?: string;
  dateDue: string;
  daysRemaining: number;
}
