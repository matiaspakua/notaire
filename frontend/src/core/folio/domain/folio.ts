/**
 * Folio instance domain model for admin (#1785 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface FolioTypeRef {
  idFolioType?: number;
  name?: string;
}

export interface NotaryRef {
  personId?: number;
  notaryRegistrationNumber?: number;
}

export interface DeedRef {
  idDeed?: number;
  number?: number;
}

export interface Folio {
  idFolio?: number;
  number?: number;
  year?: number;
  status?: string;
  notes?: string;
  fkIdFolioType?: FolioTypeRef;
  fkIdNotaryPerson?: NotaryRef;
  fkIdDeed?: DeedRef;
  version?: number;
}

/** Notary option for the folio form picker. */
export interface AvailableNotary {
  personId: number;
  firstName?: string;
  lastName?: string;
  notaryRegistrationNumber?: number;
}

export interface FolioWriteInput {
  number: number;
  year?: number;
  status: string;
  notes: string;
  typeFolioId: number;
  notaryId: number;
  deedId?: number;
}
