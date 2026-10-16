/**
 * Testimony domain model (#1834 / ADR-030).
 * Pure TypeScript — no React, Next, or HTTP.
 */

export interface TestimonyDeedRef {
  idDeed?: number;
  number?: number;
  status?: string;
}

export interface TestimonyMovementRef {
  idTestimonyMovement?: number;
  dateEntry?: string;
  dateExit?: string;
  dateRegistration?: string;
  registered?: boolean;
  cardNumber?: number;
  observedByRegistry?: boolean;
  notes?: string;
  testimony?: { idTestimony?: number };
}

export interface Testimony {
  idTestimony?: number;
  number?: number;
  flagged?: boolean;
  verified?: boolean;
  notes?: string | null;
  /** Optimistic-lock version; PUT must send the one read (409 otherwise). */
  version?: number;
  deed?: TestimonyDeedRef;
  testimonyMovements?: TestimonyMovementRef[];
}

/** Body of POST /testimonies (#1555). */
export interface TestimonyCreateInput {
  deed: { idDeed: number; number: number };
  number: number;
  notes: string;
  flagged: boolean;
  verified: boolean;
}

/** Body of PUT /testimonies/{id}: number, flagged, verified and version required (#655). */
export interface TestimonyUpdateInput {
  number: number;
  notes: string;
  flagged: boolean;
  verified: boolean;
  version: number;
}

export interface TestimonyVerifyInput {
  flagged: boolean;
  notes?: string;
}
