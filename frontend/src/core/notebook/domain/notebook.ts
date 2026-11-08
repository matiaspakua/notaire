/**
 * Notebook domain model (#1833 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface NotebookNotaryRef {
  personId?: number;
  notaryRegistrationNumber?: number;
}

export interface Notebook {
  idNotebook?: number;
  number?: number;
  year?: number;
  notes?: string;
  fkIdNotaryPerson?: NotebookNotaryRef;
  version?: number;
}

/** POST /notebooks body. */
export interface NotebookCreateInput {
  idsFolio: number[];
  idNotary: number;
  year: number;
  notes?: string | null;
}
