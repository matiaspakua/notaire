/**
 * Outbound port for the management-status catalog (#1769 / ADR-030).
 */
import type { ManagementStatus } from "../domain/management-status";

/** Write payload for create/update (version required on update). */
export interface ManagementStatusInput {
  name: string;
  notes?: string | null;
  version?: number;
}

export interface ManagementStatusGateway {
  list(): Promise<ManagementStatus[]>;
  searchByName(name: string): Promise<ManagementStatus[]>;
  create(input: ManagementStatusInput): Promise<ManagementStatus>;
  update(id: number, input: ManagementStatusInput): Promise<ManagementStatus>;
  delete(id: number): Promise<void>;
  isInUse(id: number): Promise<boolean>;
}
