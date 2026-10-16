/**
 * React Query hooks for ProcedureTemplate: the required-documents checklist per procedure type
 * (CU03 read, CU55 admin #1362). Hexagon wiring: #1788 / ADR-030.
 */
import { useMemo } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { procedureTemplateUseCases } from "@/composition/procedure-template";
import { useDocumentTypes } from "@/hooks/useDocuments";
import type { ProcedureTemplate, ProcedureTemplateRow } from "@/types";

export const procedureTemplateKeys = {
  root: ["procedureTemplate"] as const,
  all: ["procedureTemplate", "all"] as const,
  byProcedure: (idTypeProcedure: number) =>
    ["procedureTemplate", "procedureType", idTypeProcedure] as const,
};

function toWire(row: {
  procedureTypeId: number;
  documentTypeId: number;
  notes?: string | null;
  version?: number;
}): ProcedureTemplate {
  return {
    fkIdProcedureType: row.procedureTypeId,
    fkIdDocumentType: row.documentTypeId,
    notes: row.notes,
    version: row.version,
  };
}

export function useProcedureTemplate(idTypeProcedure: number | undefined) {
  const templates = useQuery({
    queryKey:
      idTypeProcedure !== undefined
        ? procedureTemplateKeys.byProcedure(idTypeProcedure)
        : ["procedureTemplate", "procedureType", "none"],
    queryFn: async () =>
      (await procedureTemplateUseCases.listByProcedureType(idTypeProcedure!)).map(toWire),
    enabled: idTypeProcedure !== undefined,
  });
  const { data: documentTypes } = useDocumentTypes();
  const data = useMemo<ProcedureTemplateRow[] | undefined>(() => {
    if (!templates.data) return undefined;
    const byId = new Map((documentTypes ?? []).map((d) => [d.idDocumentType, d]));
    return templates.data.map((row) => ({ ...row, documentType: byId.get(row.fkIdDocumentType) }));
  }, [templates.data, documentTypes]);
  return { ...templates, data };
}

/** Every required-document template across all procedure types (#1554). */
export function useAllProcedureTemplates() {
  return useQuery({
    queryKey: procedureTemplateKeys.all,
    queryFn: async () => (await procedureTemplateUseCases.listAll()).map(toWire),
  });
}

export interface RequiredDocumentKey {
  procedureTypeId: number;
  documentTypeId: number;
}

function useInvalidateChecklist() {
  const queryClient = useQueryClient();
  return (procedureTypeId: number) =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: procedureTemplateKeys.byProcedure(procedureTypeId) }),
      queryClient.invalidateQueries({ queryKey: procedureTemplateKeys.all }),
    ]);
}

export function useCreateProcedureTemplate() {
  const invalidate = useInvalidateChecklist();
  return useMutation({
    mutationFn: ({ procedureTypeId, documentTypeId, notes }: RequiredDocumentKey & { notes?: string }) =>
      procedureTemplateUseCases.create({ procedureTypeId, documentTypeId, notes }),
    onSuccess: (_d, v) => invalidate(v.procedureTypeId),
  });
}

export function useUpdateProcedureTemplate() {
  const invalidate = useInvalidateChecklist();
  return useMutation({
    mutationFn: ({ procedureTypeId, documentTypeId, notes }: RequiredDocumentKey & { notes: string }) =>
      procedureTemplateUseCases.update({ procedureTypeId, documentTypeId, notes }),
    onSuccess: (_d, v) => invalidate(v.procedureTypeId),
  });
}

export function useDeleteProcedureTemplate() {
  const invalidate = useInvalidateChecklist();
  return useMutation({
    mutationFn: ({ procedureTypeId, documentTypeId }: RequiredDocumentKey) =>
      procedureTemplateUseCases.delete(procedureTypeId, documentTypeId),
    onSuccess: (_d, v) => invalidate(v.procedureTypeId),
  });
}
