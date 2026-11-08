/**
 * React Query hooks for managements.
 * Hexagon wiring: #1839 / ADR-030.
 */
import { keepPreviousData, useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { managementUseCases } from "@/composition/management";
import { managementWorkflowKeys } from "@/hooks/useManagementWorkflow";
import type {
  Management as DomainManagement,
  ManagementCompleteCase as DomainCompleteCase,
  CreateCompleteManagementInput as DomainCompleteInput,
  OutstandingBalance as DomainOutstandingBalance,
  ArchivedManagement as DomainArchived,
  ManagementHistory as DomainHistory,
  ProcedureFolder as DomainFolder,
} from "@/core/management/domain/management";
import type { DtoOutstandingBalance, DeedManagement, ManagementCompleteCase } from "@/types/api";
import type {
  ProcedureFolder,
  CreateCompleteManagementInput,
  DtoArchivedManagement,
  ManagementHistory,
} from "@/types";

export const managementsKeys = {
  all: ["managements"] as const,
  detail: (id: number) => ["managements", id] as const,
  completeCase: (id: number) => ["managements", id, "complete-case"] as const,
  byClient: (id: number) => ["managements", "client", id] as const,
  byNumber: (number: number) => ["managements", "number", number] as const,
  outstandingBalance: (id: number) => ["managements", id, "outstanding-balance"] as const,
  currentStatus: (id: number) => ["managements", id, "current-status"] as const,
  page: (params: ManagementsPageParams) => ["managements", "page", params] as const,
};

export interface ManagementsPageParams {
  page: number;
  size: number;
  /** Spring sort; defaults to newest first. `null` keeps the backend order. */
  sort?: string | null;
}

/** Sort for the managements list: newest first (#1340). */
export const MANAGEMENTS_SORT = "idManagement,desc";

function toWire(m: DomainManagement): DeedManagement {
  return {
    idManagement: m.idManagement,
    number: m.number,
    heading: m.heading,
    notes: m.notes,
    currentStatus: m.currentStatus,
    dateStart: m.dateStart,
    procedureCount: m.procedureCount,
  };
}

function toCompleteCase(c: DomainCompleteCase): ManagementCompleteCase {
  return {
    idManagement: c.idManagement,
    number: c.number,
    heading: c.heading,
    notes: c.notes,
    budgetId: c.budgetId,
    notaryId: c.notaryId,
    statusManagementId: c.statusManagementId,
    typeProcedureId: c.typeProcedureId,
    propertyId: c.propertyId,
  };
}

function toOutstanding(b: DomainOutstandingBalance): DtoOutstandingBalance {
  return { pendingBalance: b.pendingBalance };
}

function toArchived(a: DomainArchived): DtoArchivedManagement {
  return {
    idManagement: a.idManagement,
    pendingBalance: a.pendingBalance,
    pendingDebtAtArchiving: a.pendingDebtAtArchiving,
  };
}

function toHistory(h: DomainHistory): ManagementHistory {
  return {
    idHistory: h.idHistory,
    date: h.date,
    notes: h.notes,
    managementId: h.managementId,
    statusManagementId: h.statusManagementId,
    statusManagementName: h.statusManagementName,
  };
}

function toFolder(f: DomainFolder): ProcedureFolder {
  return {
    idFolder: f.idFolder,
    number: f.number,
    status: f.status,
    waitReason: f.waitReason,
    idManagement: f.idManagement,
    idProcedure: f.idProcedure,
  };
}

/**
 * One server page of GET /managements (#1340). The managements table outgrows any
 * fixed size, so screens never load it whole.
 */
export function useManagementsPage(params: ManagementsPageParams, options: { enabled?: boolean } = {}) {
  return useQuery({
    queryKey: managementsKeys.page(params),
    queryFn: async () => {
      const page = await managementUseCases.listPage({
        page: params.page,
        size: params.size,
        sort: params.sort === null ? undefined : (params.sort ?? MANAGEMENTS_SORT),
      });
      return {
        ...page,
        content: page.content.map(toWire),
      };
    },
    placeholderData: keepPreviousData,
    enabled: options.enabled ?? true,
  });
}

export const historyKeys = {
  byManagement: (id: number) => ["history", "management", id] as const,
};

export const procedureFolderKeys = {
  byManagement: (id: number) => ["procedure-folders", "management", id] as const,
};

export function useManagementByNumber(number: number | undefined) {
  return useQuery({
    queryKey: managementsKeys.byNumber(number ?? 0),
    queryFn: async () => {
      const found = await managementUseCases.findByNumber(number!);
      if (!found) {
        // Preserve prior apiGet 404 throw so callers still see a query error.
        throw Object.assign(new Error("Not found"), { status: 404 });
      }
      return toWire(found);
    },
    enabled: number != null && number > 0,
    retry: false,
  });
}

export function useManagementsByClient(idPerson: number) {
  return useQuery({
    queryKey: managementsKeys.byClient(idPerson),
    queryFn: async () => (await managementUseCases.listByClient(idPerson)).map(toWire),
    enabled: idPerson > 0,
  });
}

export function useCreateManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Partial<DeedManagement>) => managementUseCases.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: managementsKeys.all }),
  });
}

export function useCreateCompleteManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (data: CreateCompleteManagementInput) =>
      toWire(await managementUseCases.createCompleteCase(data as DomainCompleteInput)),
    onSuccess: () => {
      // #773 — complete-case seeds required docs; refresh list + case-summary caches.
      qc.invalidateQueries({ queryKey: managementsKeys.all });
      qc.invalidateQueries({ queryKey: ["management-case-summary"] });
    },
  });
}

/** CU53 (#1360) - current case values of a management, to prefill the full edit. */
export function useManagementCompleteCase(id: number | undefined) {
  return useQuery({
    queryKey: managementsKeys.completeCase(id ?? 0),
    queryFn: async () => toCompleteCase(await managementUseCases.getCompleteCase(id!)),
    enabled: !!id,
  });
}

/**
 * CU53 (#1360) - full management edit through PUT /managements/{id}/complete-case. The
 * status must stay the same (CU83: status changes go through /transition).
 */
export function useUpdateCompleteManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: CreateCompleteManagementInput }) =>
      toWire(await managementUseCases.updateCompleteCase(id, data as DomainCompleteInput)),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: managementsKeys.all });
      qc.invalidateQueries({ queryKey: managementWorkflowKeys.trace(id) });
    },
  });
}

export function useDeleteManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => managementUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: managementsKeys.all }),
  });
}

/** CU16 - Aggregated outstanding balance of a management (RF-22), loaded before archive. */
export function useOutstandingBalance(managementId: number | undefined) {
  return useQuery({
    queryKey: managementsKeys.outstandingBalance(managementId ?? 0),
    queryFn: async () => toOutstanding(await managementUseCases.getOutstandingBalance(managementId!)),
    enabled: !!managementId,
  });
}

/**
 * CU16 - Archives the management, warning about and recording outstanding debt (RF-22, RF-37).
 * The `confirmado` query flag must be sent as true when the user archives anyway
 * despite unresolved procedure folders on hold (CU85, HTTP 409).
 */
export function useArchiveManagement() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, confirmado = false }: { id: number; confirmado?: boolean }) =>
      toArchived(await managementUseCases.archive(id, confirmado)),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: managementsKeys.all });
      qc.invalidateQueries({ queryKey: procedureFolderKeys.byManagement(id) });
    },
  });
}

/** CU83 - Moves a management to another status after the defined workflow allows it. */
export function useTransitionManagement() {
  const qc = useQueryClient();
  return useMutation({
    // Backend route is /transition (ManagementController#transition). The body field is statusDestination.
    mutationFn: async ({ id, statusDestination }: { id: number; statusDestination: string }) =>
      toWire(await managementUseCases.transition(id, statusDestination)),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: managementsKeys.all });
      qc.invalidateQueries({ queryKey: managementWorkflowKeys.trace(id) });
      qc.invalidateQueries({ queryKey: historyKeys.byManagement(id) });
    },
  });
}

/**
 * CU14 (#1553) - current status of a management: the latest History row, or a summary
 * built from its status (no `idHistory`). 404 when it has none. Lives under
 * managementsKeys.all, so a CU83 transition refreshes it.
 */
export function useManagementCurrentStatus(managementId: number | undefined) {
  return useQuery({
    queryKey: managementsKeys.currentStatus(managementId ?? 0),
    queryFn: async () => toHistory(await managementUseCases.getCurrentStatus(managementId!)),
    enabled: !!managementId,
    retry: false,
  });
}

/** CU13 - Full history of a management, in chronological order. */
export function useHistory(managementId: number | undefined) {
  return useQuery({
    queryKey: historyKeys.byManagement(managementId ?? 0),
    queryFn: async () => (await managementUseCases.listHistory(managementId!)).map(toHistory),
    enabled: !!managementId,
  });
}

/** CU85 - Procedure folders linked to a management. */
export function useFoldersByManagement(managementId: number | undefined) {
  return useQuery({
    queryKey: procedureFolderKeys.byManagement(managementId ?? 0),
    queryFn: async () => (await managementUseCases.listFolders(managementId!)).map(toFolder),
    enabled: !!managementId,
  });
}

/** CU85 - Puts a procedure folder on hold and requires a reason (exception 3.1). */
export function usePutFolderOnHold() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ idFolder, reason }: { idFolder: number; reason: string; managementId: number }) =>
      toFolder(await managementUseCases.putFolderOnHold(idFolder, reason)),
    onSuccess: (_data, { managementId }) => {
      qc.invalidateQueries({ queryKey: procedureFolderKeys.byManagement(managementId) });
    },
  });
}
