/**
 * Outbound port for managements (#1839 / ADR-030).
 */
import type {
  ArchivedManagement,
  CreateCompleteManagementInput,
  Management,
  ManagementCaseSummary,
  ManagementCompleteCase,
  ManagementFinancialSummary,
  ManagementHistory,
  ManagementInput,
  ManagementPage,
  ManagementPageRequest,
  OutstandingBalance,
  ProcedureFolder,
} from "../domain/management";

export interface ManagementGateway {
  listPage(request: ManagementPageRequest): Promise<ManagementPage>;
  getById(id: number): Promise<Management>;
  /** Returns null when the management number does not exist (404). */
  findByNumber(number: number): Promise<Management | null>;
  listByClient(idPerson: number): Promise<Management[]>;
  create(input: ManagementInput): Promise<void>;
  createCompleteCase(input: CreateCompleteManagementInput): Promise<Management>;
  getCompleteCase(id: number): Promise<ManagementCompleteCase>;
  updateCompleteCase(id: number, input: CreateCompleteManagementInput): Promise<Management>;
  delete(id: number): Promise<void>;
  getOutstandingBalance(id: number): Promise<OutstandingBalance>;
  archive(id: number, confirmado: boolean): Promise<ArchivedManagement>;
  transition(id: number, statusDestination: string): Promise<Management>;
  getCurrentStatus(id: number): Promise<ManagementHistory>;
  listHistory(id: number): Promise<ManagementHistory[]>;
  listFolders(managementId: number): Promise<ProcedureFolder[]>;
  putFolderOnHold(idFolder: number, reason: string): Promise<ProcedureFolder>;
  getCaseSummary(id: number): Promise<ManagementCaseSummary>;
  getFinancialSummary(id: number): Promise<ManagementFinancialSummary>;
}
