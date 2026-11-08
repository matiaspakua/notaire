/**
 * Audit domain (#1843 / ADR-030).
 */
export interface AuditRecord {
  idAuditRecord?: number;
  users?: { name?: string };
  operationDetail?: string;
  date?: string;
  module?: string;
}

export interface AuditPage {
  content: AuditRecord[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface AuditPageRequest {
  page: number;
  size: number;
  module?: string;
  idUser?: number;
  from?: string;
  to?: string;
}
