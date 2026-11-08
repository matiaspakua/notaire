import type {
  DocumentCostTemplate,
  DocumentCostTemplateCreateInput,
} from "../domain/document-cost-template";

export interface DocumentCostTemplateGateway {
  listByProcedureType(procedureTypeId: number): Promise<DocumentCostTemplate[]>;
  create(input: DocumentCostTemplateCreateInput): Promise<void>;
}
