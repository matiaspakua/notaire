/**
 * Outbound port for folio instance admin (#1785 / ADR-030).
 */
import type { AvailableNotary, Folio, FolioWriteInput } from "../domain/folio";

export interface FolioGateway {
  list(): Promise<Folio[]>;
  searchByStatus(status: string): Promise<Folio[]>;
  create(input: FolioWriteInput): Promise<void>;
  update(id: number, input: FolioWriteInput): Promise<void>;
  delete(id: number): Promise<void>;
  listAvailableNotaries(): Promise<AvailableNotary[]>;
}
