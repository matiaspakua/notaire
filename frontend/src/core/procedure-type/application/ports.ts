/**
 * Outbound port for the procedure-type catalog (#1796 / ADR-030).
 */
import type { ProcedureType } from "../domain/procedure-type";

/** Write payload for create/update. */
export interface ProcedureTypeInput {
  name: string;
  notes?: string | null;
  isArchived?: boolean;
  isRegistered?: boolean;
  associatesProperties?: boolean;
  enabled?: boolean;
}

export interface ProcedureTypeGateway {
  list(): Promise<ProcedureType[]>;
  searchByName(name: string): Promise<ProcedureType[]>;
  create(input: ProcedureTypeInput): Promise<ProcedureType>;
  update(id: number, input: ProcedureTypeInput): Promise<ProcedureType>;
  delete(id: number): Promise<void>;
  isInUse(id: number): Promise<boolean>;
  assignWorkflow(id: number, workflowDefinitionId: number | null): Promise<ProcedureType>;
}
