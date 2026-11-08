/**
 * Outbound port for procedure templates (#1788 / ADR-030).
 */
import type { ProcedureTemplate } from "../domain/procedure-template";

export interface ProcedureTemplateGateway {
  listAll(): Promise<ProcedureTemplate[]>;
  listByProcedureType(procedureTypeId: number): Promise<ProcedureTemplate[]>;
  create(input: {
    procedureTypeId: number;
    documentTypeId: number;
    notes?: string;
  }): Promise<ProcedureTemplate>;
  update(input: {
    procedureTypeId: number;
    documentTypeId: number;
    notes: string;
  }): Promise<ProcedureTemplate>;
  delete(procedureTypeId: number, documentTypeId: number): Promise<void>;
}
