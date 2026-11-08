/**
 * React Query hooks for ProcedureType (CU26, CU57, CU64 — procedure types).
 * Hexagon wiring: #1796 / ADR-030.
 */
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { procedureTypeUseCases } from "@/composition/procedure-type";
import type { ProcedureType as DomainProcedureType } from "@/core/procedure-type/domain/procedure-type";
import type { ProcedureTypeInput } from "@/core/procedure-type/application/ports";
import type { ProcedureType } from "@/types";

export const procedureTypeKeys = {
  all: ["procedureTypes"] as const,
  detail: (id: number) => ["procedureTypes", id] as const,
};

function toWire(pt: DomainProcedureType): ProcedureType {
  return {
    idProcedureType: pt.id,
    name: pt.name,
    notes: pt.notes ?? undefined,
    isArchived: pt.isArchived,
    isRegistered: pt.isRegistered,
    associatesProperties: pt.associatesProperties,
    enabled: pt.enabled,
    workflowDefinitionId: pt.workflowDefinitionId,
    workflowDefinitionName: pt.workflowDefinitionName,
  };
}

function toInput(data: Partial<ProcedureType>): ProcedureTypeInput {
  return {
    name: data.name ?? "",
    notes: data.notes,
    isArchived: data.isArchived,
    isRegistered: data.isRegistered,
    associatesProperties: data.associatesProperties,
    enabled: data.enabled,
  };
}

export function useProcedureTypes() {
  return useQuery({
    queryKey: procedureTypeKeys.all,
    queryFn: async () => (await procedureTypeUseCases.list()).map(toWire),
  });
}

export function useSearchProcedureTypes(name: string, fallback: ProcedureType[]) {
  return useQuery({
    queryKey: ["procedureTypes", "search", name, fallback],
    queryFn: async () =>
      name.trim()
        ? (await procedureTypeUseCases.searchByName(name.trim())).map(toWire)
        : fallback,
  });
}

export function useCreateProcedureType() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (data: Partial<ProcedureType>) =>
      toWire(await procedureTypeUseCases.create(toInput(data))),
    onSuccess: () => qc.invalidateQueries({ queryKey: procedureTypeKeys.all }),
  });
}

export function useUpdateProcedureType() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, data }: { id: number; data: Partial<ProcedureType> }) =>
      toWire(await procedureTypeUseCases.update(id, toInput(data))),
    onSuccess: () => qc.invalidateQueries({ queryKey: procedureTypeKeys.all }),
  });
}

export function useDeleteProcedureType() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => procedureTypeUseCases.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: procedureTypeKeys.all }),
  });
}

export function useProcedureTypeInUse() {
  return {
    check: (id: number) => procedureTypeUseCases.isInUse(id),
  };
}

export function useAssignWorkflowToProcedureType() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({
      id,
      workflowDefinitionId,
    }: {
      id: number;
      workflowDefinitionId: number | null;
    }) => toWire(await procedureTypeUseCases.assignWorkflow(id, workflowDefinitionId)),
    onSuccess: () => qc.invalidateQueries({ queryKey: procedureTypeKeys.all }),
  });
}
