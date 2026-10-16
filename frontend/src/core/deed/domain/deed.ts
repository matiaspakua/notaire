/**
 * Deed domain model (#1831 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 * Field names align with DeedResponse wire shape to limit mapping churn.
 */

export interface Deed {
  idDeed?: number;
  number?: number;
  dateDeedrecording?: string;
  dateRegistration?: string;
  body?: string;
  notes?: string;
  registrationEntryNumber?: string;
  status?: string;
  version?: number;
}
