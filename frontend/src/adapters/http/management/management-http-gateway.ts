/**
 * HTTP adapter for managements (#1839 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiGetPage, apiPost, apiPut } from "@/lib/api-client";
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
} from "@/core/management/domain/management";
import type { ManagementGateway } from "@/core/management/application/ports";

function httpStatus(err: unknown): number | undefined {
  if (typeof err === "object" && err !== null && "status" in err) {
    const status = (err as { status: unknown }).status;
    return typeof status === "number" ? status : undefined;
  }
  return undefined;
}

/**
 * ManagementController's CompleteCaseRequest wire names are (number, heading, notes, budgetId,
 * notaryId, statusManagementId, typeProcedureId, propertyId): the input's procedureTypeId is
 * sent as typeProcedureId, otherwise the backend reads null and answers 400.
 */
export function toCompleteCaseRequest(data: CreateCompleteManagementInput) {
  return {
    number: data.number,
    heading: data.heading,
    notes: data.notes,
    budgetId: data.budgetId,
    notaryId: data.notaryId,
    statusManagementId: data.statusManagementId,
    typeProcedureId: data.procedureTypeId,
    propertyId: data.propertyId,
  };
}

function toManagement(dto: Management): Management {
  return {
    idManagement: dto.idManagement,
    number: dto.number,
    heading: dto.heading,
    notes: dto.notes,
    currentStatus: dto.currentStatus,
    dateStart: dto.dateStart,
    procedureCount: dto.procedureCount,
  };
}

function toCompleteCase(dto: ManagementCompleteCase & { typeProcedureId?: number }): ManagementCompleteCase {
  return {
    idManagement: dto.idManagement,
    number: dto.number,
    heading: dto.heading,
    notes: dto.notes,
    budgetId: dto.budgetId,
    notaryId: dto.notaryId,
    statusManagementId: dto.statusManagementId,
    typeProcedureId: dto.typeProcedureId,
    propertyId: dto.propertyId,
  };
}

export const managementHttpGateway: ManagementGateway = {
  listPage: async (request: ManagementPageRequest): Promise<ManagementPage> => {
    const page = await apiGetPage<Management>("/managements", {
      page: request.page,
      size: request.size,
      sort: request.sort,
    });
    return {
      content: page.content.map(toManagement),
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      number: page.number,
      size: page.size,
    };
  },
  getById: async (id: number) => toManagement(await apiGet<Management>(`/managements/${id}`)),
  findByNumber: async (number: number) => {
    try {
      return toManagement(await apiGet<Management>(`/managements/number/${number}`));
    } catch (e) {
      if (httpStatus(e) === 404) return null;
      throw e;
    }
  },
  listByClient: async (idPerson: number) =>
    (await apiGet<Management[]>(`/managements/client/${idPerson}`)).map(toManagement),
  create: async (input: ManagementInput) => {
    await apiPost<void>("/managements", input);
  },
  createCompleteCase: async (input: CreateCompleteManagementInput) =>
    toManagement(await apiPost<Management>("/managements/complete-case", toCompleteCaseRequest(input))),
  getCompleteCase: async (id: number) =>
    toCompleteCase(await apiGet<ManagementCompleteCase>(`/managements/${id}/complete-case`)),
  updateCompleteCase: async (id: number, input: CreateCompleteManagementInput) =>
    toManagement(
      await apiPut<Management>(`/managements/${id}/complete-case`, toCompleteCaseRequest(input)),
    ),
  delete: async (id: number) => {
    await apiDelete(`/managements/${id}`);
  },
  getOutstandingBalance: async (id: number) =>
    apiGet<OutstandingBalance>(`/managements/${id}/outstanding-balance`),
  archive: async (id: number, confirmado: boolean) =>
    apiPost<ArchivedManagement>(`/managements/${id}/archive?confirmado=${confirmado}`, {}),
  transition: async (id: number, statusDestination: string) =>
    toManagement(await apiPost<Management>(`/managements/${id}/transition`, { statusDestination })),
  getCurrentStatus: async (id: number) =>
    apiGet<ManagementHistory>(`/managements/${id}/current-status`),
  listHistory: async (id: number) => apiGet<ManagementHistory[]>(`/managements/${id}/history`),
  listFolders: async (managementId: number) =>
    apiGet<ProcedureFolder[]>(`/procedure-folders?managementId=${managementId}`),
  putFolderOnHold: async (idFolder: number, reason: string) =>
    apiPut<ProcedureFolder>(`/procedure-folders/${idFolder}/wait`, { reason }),
  getCaseSummary: async (id: number) =>
    apiGet<ManagementCaseSummary>(`/managements/${id}/case-summary`),
  getFinancialSummary: async (id: number) =>
    apiGet<ManagementFinancialSummary>(`/managements/${id}/financial-summary`),
};
