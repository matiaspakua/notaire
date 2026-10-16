/**
 * Outbound port for the folio-type catalog (#1785 / ADR-030).
 */
import type { FolioType } from "../domain/folio-type";

/** Write payload for create/update (update may carry notes/enabled/version). */
export interface FolioTypeInput {
  name: string;
  isAuxiliary?: boolean;
  notes?: string;
  enabled?: boolean;
  version?: number;
}

export interface FolioTypeGateway {
  list(): Promise<FolioType[]>;
  searchByName(name: string): Promise<FolioType[]>;
  create(input: FolioTypeInput): Promise<FolioType>;
  update(id: number, input: FolioTypeInput): Promise<FolioType>;
  delete(id: number): Promise<void>;
  isInUse(id: number): Promise<boolean>;
}
